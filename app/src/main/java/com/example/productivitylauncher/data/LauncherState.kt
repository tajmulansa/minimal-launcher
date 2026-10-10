package com.example.productivitylauncher.data

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.productivitylauncher.ui.theme.ThemeMode
import org.json.JSONArray
import org.json.JSONObject

/**
 * All launcher state, saved on the device in SharedPreferences. Nothing leaves the phone.
 * Fields are Compose state, so screens update by themselves when a value changes.
 */
class LauncherState(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences("launcher", Context.MODE_PRIVATE)

    private fun str(k: String, d: String = ""): String = prefs.getString(k, d) ?: d
    private fun int(k: String, d: Int): Int = prefs.getInt(k, d)
    private fun bool(k: String, d: Boolean): Boolean = prefs.getBoolean(k, d)
    private fun long(k: String, d: Long): Long = prefs.getLong(k, d)
    private fun save(block: SharedPreferences.Editor.() -> Unit) {
        val e = prefs.edit()
        e.block()
        e.apply()
    }

    /** Bumped whenever the launcher returns to the foreground, so screens re-check permissions. */
    var resumeTick by mutableIntStateOf(0)
        private set

    fun onResume() { resumeTick++ }

    fun pipFacts(): PipFacts = PipFacts(
        hour = hourOf(),
        frog = frog,
        frogDone = frogDone,
        water = water,
        waterGoal = waterGoal,
        gatedMin = gatedMs?.let { it / 60_000L },
        limitMin = dailyLimitMin,
        activeMin45 = activeMs45?.let { it / 60_000L },
        eveningHour = eveningHour,
        eveningDone = eveningDone,
        dumpCount = dumps.size,
        focusActive = focusActive,
        frogJustDone = frogDone && System.currentTimeMillis() - frogDoneAt < 45 * 60_000L,
        tone = look.tone,
    )

    // ------------------------------------------------------------ apps

    var apps by mutableStateOf<List<AppEntry>>(emptyList())
        private set

    fun refreshApps(pm: PackageManager) {
        val loaded = loadLaunchableApps(pm, appContext.packageName)
        apps = loaded
    }

    fun appByPackage(pkg: String): AppEntry? = apps.firstOrNull { it.packageName == pkg }

    // ------------------------------------------------------------ onboarding and appearance

    var onboarded by mutableStateOf(bool("onboarded", false))
        private set

    fun finishOnboarding() { onboarded = true; save { putBoolean("onboarded", true) } }

    // ------------------------------------------------------------ Vibe (the look and feel)

    /** What the launcher looks like and how Pip talks right now. Saved as separate fields. */
    var look by mutableStateOf(readLook())
        private set

    private fun <E : Enum<E>> enumOf(values: Array<E>, name: String, default: E): E =
        values.firstOrNull { it.name == name } ?: default

    private fun readLook() = Look(
        theme = enumOf(ThemeMode.entries.toTypedArray(), str("theme", "Auto"), DEFAULT_LOOK.theme),
        accent = enumOf(Accent.entries.toTypedArray(), str("accent", "Default"), DEFAULT_LOOK.accent),
        clock = enumOf(ClockFace.entries.toTypedArray(), str("clock_face", "Classic"), DEFAULT_LOOK.clock),
        layout = enumOf(HomeLayout.entries.toTypedArray(), str("home_layout", "Cards"), DEFAULT_LOOK.layout),
        tone = enumOf(PipTone.entries.toTypedArray(), str("pip_tone", "Friendly"), DEFAULT_LOOK.tone),
    )

    fun updateLook(l: Look) {
        look = l
        save {
            putString("theme", l.theme.name)
            putString("accent", l.accent.name)
            putString("clock_face", l.clock.name)
            putString("home_layout", l.layout.name)
            putString("pip_tone", l.tone.name)
        }
    }

    var customVibes by mutableStateOf(readCustomVibes())
        private set

    private fun readCustomVibes(): List<Vibe> = runCatching {
        val arr = JSONArray(str("custom_vibes", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Vibe(
                id = o.getString("id"),
                name = o.getString("name"),
                tagline = "Made by you",
                look = Look(
                    enumOf(ThemeMode.entries.toTypedArray(), o.optString("theme"), DEFAULT_LOOK.theme),
                    enumOf(Accent.entries.toTypedArray(), o.optString("accent"), DEFAULT_LOOK.accent),
                    enumOf(ClockFace.entries.toTypedArray(), o.optString("clock"), DEFAULT_LOOK.clock),
                    enumOf(HomeLayout.entries.toTypedArray(), o.optString("layout"), DEFAULT_LOOK.layout),
                    enumOf(PipTone.entries.toTypedArray(), o.optString("tone"), DEFAULT_LOOK.tone),
                ),
                custom = true,
            )
        }
    }.getOrDefault(emptyList())

    private fun writeCustomVibes(list: List<Vibe>) {
        customVibes = list
        val arr = JSONArray()
        list.forEach { v ->
            arr.put(
                JSONObject().put("id", v.id).put("name", v.name)
                    .put("theme", v.look.theme.name).put("accent", v.look.accent.name)
                    .put("clock", v.look.clock.name).put("layout", v.look.layout.name).put("tone", v.look.tone.name),
            )
        }
        save { putString("custom_vibes", arr.toString()) }
    }

    /** The Vibe the current look matches, or null when the look has been changed by hand. */
    val activeVibe: Vibe? get() = (PRESET_VIBES + customVibes).firstOrNull { it.look == look }

    fun saveVibe(name: String) {
        val n = name.trim().take(24)
        if (n.isBlank()) return
        writeCustomVibes(customVibes + Vibe(System.nanoTime().toString(), n, "Made by you", look, custom = true))
    }

    fun deleteVibe(id: String) = writeCustomVibes(customVibes.filterNot { it.id == id })

    val themeMode: ThemeMode get() = look.theme

    var pipOn by mutableStateOf(bool("pip_on", true))
        private set

    fun updatePipOn(v: Boolean) { pipOn = v; save { putBoolean("pip_on", v) } }

    // ------------------------------------------------------------ frog

    var frog by mutableStateOf(str("frog"))
        private set
    var frogDoneDay by mutableStateOf(str("frog_done_day"))
        private set
    /** When the frog was marked eaten (milliseconds), so Pip can react right away. */
    var frogDoneAt by mutableStateOf(long("frog_done_at", 0L))
        private set
    var tomorrowFrog by mutableStateOf(str("tomorrow_frog"))
        private set
    var frogMinutes by mutableStateOf(int("frog_minutes", 25))
        private set
    var eatenDays by mutableStateOf(prefs.getStringSet("eaten_days", emptySet()) ?: emptySet())
        private set

    val frogDone: Boolean get() = frogDoneDay == dayKey()

    fun updateFrog(text: String) { frog = text; save { putString("frog", text) } }
    fun updateTomorrowFrog(text: String) { tomorrowFrog = text; save { putString("tomorrow_frog", text) } }
    fun updateFrogMinutes(m: Int) { frogMinutes = m.coerceIn(5, 120); save { putInt("frog_minutes", frogMinutes) } }

    fun setFrogDone(done: Boolean) {
        val today = dayKey()
        if (done) {
            frogDoneDay = today
            frogDoneAt = System.currentTimeMillis()
            eatenDays = eatenDays + today
        } else {
            frogDoneDay = ""
            frogDoneAt = 0L
            eatenDays = eatenDays - today
        }
        save {
            putLong("frog_done_at", frogDoneAt)
            putString("frog_done_day", frogDoneDay)
            putStringSet("eaten_days", eatenDays)
        }
    }

    // ------------------------------------------------------------ the other tasks in the to-do list

    var tasks by mutableStateOf(readTasks())
        private set

    private fun readTasks(): List<Task> = runCatching {
        val arr = JSONArray(str("tasks", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Task(o.getString("id"), o.getString("text"), o.optBoolean("done", false))
        }
    }.getOrDefault(emptyList())

    private fun writeTasks(list: List<Task>) {
        tasks = list
        val arr = JSONArray()
        list.forEach { t -> arr.put(JSONObject().put("id", t.id).put("text", t.text).put("done", t.done)) }
        save { putString("tasks", arr.toString()) }
    }

    /** The other tasks wait until the frog is eaten. With no frog set, nothing waits. */
    val tasksLocked: Boolean get() = frog.isNotBlank() && !frogDone

    fun addTask(text: String) {
        if (text.isBlank()) return
        writeTasks(tasks + Task(System.nanoTime().toString(), text.trim()))
    }

    fun toggleTask(id: String) {
        if (tasksLocked) return
        writeTasks(tasks.map { if (it.id == id) it.copy(done = !it.done) else it })
    }

    fun removeTask(id: String) = writeTasks(tasks.filterNot { it.id == id })

    /** Makes a task today's frog. A frog that is not done yet goes back into the list. Not allowed once the frog is eaten. */
    fun makeFrog(id: String) {
        if (frogDone) return
        val t = tasks.firstOrNull { it.id == id } ?: return
        val old = frog
        val rest = tasks.filterNot { it.id == id }
        writeTasks(if (old.isNotBlank()) listOf(Task(System.nanoTime().toString(), old)) + rest else rest)
        updateFrog(t.text)
    }

    // ------------------------------------------------------------ water, note

    var water by mutableStateOf(int("water", 0))
        private set
    var waterGoal by mutableStateOf(int("water_goal", 8))
        private set

    fun updateWater(n: Int) { water = n.coerceIn(0, 30); save { putInt("water", water) } }
    fun updateWaterGoal(n: Int) { waterGoal = n.coerceIn(1, 16); save { putInt("water_goal", waterGoal) } }

    var note by mutableStateOf(str("note"))
        private set

    fun updateNote(t: String) { note = t; save { putString("note", t) } }

    // ------------------------------------------------------------ habits and brain dump

    var habits by mutableStateOf(readHabits())
        private set

    private fun readHabits(): List<Habit> = runCatching {
        val arr = JSONArray(str("habits", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            val days = o.optJSONArray("done")
            Habit(
                id = o.getString("id"),
                anchor = o.getString("anchor"),
                action = o.getString("action"),
                doneDays = if (days == null) emptySet() else (0 until days.length()).map { days.getString(it) }.toSet(),
            )
        }
    }.getOrDefault(emptyList())

    private fun writeHabits(list: List<Habit>) {
        habits = list
        val arr = JSONArray()
        list.forEach { h ->
            val o = JSONObject()
            o.put("id", h.id)
            o.put("anchor", h.anchor)
            o.put("action", h.action)
            o.put("done", JSONArray(h.doneDays.toList()))
            arr.put(o)
        }
        save { putString("habits", arr.toString()) }
    }

    fun addHabit(anchor: String, action: String) {
        if (anchor.isBlank() || action.isBlank()) return
        writeHabits(habits + Habit(System.nanoTime().toString(), anchor.trim(), action.trim()))
    }

    fun removeHabit(id: String) = writeHabits(habits.filterNot { it.id == id })

    fun toggleHabit(id: String) {
        val today = dayKey()
        writeHabits(habits.map {
            if (it.id != id) it else it.copy(doneDays = if (today in it.doneDays) it.doneDays - today else it.doneDays + today)
        })
    }

    var dumps by mutableStateOf(readDumps())
        private set

    private fun readDumps(): List<Dump> = runCatching {
        val arr = JSONArray(str("dumps", "[]"))
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Dump(o.getString("id"), o.getString("text"), o.optLong("at", 0L))
        }
    }.getOrDefault(emptyList())

    private fun writeDumps(list: List<Dump>) {
        dumps = list
        val arr = JSONArray()
        list.forEach { d ->
            val o = JSONObject()
            o.put("id", d.id)
            o.put("text", d.text)
            o.put("at", d.createdAt)
            arr.put(o)
        }
        save { putString("dumps", arr.toString()) }
    }

    fun addDump(text: String) {
        if (text.isBlank()) return
        writeDumps(listOf(Dump(System.nanoTime().toString(), text.trim(), System.currentTimeMillis())) + dumps)
    }

    fun removeDump(id: String) = writeDumps(dumps.filterNot { it.id == id })

    // ------------------------------------------------------------ gated apps and home apps

    var gated by mutableStateOf(prefs.getStringSet("gated", emptySet()) ?: emptySet())
        private set
    var pendingRemoval by mutableStateOf(readLongMap("pending_removal"))
        private set
    var homeApps by mutableStateOf(readStringList("home_apps"))
        private set

    private fun readStringList(k: String): List<String> = runCatching {
        val arr = JSONArray(str(k, "[]"))
        (0 until arr.length()).map { arr.getString(it) }
    }.getOrDefault(emptyList())

    private fun readLongMap(k: String): Map<String, Long> = runCatching {
        val o = JSONObject(str(k, "{}"))
        o.keys().asSequence().associateWith { o.getLong(it) }
    }.getOrDefault(emptyMap())

    private fun writeLongMap(k: String, m: Map<String, Long>) {
        val o = JSONObject()
        m.forEach { (key, v) -> o.put(key, v) }
        save { putString(k, o.toString()) }
    }

    fun isGated(pkg: String) = pkg in gated

    fun addGated(pkg: String) {
        gated = gated + pkg
        pendingRemoval = pendingRemoval - pkg
        // A gated app does not belong on the Home list.
        if (pkg in homeApps) updateHomeApps(homeApps - pkg)
        save { putStringSet("gated", gated) }
        writeLongMap("pending_removal", pendingRemoval)
    }

    /** Removing a gate waits 24 hours, so a weak moment cannot undo it. */
    fun requestRemoveGated(pkg: String) {
        if (pkg !in gated || pkg in pendingRemoval) return
        pendingRemoval = pendingRemoval + (pkg to System.currentTimeMillis() + WEAKEN_DELAY_MS)
        writeLongMap("pending_removal", pendingRemoval)
    }

    fun cancelRemoveGated(pkg: String) {
        pendingRemoval = pendingRemoval - pkg
        writeLongMap("pending_removal", pendingRemoval)
    }

    fun updateHomeApps(list: List<String>) {
        homeApps = list.distinct().take(6)
        val arr = JSONArray(homeApps)
        save { putString("home_apps", arr.toString()) }
    }

    // ------------------------------------------------------------ gate settings (weakening waits 24h)

    private var limitValue by mutableStateOf(int("limit", 60))
    private var breathsValue by mutableStateOf(int("breaths", 3))
    private var wordsValue by mutableStateOf(int("phrase_words", 5))
    private var growingValue by mutableStateOf(int("growing_wait", 1))
    var pendingChanges by mutableStateOf(readPending())
        private set

    val dailyLimitMin: Int get() = limitValue
    val breaths: Int get() = breathsValue
    val phraseWords: Int get() = wordsValue
    val growingWait: Boolean get() = growingValue == 1

    private fun readPending(): Map<String, PendingChange> = runCatching {
        val o = JSONObject(str("pending_changes", "{}"))
        o.keys().asSequence().associateWith {
            val p = o.getJSONObject(it)
            PendingChange(p.getInt("v"), p.getLong("at"))
        }
    }.getOrDefault(emptyMap())

    private fun writePending(m: Map<String, PendingChange>) {
        pendingChanges = m
        val o = JSONObject()
        m.forEach { (k, p) -> o.put(k, JSONObject().put("v", p.value).put("at", p.applyAt)) }
        save { putString("pending_changes", o.toString()) }
    }

    private fun current(s: GateSetting): Int = when (s) {
        GateSetting.Limit -> limitValue
        GateSetting.Breaths -> breathsValue
        GateSetting.PhraseWords -> wordsValue
        GateSetting.GrowingWait -> growingValue
    }

    private fun applyNow(s: GateSetting, v: Int) {
        when (s) {
            GateSetting.Limit -> limitValue = v
            GateSetting.Breaths -> breathsValue = v
            GateSetting.PhraseWords -> wordsValue = v
            GateSetting.GrowingWait -> growingValue = v
        }
        save { putInt(s.key, v) }
    }

    /** Stronger changes apply at once. Weaker ones are saved as pending and apply after 24 hours. */
    fun changeGateSetting(s: GateSetting, v: Int) {
        val old = current(s)
        if (v == old) {
            writePending(pendingChanges - s.key)
        } else if (isWeakening(s, old, v)) {
            writePending(pendingChanges + (s.key to PendingChange(v, System.currentTimeMillis() + WEAKEN_DELAY_MS)))
        } else {
            applyNow(s, v)
            writePending(pendingChanges - s.key)
        }
    }

    fun pendingFor(s: GateSetting): PendingChange? = pendingChanges[s.key]

    /** Applies every pending change and gate removal whose 24 hours have passed. Call on start and resume. */
    fun applyPending() {
        val now = System.currentTimeMillis()
        val due = pendingChanges.filter { it.value.applyAt <= now }
        if (due.isNotEmpty()) {
            due.forEach { (key, p) ->
                GateSetting.entries.firstOrNull { it.key == key }?.let { applyNow(it, p.value) }
            }
            writePending(pendingChanges - due.keys)
        }
        val removable = pendingRemoval.filter { it.value <= now }.keys
        if (removable.isNotEmpty()) {
            gated = gated - removable
            pendingRemoval = pendingRemoval - removable
            save { putStringSet("gated", gated) }
            writeLongMap("pending_removal", pendingRemoval)
        }
    }

    // ------------------------------------------------------------ gate bookkeeping

    private var opensDay = str("opens_day")
    private var opens by mutableStateOf(readOpens())

    private fun readOpens(): Map<String, Long> =
        if (str("opens_day") == dayKey()) readLongMap("opens") else emptyMap()

    fun opensToday(pkg: String): Int {
        if (opensDay != dayKey()) return 0
        return (opens[pkg] ?: 0L).toInt()
    }

    fun recordOpen(pkg: String) {
        val today = dayKey()
        if (opensDay != today) { opensDay = today; opens = emptyMap() }
        opens = opens + (pkg to (opens[pkg] ?: 0L) + 1)
        save { putString("opens_day", today) }
        writeLongMap("opens", opens)
    }

    var backedOut by mutableStateOf(readLongMap("backed_out"))
        private set

    fun recordBackOut() {
        val today = dayKey()
        backedOut = backedOut + (today to (backedOut[today] ?: 0L) + 1)
        writeLongMap("backed_out", backedOut)
    }

    fun backedOutOn(day: String): Int = (backedOut[day] ?: 0L).toInt()

    // Minutes the user chose in gates today. Used for the daily limit when usage access is not granted.
    private var bookedDay = str("booked_day")
    var bookedMs by mutableStateOf(if (str("booked_day") == dayKey()) long("booked_ms", 0L) else 0L)
        private set

    private fun addBooked(ms: Long) {
        val today = dayKey()
        if (bookedDay != today) { bookedDay = today; bookedMs = 0L }
        bookedMs += ms
        save { putString("booked_day", today); putLong("booked_ms", bookedMs) }
    }

    // ------------------------------------------------------------ sessions

    var sessionPkg by mutableStateOf(str("session_pkg"))
        private set
    var sessionEnd by mutableStateOf(long("session_end", 0L))
        private set
    var sessionNotified by mutableStateOf(bool("session_notified", true))
        private set

    val sessionActive: Boolean get() = sessionEnd > System.currentTimeMillis()

    fun startSession(pkg: String, minutes: Int) {
        sessionPkg = pkg
        sessionEnd = System.currentTimeMillis() + minutes * 60_000L
        sessionNotified = false
        addBooked(minutes * 60_000L)
        save {
            putString("session_pkg", pkg)
            putLong("session_end", sessionEnd)
            putBoolean("session_notified", false)
        }
    }

    /** Re-reads session fields that the floating timer service may have changed. */
    fun syncSession() {
        sessionPkg = str("session_pkg")
        sessionEnd = long("session_end", 0L)
        sessionNotified = bool("session_notified", true)
    }

    fun endSessionEarly() {
        sessionEnd = System.currentTimeMillis()
        save { putLong("session_end", sessionEnd) }
    }

    /** True once, when a session has run out and the user has not yet seen "Time's up". */
    fun consumeTimesUp(): Boolean {
        if (sessionPkg.isNotEmpty() && !sessionNotified && sessionEnd <= System.currentTimeMillis()) {
            sessionNotified = true
            save { putBoolean("session_notified", true) }
            return true
        }
        return false
    }

    // ------------------------------------------------------------ focus

    var focusEnd by mutableStateOf(long("focus_end", 0L))
        private set
    var focusTotalMs by mutableStateOf(long("focus_total", 0L))
        private set
    var focusDnd by mutableStateOf(bool("focus_dnd", true))
        private set
    var focusLockGated by mutableStateOf(bool("focus_lock", true))
        private set

    val focusActive: Boolean get() = focusEnd > System.currentTimeMillis()

    fun startFocus(minutes: Int) {
        focusTotalMs = minutes * 60_000L
        focusEnd = System.currentTimeMillis() + focusTotalMs
        save { putLong("focus_end", focusEnd); putLong("focus_total", focusTotalMs) }
    }

    fun stopFocus() {
        focusEnd = 0L
        save { putLong("focus_end", 0L) }
    }

    fun updateFocusDnd(v: Boolean) { focusDnd = v; save { putBoolean("focus_dnd", v) } }
    fun updateFocusLockGated(v: Boolean) { focusLockGated = v; save { putBoolean("focus_lock", v) } }

    // ------------------------------------------------------------ evening and goals

    var eveningHour by mutableStateOf(int("evening_hour", 21))
        private set
    var eveningDoneDay by mutableStateOf(str("evening_done"))
        private set
    var screenGoalMin by mutableStateOf(int("screen_goal", 180))
        private set

    val eveningDone: Boolean get() = eveningDoneDay == dayKey()

    fun updateEveningHour(h: Int) { eveningHour = h.coerceIn(18, 23); save { putInt("evening_hour", eveningHour) } }
    fun finishEvening() { eveningDoneDay = dayKey(); save { putString("evening_done", eveningDoneDay) } }
    fun setScreenGoal(m: Int) { screenGoalMin = m.coerceIn(30, 600); save { putInt("screen_goal", screenGoalMin) } }

    // ------------------------------------------------------------ widgets page

    var widgets by mutableStateOf(readStringList("widgets").ifEmpty { DEFAULT_WIDGETS })
        private set

    var widgetHeights by mutableStateOf(readLongMap("widget_heights"))
        private set

    fun widgetHeight(id: String, default: Int): Int = (widgetHeights[id] ?: default.toLong()).toInt()

    fun updateWidgetHeight(id: String, dp: Int) {
        widgetHeights = widgetHeights + (id to dp.coerceIn(80, 600).toLong())
        writeLongMap("widget_heights", widgetHeights)
    }

    fun hasWidget(id: String) = id in widgets

    fun toggleWidget(id: String) = updateWidgets(if (id in widgets) widgets - id else widgets + id)

    fun addWidgetId(id: String) { if (id !in widgets) updateWidgets(widgets + id) }

    fun removeWidgetId(id: String) = updateWidgets(widgets - id)

    private fun updateWidgets(list: List<String>) {
        widgets = list
        save { putString("widgets", JSONArray(list).toString()) }
    }

    // ------------------------------------------------------------ usage numbers (read-only, from UsageStats)

    var usageAccess by mutableStateOf(false)
        private set
    var screenTimeMs by mutableStateOf<Long?>(null)
        private set
    var gatedMs by mutableStateOf<Long?>(null)
        private set
    var activeMs45 by mutableStateOf<Long?>(null)
        private set
    var weekMs by mutableStateOf<List<Long>>(emptyList())
        private set

    /** Reads usage numbers on the calling thread. Call it off the main thread. */
    fun refreshUsage() {
        val reader = UsageReader(appContext)
        val access = reader.hasAccess()
        if (!access) {
            usageAccess = false
            screenTimeMs = null
            gatedMs = null
            activeMs45 = null
            weekMs = emptyList()
            return
        }
        val now = System.currentTimeMillis()
        val dayStart = startOfDay(now)
        val perApp = reader.perPackage(dayStart, now)
        val own = appContext.packageName
        val total = perApp.filterKeys { it != own }.values.sum()
        val gatedTotal = perApp.filterKeys { it in gated }.values.sum()
        val week = (6 downTo 0).map { back ->
            val s = startOfDay(now - back * DAY_MS)
            val e = if (back == 0) now else s + DAY_MS
            reader.perPackage(s, e).filterKeys { it != own }.values.sum()
        }
        usageAccess = true
        screenTimeMs = total
        gatedMs = gatedTotal
        activeMs45 = reader.activeMsInLast(45 * 60_000L)
        weekMs = week
    }

    /** Time used in gated apps today: real usage if allowed, otherwise the minutes the user booked. */
    val gatedUsedMs: Long get() = gatedMs ?: bookedMs

    val limitReached: Boolean get() = gatedUsedMs >= dailyLimitMin * 60_000L

    // ------------------------------------------------------------ daily rollover and reset

    /** Starts a new day: resets water, moves tomorrow's frog in, clears a finished frog, carries an unfinished one over. */
    fun rollover() {
        val today = dayKey()
        val last = str("last_day")
        if (last == today) return
        if (last.isNotEmpty()) {
            if (tomorrowFrog.isNotBlank()) {
                updateFrog(tomorrowFrog)
                updateTomorrowFrog("")
            } else if (frogDoneDay.isNotEmpty() && frogDoneDay != today) {
                updateFrog("")
            }
        }
        // Finished tasks are cleared for the new day. Unfinished ones carry over.
        if (last.isNotEmpty() && tasks.any { it.done }) writeTasks(tasks.filterNot { it.done })
        updateWater(0)
        save { putString("last_day", today) }
    }

    /** Deletes everything this app stored. */
    fun resetAll() {
        prefs.edit().clear().apply()
        frog = ""; frogDoneDay = ""; frogDoneAt = 0L; tomorrowFrog = ""; frogMinutes = 25; eatenDays = emptySet()
        water = 0; waterGoal = 8; note = ""
        habits = emptyList(); dumps = emptyList(); tasks = emptyList()
        gated = emptySet(); pendingRemoval = emptyMap(); homeApps = emptyList()
        limitValue = 60; breathsValue = 3; wordsValue = 5; growingValue = 1; pendingChanges = emptyMap()
        opens = emptyMap(); backedOut = emptyMap(); bookedMs = 0L
        sessionPkg = ""; sessionEnd = 0L; sessionNotified = true
        focusEnd = 0L; focusDnd = true; focusLockGated = true
        eveningHour = 21; eveningDoneDay = ""; screenGoalMin = 180
        widgets = DEFAULT_WIDGETS; widgetHeights = emptyMap()
        look = DEFAULT_LOOK; customVibes = emptyList(); pipOn = true; onboarded = false
    }
}
