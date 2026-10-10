package com.example.productivitylauncher.data

/** One habit in the habit stack: "After <anchor>, I <action>". */
data class Habit(
    val id: String,
    val anchor: String,
    val action: String,
    val doneDays: Set<String> = emptySet(),
)

/** One ordinary task in the to-do list. The frog is kept separately: there is only one per day. */
data class Task(val id: String, val text: String, val done: Boolean = false)

/** One line in the brain dump. */
data class Dump(val id: String, val text: String, val createdAt: Long)

/** The widgets the launcher itself provides. Hosted app widgets use ids like "ext:12". */
enum class LauncherWidget(val id: String, val title: String, val subtitle: String, val letter: String) {
    Frog("frog", "To-do list", "Many tasks, one frog first", "T"),
    Water("water", "Water reminder", "Count glasses", "W"),
    Budget("budget", "Distraction budget", "Time left in gated apps", "D"),
    Agenda("agenda", "Agenda", "Today's calendar events", "A"),
    Habits("habits", "Habit stack", "After X, I do Y", "H"),
    Note("note", "Quick note", "Jot one thing", "N"),
    Dump("dump", "Brain dump", "Empty your head, sort later", "B");

    companion object {
        fun byId(id: String): LauncherWidget? = entries.firstOrNull { it.id == id }
    }
}

val DEFAULT_WIDGETS = listOf("frog", "water", "budget", "note", "dump")

/** Gate settings that can be made weaker. Weakening waits 24 hours. */
enum class GateSetting(val key: String) {
    Limit("limit"),
    Breaths("breaths"),
    PhraseWords("phrase_words"),
    GrowingWait("growing_wait"),
}

/** A weakening change that is waiting for its 24 hour delay to pass. */
data class PendingChange(val value: Int, val applyAt: Long)
