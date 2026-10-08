package com.example.productivitylauncher.data

import java.util.Locale

/*
 * Pip understands short typed commands. This is plain keyword and pattern matching that runs on the phone:
 * no network, no AI model. Pure Kotlin so it is unit tested (see PipBrainTest).
 */

enum class PipGo { Settings, Evening, Week, BrainDump, Apps, Widgets }

sealed interface PipIntent {
    data class AddWater(val glasses: Int) : PipIntent
    data class SetFrog(val text: String) : PipIntent
    data class SetTomorrow(val text: String) : PipIntent
    data object FrogDone : PipIntent
    data object FrogUndo : PipIntent
    data class Focus(val minutes: Int?) : PipIntent
    data class AddDump(val text: String) : PipIntent
    data class AddNote(val text: String) : PipIntent
    data class Ask(val ask: PipAsk) : PipIntent
    data object ScreenTime : PipIntent
    data object WaterStatus : PipIntent
    data object FrogStatus : PipIntent
    data object TimeLeft : PipIntent
    data class Go(val dest: PipGo) : PipIntent
    data object Greeting : PipIntent
    data object Thanks : PipIntent
    data class Unknown(val text: String) : PipIntent
}

private val NUMBER_WORDS = mapOf(
    "a" to 1, "an" to 1, "one" to 1, "two" to 2, "couple" to 2, "three" to 3, "four" to 4,
    "five" to 5, "six" to 6, "seven" to 7, "eight" to 8, "ten" to 10,
)

private fun rx(p: String) = Regex(p, RegexOption.IGNORE_CASE)

private fun cleanup(raw: String): String {
    var s = raw.trim()
    s = s.replace(rx("^(?:hey|hi|hello|ok|okay)?[ ,]*pip\\b[ ,:!]*"), "")
    s = s.replace(rx("^(?:please|pls|can you|could you)\\s+"), "")
    return s.trim()
}

private fun numberIn(t: String): Int? {
    Regex("\\d+").find(t)?.let { return it.value.toIntOrNull() }
    for (w in t.split(Regex("[^a-z]+"))) NUMBER_WORDS[w]?.let { return it }
    return null
}

fun parsePipCommand(raw: String): PipIntent {
    val text = cleanup(raw).trim().trimEnd('.', '!')
    val t = text.lowercase(Locale.ROOT)
    if (t.isEmpty()) return PipIntent.Greeting

    if (t in setOf("undo", "not done", "undone", "i'm not done", "im not done", "unmark") || t.startsWith("undo ")) return PipIntent.FrogUndo
    if (rx("^(?:thanks|thank you|thx|ty)\\b").containsMatchIn(t)) return PipIntent.Thanks
    if (t.length <= 24 && rx("^(?:hi|hello|hey|yo|sup|good (?:morning|afternoon|evening))\\b").containsMatchIn(t)) return PipIntent.Greeting

    rx("^tomorrow(?:'s)?\\s*(?:frog\\s*(?:is|to|:|-)?|:|-)\\s*(.+)$").find(text)?.let {
        val v = it.groupValues[1].trim()
        if (v.isNotEmpty()) return PipIntent.SetTomorrow(v)
    }
    rx("^(?:set\\s+)?(?:my\\s+|today'?s\\s+|the\\s+)?frog\\s*(?:is|to|:|=|-)\\s*(.+)$").find(text)?.let {
        val v = it.groupValues[1].trim()
        if (v.lowercase(Locale.ROOT) in setOf("done", "finished", "complete", "completed", "eaten")) return PipIntent.FrogDone
        if (v.isNotEmpty()) return PipIntent.SetFrog(v)
    }
    rx("^set\\s+(.+?)\\s+as\\s+(?:my\\s+|today'?s\\s+)?frog$").find(text)?.let {
        return PipIntent.SetFrog(it.groupValues[1].trim())
    }

    if (t in setOf("done", "finished", "i'm done", "im done", "i did it", "completed") ||
        rx("\\b(?:ate|eaten|finished|completed|done with)\\b.*\\bfrog\\b|\\bfrog\\b.*\\b(?:done|eaten|finished|complete|completed)\\b").containsMatchIn(t)
    ) return PipIntent.FrogDone

    if (t.contains("water") || t.contains("glass") || t.contains("hydrat")) {
        if (rx("\\?|how (?:much|many)|status|so far").containsMatchIn(t)) return PipIntent.WaterStatus
        return PipIntent.AddWater((numberIn(t) ?: 1).coerceIn(1, 8))
    }

    if (rx("^(?:start\\s+|begin\\s+|let'?s\\s+|i want to\\s+)?(?:a\\s+)?(?:focus|pomodoro|deep work)\\b").containsMatchIn(t) ||
        (t.contains("focus") && rx("\\b(?:start|begin|let'?s|lets)\\b").containsMatchIn(t))
    ) {
        val minutes = when {
            t.contains("half hour") || t.contains("half an hour") -> 30
            t.contains("hour") && !Regex("\\d").containsMatchIn(t) -> 60
            else -> Regex("(\\d+)").find(t)?.value?.toIntOrNull()
        }
        return PipIntent.Focus(minutes?.coerceIn(5, 120))
    }

    rx("^note\\s*(?:to self)?\\s*(?::|-|that|to)?\\s*(.+)$").find(text)?.let {
        val v = it.groupValues[1].trim()
        if (v.isNotEmpty()) return PipIntent.AddNote(v)
    }
    rx("^(?:brain\\s*dump|dump|idea|remember(?: that)?|remind me(?: to)?|todo|to-do|jot down)\\s*(?:to|:|-)?\\s*(.+)$").find(text)?.let {
        val v = it.groupValues[1].trim()
        if (v.isNotEmpty()) return PipIntent.AddDump(v)
    }

    if (rx("what (?:should|do|can) i (?:do|work on)|what now|what next|what'?s next|i'?m bored|im bored|don'?t know what to do|help me (?:decide|start)").containsMatchIn(t)) return PipIntent.Ask(PipAsk.WhatNow)
    if (rx("how am i doing|how'?s it going|how did i do|\\bstats\\b|progress|summary|streak|how'?s my day").containsMatchIn(t)) return PipIntent.Ask(PipAsk.HowAmI)
    if (rx("screen ?time|phone usage|how long.*phone").containsMatchIn(t)) return PipIntent.ScreenTime
    if (rx("what'?s my frog|what is my frog|which frog|^(?:my )?frog\\??$").containsMatchIn(t)) return PipIntent.FrogStatus
    if (rx("time left|how long left|how much time (?:is )?left|^timer\\??$").containsMatchIn(t)) return PipIntent.TimeLeft

    if (rx("close the day|evening shutdown|wind down|shut ?down").containsMatchIn(t)) return PipIntent.Go(PipGo.Evening)
    rx("^(?:open|go to|show(?: me)?|take me to)\\s+(?:the\\s+|my\\s+)?(settings|week(?:ly review)?|weekly|evening|brain dump|dump|all apps|apps|widgets)").find(t)?.let {
        val d = it.groupValues[1]
        return PipIntent.Go(
            when {
                d.startsWith("setting") -> PipGo.Settings
                d.startsWith("week") -> PipGo.Week
                d.startsWith("evening") -> PipGo.Evening
                d.contains("dump") -> PipGo.BrainDump
                d.contains("app") -> PipGo.Apps
                else -> PipGo.Widgets
            },
        )
    }
    if (t == "settings") return PipIntent.Go(PipGo.Settings)

    return PipIntent.Unknown(text)
}

/** Habit words for matching "I read my book" to a habit like "read one page". Words under 4 letters are ignored. */
fun habitMatches(habitAction: String, sentence: String): Boolean {
    val a = habitAction.lowercase(Locale.ROOT).split(Regex("[^a-z]+")).filter { it.length >= 4 }
    val s = sentence.lowercase(Locale.ROOT).split(Regex("[^a-z]+")).filter { it.length >= 4 }.toSet()
    return a.any { w -> s.any { it.startsWith(w.take(4)) } }
}
