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

enum class PipAction { None, Focus, SetFrog, Water, Evening, BrainDump, GatedApps }

/** What a message is about. Pip learns per topic what you act on and what you wave away. */
enum class PipTopic { Focus, LongUse, Calendar, Evening, Late, Frog, Habit, GateChange, GateOpens, Budget, Water, Praise, Brain, Idle }

/** Things the user can ask Pip from the card. */
enum class PipAsk { WhatNow, HowAmI }

data class PipMessage(
    val face: String,
    val text: String,
    val primaryLabel: String?,
    val action: PipAction,
    val nudge: Boolean,
    val topic: PipTopic = PipTopic.Idle,
    val score: Int = 0,
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
    val frogStreak: Int = 0,
    val usualFinishHour: Int? = null,
    val nextEventTitle: String? = null,
    val nextEventInMin: Int? = null,
    val topGateApp: String? = null,
    val topGateOpens: Int = 0,
    val pendingRemovalApp: String? = null,
    val pendingRemovalHours: Int = 0,
    val screenTimeMin: Long? = null,
    val screenGoalMin: Int = 180,
    val backedOutToday: Int = 0,
    val habitsTotal: Int = 0,
    val habitsDone: Int = 0,
)

/** Consecutive days with an eaten frog, counting back from today (or yesterday if today is not done yet). */
fun frogStreak(eatenDays: Set<String>, now: Long = System.currentTimeMillis()): Int {
    var offset = if (dayKey(now) in eatenDays) 0 else 1
    var n = 0
    while (dayKey(now - offset * DAY_MS) in eatenDays) { n++; offset++ }
    return n
}

/** Median of the hours the frog was usually finished, or null until there are at least three. */
fun usualHour(hours: List<Int>): Int? =
    if (hours.size < 3) null else hours.sorted()[hours.size / 2]

/** How long a topic stays quiet after the user waves it away: longer each time. */
fun pipSnoozeMs(dismissCount: Int): Long = (60L + 30L * dismissCount.coerceIn(0, 6)) * 60_000L

private fun pick(options: List<String>, variant: Int): String = options[Math.floorMod(variant, options.size)]

private fun hourLabel(h: Int): String = when {
    h == 0 -> "12 AM"
    h < 12 -> "$h AM"
    h == 12 -> "12 PM"
    else -> "${h - 12} PM"
}

/** All messages Pip could say right now, each with a score. The best one that is not snoozed wins. */
fun pipCandidates(f: PipFacts, variant: Int = 0): List<PipMessage> {
    val out = ArrayList<PipMessage>()
    fun add(face: String, text: String, label: String?, action: PipAction, nudge: Boolean, topic: PipTopic, score: Int) {
        out.add(PipMessage(face, text, label, action, nudge, topic, score))
    }

    if (f.focusActive) {
        add("[-_-]", pick(listOf("Shh. I'm quiet while you focus. You've got this.", "Deep work mode. I'll be here when you're done."), variant), null, PipAction.None, false, PipTopic.Focus, 100)
        return out
    }
    if ((f.activeMin45 ?: 0) >= 40) {
        add("[o_o]", pick(listOf(
            "That was a long stretch on your phone. Stand up, look far away for a minute, then pick one thing.",
            "You've been on your phone almost nonstop. Stretch, drink some water, then come back with a plan.",
        ), variant), null, PipAction.None, true, PipTopic.LongUse, 95)
    }
    if (f.nextEventTitle != null && f.nextEventInMin != null && f.nextEventInMin in 0..30) {
        add("[^o^]", "\"${f.nextEventTitle}\" starts in ${f.nextEventInMin} min. Wrap up what you're doing and get ready.", null, PipAction.None, true, PipTopic.Calendar, 92)
    }
    if (f.hour >= f.eveningHour && !f.eveningDone) {
        add("[-_-]", pick(listOf(
            "The day is nearly over. Let's close it and pick tomorrow's frog.",
            "Time to wind down. Two minutes now makes tomorrow easier.",
        ), variant), "Close the day", PipAction.Evening, true, PipTopic.Evening, 90)
    }
    if (f.hour >= 23 || f.hour < 5) {
        add("[-_-]", pick(listOf(
            "It's late. Sleep beats scrolling every time. Put the phone down?",
            "Your brain needs rest to remember what you studied. Time to sleep.",
        ), variant), null, PipAction.None, true, PipTopic.Late, 88)
    }
    if (f.frog.isBlank()) {
        add("[^_^]", pick(listOf(
            "What's the one thing that matters most today? Set your frog.",
            "Pick one thing. Just one. Everything else is a bonus.",
        ), variant), "Set my frog", PipAction.SetFrog, true, PipTopic.Frog, 80)
    } else if (!f.frogDone) {
        if (f.hour >= 12) {
            val urgency = (70 + (f.hour - 12) * 2).coerceAtMost(85)
            add("[>_<]", pick(listOf(
                "Your frog is still waiting: \"${f.frog}\". Just two minutes to start?",
                "\"${f.frog}\" won't eat itself. Start with two minutes, I'll be quiet.",
            ), variant), "Start focus", PipAction.Focus, true, PipTopic.Frog, urgency)
        } else if (f.usualFinishHour != null && f.hour < f.usualFinishHour - 1) {
            add("[^_^]", "You usually finish your frog around ${hourLabel(f.usualFinishHour)}. Start earlier today and your evening is free.", "Start focus", PipAction.Focus, false, PipTopic.Frog, 48)
        } else {
            add("[^_^]", pick(listOf("Morning! Your frog: \"${f.frog}\". Start with just two minutes.", "Good time to eat the frog while your head is fresh."), variant), "Start focus", PipAction.Focus, false, PipTopic.Frog, 45)
        }
    }
    if (f.topGateApp != null && f.topGateOpens >= 3) {
        add("[o_o]", "That's gate number ${f.topGateOpens} for ${f.topGateApp} today. What are you hoping to find there?", null, PipAction.None, true, PipTopic.GateOpens, 58)
    }
    if (f.pendingRemovalApp != null) {
        add("[o_o]", "The gate on ${f.pendingRemovalApp} comes off in about ${f.pendingRemovalHours}h. You can still keep it.", "Keep the gate", PipAction.GatedApps, true, PipTopic.GateChange, 60)
    }
    if (f.gatedMin != null && f.limitMin > 0 && f.gatedMin * 100 >= f.limitMin * 80L) {
        add("[o_o]", "Your gated apps are almost used up for today. Save the rest for something you'll enjoy.", null, PipAction.None, true, PipTopic.Budget, 65)
    }
    if (f.screenTimeMin != null && f.screenGoalMin > 0 && f.screenTimeMin > f.screenGoalMin) {
        add("[o_o]", "You're past your screen time goal for today. One last thing, then put it away?", null, PipAction.None, true, PipTopic.Budget, 62)
    }
    val expectedWater = (f.waterGoal * (f.hour.coerceIn(8, 21) - 8) / 13)
    if (f.water + 2 <= expectedWater) {
        add("[^o^]", pick(listOf("Time for some water. A glass now keeps the headache away.", "Hydration check. Have a glass?"), variant), "Drank one", PipAction.Water, true, PipTopic.Water, 55)
    }
    if (f.habitsTotal > 0 && f.habitsDone < f.habitsTotal && f.hour >= 18) {
        add("[^_^]", "${f.habitsTotal - f.habitsDone} habit${if (f.habitsTotal - f.habitsDone == 1) "" else "s"} left today. Small ones count.", null, PipAction.None, false, PipTopic.Habit, 40)
    }
    if (f.frogDone) {
        if (f.frogStreak >= 2) {
            add("[^o^]", "Frog eaten, ${f.frogStreak} days in a row. That streak is worth protecting.", null, PipAction.None, false, PipTopic.Praise, 42)
        } else {
            add("[^o^]", pick(listOf("Frog eaten! Everything else today is a bonus.", "Done. The hardest thing is behind you."), variant), null, PipAction.None, false, PipTopic.Praise, 40)
        }
    } else if (f.backedOutToday > 0) {
        add("[^o^]", "You backed out of a gate ${f.backedOutToday} time${if (f.backedOutToday == 1) "" else "s"} today. Every one wins you time back.", null, PipAction.None, false, PipTopic.Praise, 38)
    }
    if (f.dumpCount > 0) {
        add("[^_^]", "You have ${f.dumpCount} thought${if (f.dumpCount == 1) "" else "s"} in your brain dump. Sort them when you're ready.", "Open it", PipAction.BrainDump, false, PipTopic.Brain, 35)
    }
    add("[^_^]", pick(listOf("I'm here if you need a nudge.", "Small steps count. One thing at a time.", "Ask me what to do next."), variant), if (f.frog.isNotBlank() && !f.frogDone) "Start focus" else null, if (f.frog.isNotBlank() && !f.frogDone) PipAction.Focus else PipAction.None, false, PipTopic.Idle, 10)
    return out
}

/**
 * Picks the best message. [snoozeUntil] holds topics the user waved away (topic name to time),
 * [dismissed] how many times each was waved away. Urgent topics (score 95 and up) always get through.
 */
fun pickPip(
    f: PipFacts,
    variant: Int = 0,
    snoozeUntil: Map<String, Long> = emptyMap(),
    dismissed: Map<String, Long> = emptyMap(),
    now: Long = System.currentTimeMillis(),
): PipMessage {
    val all = pipCandidates(f, variant)
    val usable = all.filter { it.score >= 95 || (snoozeUntil[it.topic.name] ?: 0L) <= now }
    val ranked = usable.ifEmpty { all.filter { it.topic == PipTopic.Idle } }
    return ranked.maxByOrNull { it.score - 12 * (dismissed[it.topic.name] ?: 0L).toInt().coerceAtMost(4) } ?: all.last()
}

fun pipMessage(f: PipFacts, variant: Int = 0): PipMessage = pickPip(f, variant)

/** "What should I do now?" in plain words, from the same facts. */
fun pipAnswerNow(f: PipFacts): String = when {
    f.focusActive -> "Keep going. Nothing else matters until the timer ends."
    f.frog.isBlank() -> "Set your frog first. One task, the one you'd rather avoid."
    !f.frogDone -> "Start a focus session on \"${f.frog}\". Even two minutes counts."
    f.hour >= f.eveningHour && !f.eveningDone -> "Close the day and pick tomorrow's frog."
    f.water + 2 <= (f.waterGoal * (f.hour.coerceIn(8, 21) - 8) / 13) -> "Drink a glass of water, then take a short break."
    f.dumpCount > 0 -> "Sort your brain dump. Turn one thought into tomorrow's frog."
    else -> "You're on track. Rest, read, or do something you enjoy."
}

/** "How am I doing?" as a short, honest summary. */
fun pipAnswerHow(f: PipFacts): String {
    val parts = ArrayList<String>()
    parts.add(if (f.frogDone) "Frog eaten today." else if (f.frog.isBlank()) "No frog set yet." else "Frog still waiting.")
    if (f.frogStreak > 0) parts.add("Streak: ${f.frogStreak} day${if (f.frogStreak == 1) "" else "s"}.")
    parts.add("Water: ${f.water} of ${f.waterGoal}.")
    if (f.screenTimeMin != null) parts.add("Screen time: ${formatMinutes(f.screenTimeMin)} of your ${formatMinutes(f.screenGoalMin.toLong())} goal.")
    if (f.backedOutToday > 0) parts.add("Backed out of ${f.backedOutToday} gate${if (f.backedOutToday == 1) "" else "s"}.")
    return parts.joinToString(" ")
}
