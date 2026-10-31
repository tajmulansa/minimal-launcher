package com.example.productivitylauncher.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.random.Random

/*
 * Pure Kotlin helpers. Nothing here touches Android, so it is unit tested (see LogicTest).
 */

const val DAY_MS = 24L * 60 * 60 * 1000
const val WEAKEN_DELAY_MS = DAY_MS

val PHRASE_WORDS = listOf(
    "river", "candle", "mountain", "paper", "garden", "window", "orange", "pencil",
    "forest", "silver", "morning", "bridge", "lantern", "meadow", "harbor", "copper",
    "cloud", "maple", "pebble", "anchor", "violet", "thunder", "feather", "compass",
    "blanket", "island", "marble", "saddle", "whistle", "velvet", "button", "ladder",
)

fun generatePhrase(words: Int, random: Random = Random.Default): String =
    PHRASE_WORDS.shuffled(random).take(words.coerceIn(2, 8)).joinToString(" ")

fun normalizePhrase(s: String): String =
    s.trim().lowercase(Locale.ROOT).replace(Regex("\\s+"), " ")

fun phraseMatches(typed: String, target: String): Boolean =
    normalizePhrase(typed) == normalizePhrase(target)

/** How many leading characters of [typed] match [target] (used for the progress counter). */
fun phraseProgress(typed: String, target: String): Int {
    val a = typed.lowercase(Locale.ROOT)
    val b = target.lowercase(Locale.ROOT)
    var n = 0
    while (n < a.length && n < b.length && a[n] == b[n]) n++
    return n
}

/** Each open of a gated app today adds 5 seconds to the wait, up to one minute. */
fun extraWaitSeconds(opensToday: Int, growing: Boolean): Int =
    if (!growing) 0 else (opensToday.coerceAtLeast(0) * 5).coerceAtMost(60)

/** True when the new value makes the gate easier to get through. */
fun isWeakening(setting: GateSetting, old: Int, new: Int): Boolean = when (setting) {
    GateSetting.Limit -> new > old
    GateSetting.Breaths, GateSetting.PhraseWords, GateSetting.GrowingWait -> new < old
}

/** Lock In: an app is found only by typing its full name (any capitals, spaces at the ends ignored). */
fun exactNameMatches(apps: List<AppEntry>, query: String): List<AppEntry> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return apps.filter { it.label.trim().equals(q, ignoreCase = true) }
}

fun formatMinutes(totalMinutes: Long): String {
    val m = totalMinutes.coerceAtLeast(0)
    val h = m / 60
    val r = m % 60
    return if (h > 0) "${h}h ${r}m" else "${r}m"
}

fun formatMillis(ms: Long): String = formatMinutes(ms / 60_000)

/** mm:ss for countdowns. */
fun formatClock(ms: Long): String {
    val total = (ms.coerceAtLeast(0) + 999) / 1000
    return "%02d:%02d".format(Locale.US, total / 60, total % 60)
}

fun dayKey(millis: Long = System.currentTimeMillis()): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))

fun startOfDay(millis: Long = System.currentTimeMillis()): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

fun hourOf(millis: Long = System.currentTimeMillis()): Int {
    val c = Calendar.getInstance()
    c.timeInMillis = millis
    return c.get(Calendar.HOUR_OF_DAY)
}

/** Alphabetical section header for an app label ("#" for anything that is not a letter). */
/** The first whole character of a label (an emoji is two Chars, so never split it). */
private fun firstSymbol(label: String): String {
    val s = label.trim()
    if (s.isEmpty()) return ""
    return String(Character.toChars(s.codePointAt(0)))
}

/** The list section for an app: A to Z, with accents dropped ("École" is E), or # for everything else. */
fun sectionOf(label: String): String {
    val first = firstSymbol(label)
    if (first.isEmpty()) return "#"
    val base = java.text.Normalizer.normalize(first, java.text.Normalizer.Form.NFD).firstOrNull()?.uppercaseChar() ?: return "#"
    return if (base in 'A'..'Z') base.toString() else "#"
}

/** The big letter on an app badge. Emoji and non-Latin first characters stay whole. */
fun letterOf(label: String): String {
    val first = firstSymbol(label)
    return if (first.isEmpty()) "?" else first.uppercase()
}

// ---------------------------------------------------------------- Pip
//
// Pip is a reminder, a supporter and a motivator. It always knows the time of day:
// good morning in the morning, an afternoon check-in, an evening wind-down, and sleep talk at night.
// When you finish something it says well done, and after you eat the frog it tells you to look after yourself.

enum class PipAction { None, Focus, SetFrog, Water, Evening, BrainDump }

enum class DayPart { Morning, Afternoon, Evening, Night }

/** 5 to 11 morning, 12 to 16 afternoon, 17 to 20 evening, 21 to 4 night. */
fun dayPartOf(hour: Int): DayPart = when (hour) {
    in 5..11 -> DayPart.Morning
    in 12..16 -> DayPart.Afternoon
    in 17..20 -> DayPart.Evening
    else -> DayPart.Night
}

fun greetingFor(part: DayPart): String = when (part) {
    DayPart.Morning -> "Good morning!"
    DayPart.Afternoon -> "Good afternoon!"
    DayPart.Evening -> "Good evening!"
    DayPart.Night -> "Hey, still up?"
}

data class PipMessage(
    val face: String,
    val text: String,
    val primaryLabel: String?,
    val action: PipAction,
    val nudge: Boolean,
)

/** Everything Pip knows. Null usage values mean usage access is not granted. All of it stays on the phone. */
data class PipFacts(
    val hour: Int,
    val frog: String,
    val frogDone: Boolean,
    val water: Int,
    val waterGoal: Int,
    val gatedMin: Long?,
    val limitMin: Int,
    val activeMin45: Long?,
    val eveningHour: Int,
    val eveningDone: Boolean,
    val dumpCount: Int,
    val focusActive: Boolean,
    /** True for 45 minutes after the frog was marked eaten. */
    val frogJustDone: Boolean = false,
)

private fun pick(options: List<String>, variant: Int): String = options[Math.floorMod(variant, options.size)]

private fun waterBehind(f: PipFacts): Boolean =
    f.water + 2 <= (f.waterGoal * (f.hour.coerceIn(8, 21) - 8) / 13)

fun pipMessage(f: PipFacts, variant: Int = 0): PipMessage {
    val part = dayPartOf(f.hour)
    val hello = greetingFor(part)

    // 1. Focus: stay quiet and encourage.
    if (f.focusActive) {
        return PipMessage("[-_-]", pick(listOf("Shh. I'm quiet while you focus. You've got this.", "Deep work. I'll be here when you're done."), variant), null, PipAction.None, false)
    }

    // 2. Just ate the frog: appreciate it, then look after yourself.
    if (f.frogJustDone) {
        return if (waterBehind(f) || f.water == 0) {
            PipMessage("[^o^]", "You ate the frog! I'm really proud of you. Reward yourself: drink a glass of water and stretch for a minute.", "Drank one", PipAction.Water, true)
        } else {
            PipMessage("[^o^]", "You ate the frog! Amazing work. You've earned a 5 minute break, stand up and stretch.", null, PipAction.None, true)
        }
    }

    // 3. Long phone stretch.
    if ((f.activeMin45 ?: 0) >= 40) {
        return PipMessage("[o_o]", "You've been on your phone for a long stretch. Stand up, look far away for a minute, then pick one thing.", null, PipAction.None, true)
    }

    // 4. Time to wind down, or time to sleep.
    if (f.hour >= f.eveningHour && !f.eveningDone && f.hour < 23) {
        return PipMessage("[-_-]", "Good evening! The day is nearly over. Let's close it and pick tomorrow's frog.", "Close the day", PipAction.Evening, true)
    }
    if (f.hour >= 23 || f.hour < 5) {
        return PipMessage("[-_-]", "It's late. Sleep helps you remember what you studied. Put the phone down and rest well.", null, PipAction.None, true)
    }

    // 5. The frog.
    if (f.frog.isBlank()) {
        return if (part == DayPart.Evening) {
            PipMessage("[^_^]", "$hello What will tomorrow's frog be? Pick it tonight and you can start fast.", "Close the day", PipAction.Evening, true)
        } else {
            PipMessage("[^_^]", "$hello What's the one thing that matters most today? Set your frog.", "Set my frog", PipAction.SetFrog, true)
        }
    }
    if (!f.frogDone) {
        return when (part) {
            DayPart.Morning -> PipMessage("[^_^]", "$hello Your frog today: \"${f.frog}\". Start with just two minutes.", "Start focus", PipAction.Focus, true)
            DayPart.Afternoon -> PipMessage("[>_<]", "$hello Your frog \"${f.frog}\" is still waiting. Two minutes to start?", "Start focus", PipAction.Focus, true)
            else -> PipMessage("[>_<]", "$hello \"${f.frog}\" is still not done. A short focus session now, or tomorrow is fine too.", "Start focus", PipAction.Focus, true)
        }
    }

    // 6. Water.
    if (waterBehind(f) || (part == DayPart.Morning && f.water == 0)) {
        return PipMessage("[^o^]", if (part == DayPart.Morning && f.water == 0) "$hello Start the day with a glass of water." else "Time for some water. A glass now keeps the headache away.", "Drank one", PipAction.Water, true)
    }

    // 7. Gated apps almost used up.
    if (f.gatedMin != null && f.limitMin > 0 && f.gatedMin * 100 >= f.limitMin * 80L) {
        return PipMessage("[o_o]", "Your gated apps are almost used up for today. Save the rest for something you'll enjoy.", null, PipAction.None, true)
    }

    // 8. The frog is eaten: appreciate.
    if (f.frogDone) {
        val text = when (part) {
            DayPart.Morning -> "$hello Your frog is already eaten. What a great start."
            DayPart.Afternoon -> "$hello You ate your frog today. Be proud of that, and enjoy the rest of your day."
            else -> "$hello You ate your frog today. Well done. Time to relax."
        }
        return PipMessage("[^o^]", text, null, PipAction.None, false)
    }

    if (f.dumpCount > 0) {
        return PipMessage("[^_^]", "You have ${f.dumpCount} thought${if (f.dumpCount == 1) "" else "s"} in your brain dump. Sort them when you're ready.", "Open it", PipAction.BrainDump, false)
    }

    return PipMessage("[^_^]", "$hello " + pick(listOf("I'm here if you need a nudge.", "One small step is enough."), variant), null, PipAction.None, false)
}

/** Whole days from [today] to the exam day (both as epoch days). Negative once the exam has passed. */
fun daysUntil(examEpochDay: Long, today: Long = java.time.LocalDate.now().toEpochDay()): Long = examEpochDay - today

/** 7:00 AM style text for minutes after midnight. */
fun formatTimeOfDay(min: Int): String {
    val h = (min / 60) % 24
    val m = min % 60
    val h12 = if (h % 12 == 0) 12 else h % 12
    return "%d:%02d %s".format(h12, m, if (h < 12) "AM" else "PM")
}

// ---------------------------------------------------------------- Home greeting and wallpaper text

/** True between bedtime and wake time, also when bedtime is after midnight (for example 01:00 to 07:00). */
fun isAsleep(minuteOfDay: Int, wakeMin: Int, bedMin: Int): Boolean =
    if (bedMin > wakeMin) minuteOfDay >= bedMin || minuteOfDay < wakeMin
    else minuteOfDay >= bedMin && minuteOfDay < wakeMin

/** The Home greeting with its full stop: "Good morning.", or "Rest well." while asleep. */
fun homeGreeting(minuteOfDay: Int, wakeMin: Int, bedMin: Int): String = when {
    isAsleep(minuteOfDay, wakeMin, bedMin) -> "Rest well."
    minuteOfDay < 12 * 60 -> "Good morning."
    minuteOfDay < 17 * 60 -> "Good afternoon."
    else -> "Good evening."
}

/** The text under "Up next" on Home and on the Still wallpaper. */
fun upNextText(nextTask: String?, hasAnyTask: Boolean): String =
    nextTask ?: if (hasAnyTask) "All done for today" else "Add a task on Study"

/** Hour as shown on the clock: 12 hour (1 to 12) or 24 hour (0 to 23), as in the Home clock. */
fun clockHour(hour24: Int, use24: Boolean): Int =
    if (use24) hour24 else (hour24 % 12).let { if (it == 0) 12 else it }

/** "07:05" style text used by the wallpaper clock (the Home clock draws the same digits one by one). */
fun clockText(hour24: Int, minute: Int, use24: Boolean): String =
    "%02d:%02d".format(Locale.US, clockHour(hour24, use24), minute)
