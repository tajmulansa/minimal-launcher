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
fun sectionOf(label: String): String {
    val c = label.trim().firstOrNull()?.uppercaseChar() ?: return "#"
    return if (c in 'A'..'Z') c.toString() else "#"
}

fun letterOf(label: String): String =
    label.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"

// ---------------------------------------------------------------- Pip

enum class PipAction { None, Focus, SetFrog, Water, Evening, BrainDump }

data class PipMessage(
    val face: String,
    val text: String,
    val primaryLabel: String?,
    val action: PipAction,
    val nudge: Boolean,
)

/** Everything Pip needs to pick a line. Null usage values mean usage access is not granted. */
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
)

fun pipMessage(f: PipFacts, variant: Int = 0): PipMessage {
    if (f.focusActive) {
        return PipMessage("[-_-]", "Shh. I'm quiet while you focus. You've got this.", null, PipAction.None, false)
    }
    if ((f.activeMin45 ?: 0) >= 40) {
        return PipMessage(
            "[o_o]",
            "That was a long stretch on your phone. Stand up, look far away for a minute, then pick one thing.",
            null, PipAction.None, true,
        )
    }
    if (f.hour >= f.eveningHour && !f.eveningDone) {
        return PipMessage("[-_-]", "The day is nearly over. Let's close it and pick tomorrow's frog.", "Close the day", PipAction.Evening, true)
    }
    if (f.frog.isBlank()) {
        return PipMessage("[^_^]", "What's the one thing that matters most today? Set your frog.", "Set my frog", PipAction.SetFrog, true)
    }
    if (!f.frogDone && f.hour >= 12) {
        return PipMessage("[>_<]", "Your frog is still waiting: \"${f.frog}\". Just two minutes to start?", "Start focus", PipAction.Focus, true)
    }
    val expectedWater = (f.waterGoal * (f.hour.coerceIn(8, 21) - 8) / 13)
    if (f.water + 2 <= expectedWater) {
        return PipMessage("[^o^]", "Time for some water. A glass now keeps the headache away.", "Drank one", PipAction.Water, true)
    }
    if (f.gatedMin != null && f.limitMin > 0 && f.gatedMin * 100 >= f.limitMin * 80L) {
        return PipMessage("[o_o]", "Your gated apps are almost used up for today. Save the rest for something you'll enjoy.", null, PipAction.None, true)
    }
    if (f.frogDone) {
        return PipMessage("[^o^]", "Frog eaten! Everything else today is a bonus.", null, PipAction.None, false)
    }
    if (f.dumpCount > 0) {
        return PipMessage("[^_^]", "You have ${f.dumpCount} thought${if (f.dumpCount == 1) "" else "s"} in your brain dump. Sort them when you're ready.", "Open it", PipAction.BrainDump, false)
    }
    val greetings = listOf(
        "Morning! Start with just two minutes.",
        "Small steps count. One thing at a time.",
        "I'm here if you need a nudge.",
    )
    return PipMessage("[^_^]", greetings[Math.floorMod(variant, greetings.size)], if (f.frog.isNotBlank()) "Start focus" else null,
        if (f.frog.isNotBlank()) PipAction.Focus else PipAction.None, false)
}
