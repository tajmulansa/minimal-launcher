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

fun greetingFor(part: DayPart, tone: PipTone = PipTone.Friendly): String = when (tone) {
    PipTone.Friendly -> when (part) {
        DayPart.Morning -> "Good morning!"
        DayPart.Afternoon -> "Good afternoon!"
        DayPart.Evening -> "Good evening!"
        DayPart.Night -> "Hey, still up?"
    }
    PipTone.Hype -> when (part) {
        DayPart.Morning -> "Good morning, let's go!"
        DayPart.Afternoon -> "Good afternoon, keep the energy up!"
        DayPart.Evening -> "Good evening, you're still in this!"
        DayPart.Night -> "Hey, night owl!"
    }
    PipTone.Teasing -> when (part) {
        DayPart.Morning -> "Morning. Look who's up."
        DayPart.Afternoon -> "Afternoon. Still alive?"
        DayPart.Evening -> "Evening. The day is almost over, no pressure."
        DayPart.Night -> "Still up? Bold of you."
    }
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
    /** How Pip talks. Chosen in the Vibe. */
    val tone: PipTone = PipTone.Friendly,
)

private fun pick(options: List<String>, variant: Int): String = options[Math.floorMod(variant, options.size)]

private fun waterBehind(f: PipFacts): Boolean =
    f.water + 2 <= (f.waterGoal * (f.hour.coerceIn(8, 21) - 8) / 13)

fun pipMessage(f: PipFacts, variant: Int = 0): PipMessage {
    val part = dayPartOf(f.hour)
    val tone = f.tone
    val hello = greetingFor(part, tone)
    // One line per tone: friendly, hype, teasing.
    fun say(friendly: String, hype: String, teasing: String) = when (tone) {
        PipTone.Friendly -> friendly
        PipTone.Hype -> hype
        PipTone.Teasing -> teasing
    }
    fun sayAny(friendly: List<String>, hype: List<String>, teasing: List<String>) = pick(
        when (tone) { PipTone.Friendly -> friendly; PipTone.Hype -> hype; PipTone.Teasing -> teasing }, variant,
    )

    // 1. Focus: stay quiet and encourage.
    if (f.focusActive) {
        return PipMessage(
            "[-_-]",
            sayAny(
                listOf("Shh. I'm quiet while you focus. You've got this.", "Deep work. I'll be here when you're done."),
                listOf("Locked in. Go go go.", "Deep work mode. Crush it."),
                listOf("Shh. Phone down. I'll judge silently.", "Focus time. I'll behave, you behave."),
            ),
            null, PipAction.None, false,
        )
    }

    // 2. Just ate the frog: appreciate it, then look after yourself.
    if (f.frogJustDone) {
        return if (waterBehind(f) || f.water == 0) {
            PipMessage(
                "[^o^]",
                say(
                    "You ate the frog! I'm really proud of you. Reward yourself: drink a glass of water and stretch for a minute.",
                    "FROG EATEN! Huge. Go drink a glass of water and stretch, you earned it.",
                    "Oh wow, you actually ate the frog. Proud of you. Now drink some water, hydrate, legend.",
                ),
                "Drank one", PipAction.Water, true,
            )
        } else {
            PipMessage(
                "[^o^]",
                say(
                    "You ate the frog! Amazing work. You've earned a 5 minute break, stand up and stretch.",
                    "FROG EATEN! Massive. Take a 5 minute break, stand up and stretch.",
                    "Frog eaten. Okay, I'm impressed. Take 5 minutes, stand up, stretch like a cat.",
                ),
                null, PipAction.None, true,
            )
        }
    }

    // 3. Long phone stretch.
    if ((f.activeMin45 ?: 0) >= 40) {
        return PipMessage(
            "[o_o]",
            say(
                "You've been on your phone for a long stretch. Stand up, look far away for a minute, then pick one thing.",
                "Long scroll alert! Stand up, look far away for a minute, then smash one thing.",
                "That was a long scroll. Stand up, look at something far away, then pick one thing. Just one.",
            ),
            null, PipAction.None, true,
        )
    }

    // 4. Time to wind down, or time to sleep.
    if (f.hour >= f.eveningHour && !f.eveningDone && f.hour < 23) {
        return PipMessage(
            "[-_-]",
            say(
                "Good evening! The day is nearly over. Let's close it and pick tomorrow's frog.",
                "The day is almost done. Let's close it strong and pick tomorrow's frog.",
                "The day is almost over. Close it, pick tomorrow's frog, and go be a person.",
            ),
            "Close the day", PipAction.Evening, true,
        )
    }
    if (f.hour >= 23 || f.hour < 5) {
        return PipMessage(
            "[-_-]",
            say(
                "It's late. Sleep helps you remember what you studied. Put the phone down and rest well.",
                "It's late. Sleep is part of the plan, champions rest. Phone down.",
                "It's late. Your notes will still be there tomorrow. Sleep, it's literally free.",
            ),
            null, PipAction.None, true,
        )
    }

    // 5. The frog.
    if (f.frog.isBlank()) {
        return if (part == DayPart.Evening) {
            PipMessage(
                "[^_^]",
                say(
                    "$hello What will tomorrow's frog be? Pick it tonight and you can start fast.",
                    "$hello Pick tomorrow's frog tonight and start fast!",
                    "$hello Pick tomorrow's frog now so future you doesn't have to think.",
                ),
                "Close the day", PipAction.Evening, true,
            )
        } else {
            PipMessage(
                "[^_^]",
                say(
                    "$hello What's the one thing that matters most today? Set your frog.",
                    "$hello What's the big one today? Set your frog and go get it!",
                    "$hello What's the one thing you keep avoiding? That's your frog.",
                ),
                "Set my frog", PipAction.SetFrog, true,
            )
        }
    }
    if (!f.frogDone) {
        return when (part) {
            DayPart.Morning -> PipMessage(
                "[^_^]",
                say(
                    "$hello Your frog today: \"${f.frog}\". Start with just two minutes.",
                    "$hello Your frog: \"${f.frog}\". Start now, two minutes, let's go!",
                    "$hello Your frog \"${f.frog}\" is not going to eat itself. Just two minutes.",
                ),
                "Start focus", PipAction.Focus, true,
            )
            DayPart.Afternoon -> PipMessage(
                "[>_<]",
                say(
                    "$hello Your frog \"${f.frog}\" is still waiting. Two minutes to start?",
                    "$hello \"${f.frog}\" is waiting. Two minutes and you're rolling!",
                    "$hello \"${f.frog}\" is still sitting there. Two minutes? I'll wait.",
                ),
                "Start focus", PipAction.Focus, true,
            )
            else -> PipMessage(
                "[>_<]",
                say(
                    "$hello \"${f.frog}\" is still not done. A short focus session now, or tomorrow is fine too.",
                    "$hello \"${f.frog}\" isn't done yet. A short focus now and you win the day!",
                    "$hello \"${f.frog}\" still isn't done. Short focus now, or forgive yourself and try tomorrow.",
                ),
                "Start focus", PipAction.Focus, true,
            )
        }
    }

    // 6. Water.
    if (waterBehind(f) || (part == DayPart.Morning && f.water == 0)) {
        val text = if (part == DayPart.Morning && f.water == 0) {
            say(
                "$hello Start the day with a glass of water.",
                "$hello Glass of water first, then conquer.",
                "$hello Water first. Coffee is not a hydration plan.",
            )
        } else {
            say(
                "Time for some water. A glass now keeps the headache away.",
                "Water break! One glass and you're topped up.",
                "Your water bottle is feeling ignored. One glass?",
            )
        }
        return PipMessage("[^o^]", text, "Drank one", PipAction.Water, true)
    }

    // 7. Gated apps almost used up.
    if (f.gatedMin != null && f.limitMin > 0 && f.gatedMin * 100 >= f.limitMin * 80L) {
        return PipMessage(
            "[o_o]",
            say(
                "Your gated apps are almost used up for today. Save the rest for something you'll enjoy.",
                "Gated apps are almost used up. Save the rest, you've got goals!",
                "Your gated apps are nearly out of time today. Spend the last bit wisely.",
            ),
            null, PipAction.None, true,
        )
    }

    // 8. The frog is eaten: appreciate.
    if (f.frogDone) {
        val text = when (part) {
            DayPart.Morning -> say(
                "$hello Your frog is already eaten. What a great start.",
                "$hello Frog already eaten. What a start!",
                "$hello Frog already eaten? Show-off. Nice.",
            )
            DayPart.Afternoon -> say(
                "$hello You ate your frog today. Be proud of that, and enjoy the rest of your day.",
                "$hello Frog eaten today. Be proud, you crushed it.",
                "$hello You ate your frog today. I'm not saying I'm proud, but I'm proud.",
            )
            else -> say(
                "$hello You ate your frog today. Well done. Time to relax.",
                "$hello Frog eaten. Great day. Now relax, you earned it.",
                "$hello Frog eaten. You can relax now, you've earned it.",
            )
        }
        return PipMessage("[^o^]", text, null, PipAction.None, false)
    }

    if (f.dumpCount > 0) {
        val n = f.dumpCount
        val s = if (n == 1) "" else "s"
        return PipMessage(
            "[^_^]",
            say(
                "You have $n thought$s in your brain dump. Sort them when you're ready.",
                "You've got $n thought$s in your brain dump. Clear them out when you're ready!",
                "$n thought$s living in your brain dump. They'd like to be sorted.",
            ),
            "Open it", PipAction.BrainDump, false,
        )
    }

    return PipMessage(
        "[^_^]",
        "$hello " + sayAny(
            listOf("I'm here if you need a nudge.", "One small step is enough."),
            listOf("Ready when you are.", "One step. Then another."),
            listOf("Nothing to nag about. Suspicious.", "All quiet. I'm just here being cute."),
        ),
        null, PipAction.None, false,
    )
}
