package com.example.productivitylauncher.ui.pip

import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.PipAsk
import com.example.productivitylauncher.data.PipGo
import com.example.productivitylauncher.data.PipIntent
import com.example.productivitylauncher.data.frogStreak
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.habitMatches
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.parsePipCommand
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route

private const val HELP = "I didn't quite get that. Try: drank 2 glasses, frog: finish chapter 4, focus 30, dump: buy milk, or how am I doing?"

/**
 * Runs something the user typed to Pip and returns Pip's reply.
 * Everything happens on the phone: parsePipCommand matches patterns, this changes the launcher's own data.
 */
fun runPipCommand(raw: String, state: LauncherState, nav: Nav, showWidgets: () -> Unit, close: () -> Unit): String {
    return when (val intent = parsePipCommand(raw)) {
        is PipIntent.AddWater -> {
            state.updateWater(state.water + intent.glasses)
            val goal = state.waterGoal
            if (state.water >= goal) "Logged. ${state.water} of $goal glasses, goal reached. Nice!" else "Logged. ${state.water} of $goal glasses today."
        }
        is PipIntent.SetFrog -> {
            if (state.frogDone) {
                state.updateTomorrowFrog(intent.text)
                "Today's frog is already eaten, so I saved \"${intent.text}\" as tomorrow's frog."
            } else {
                state.updateFrog(intent.text)
                "Frog set: \"${intent.text}\". Say \"focus 25\" when you're ready to start."
            }
        }
        is PipIntent.SetTomorrow -> {
            state.updateTomorrowFrog(intent.text)
            "Tomorrow's frog: \"${intent.text}\". I'll set it up for you."
        }
        PipIntent.FrogDone -> when {
            state.frog.isBlank() -> "You haven't set a frog yet. Tell me: frog: <your task>."
            state.frogDone -> "That frog is already eaten. Well done."
            else -> {
                state.setFrogDone(true)
                val n = frogStreak(state.eatenDays)
                if (n >= 2) "Frog eaten! $n days in a row." else "Frog eaten! The hardest thing is behind you."
            }
        }
        PipIntent.FrogUndo -> {
            if (state.frogDone) { state.setFrogDone(false); "Okay, I marked your frog as not done." } else "Nothing to undo."
        }
        is PipIntent.Focus -> {
            if (state.frog.isBlank()) {
                "Set a frog first, then we can focus on it. Tell me: frog: <your task>."
            } else {
                intent.minutes?.let { state.updateFrogMinutes(it) }
                close()
                nav.go(Route.Focus)
                "Focus set to ${state.frogMinutes} min. Press Start when you're ready."
            }
        }
        is PipIntent.AddDump -> {
            state.addDump(intent.text)
            "Saved to your brain dump (${state.dumps.size} total)." + if (raw.contains("remind", ignoreCase = true)) " I can't set alarms, so check it when you're ready." else ""
        }
        is PipIntent.AddNote -> {
            state.updateNote(if (state.note.isBlank()) intent.text else state.note.trimEnd() + "\n" + intent.text)
            "Added to your quick note."
        }
        is PipIntent.Ask -> state.pipAnswer(intent.ask)
        PipIntent.ScreenTime -> {
            val st = state.screenTimeMs
            if (st == null) "Allow usage access in Settings and I can tell you your screen time."
            else "Screen time today: ${formatMillis(st)}. Your goal is ${formatMillis(state.screenGoalMin * 60_000L)}."
        }
        PipIntent.WaterStatus -> "${state.water} of ${state.waterGoal} glasses today."
        PipIntent.FrogStatus -> when {
            state.frog.isBlank() -> "No frog yet. Tell me: frog: <your task>."
            state.frogDone -> "\"${state.frog}\" is eaten. Done for today."
            else -> "Your frog: \"${state.frog}\". Not done yet."
        }
        PipIntent.TimeLeft -> {
            val now = System.currentTimeMillis()
            when {
                state.focusActive -> "${formatClock(state.focusEnd - now)} left in your focus session."
                state.sessionActive -> "${formatClock(state.sessionEnd - now)} left in ${state.appByPackage(state.sessionPkg)?.label ?: "your session"}."
                else -> "No timer is running."
            }
        }
        is PipIntent.Go -> {
            close()
            when (intent.dest) {
                PipGo.Settings -> nav.go(Route.Settings)
                PipGo.Evening -> nav.go(Route.Evening)
                PipGo.Week -> nav.go(Route.Week)
                PipGo.BrainDump -> nav.go(Route.BrainDump)
                PipGo.Apps -> nav.go(Route.Apps)
                PipGo.Widgets -> showWidgets()
            }
            "On it."
        }
        PipIntent.Greeting -> "Hi! Tell me things like \"drank 2 glasses\" or \"frog: finish chapter 4\". Or ask what to do now."
        PipIntent.Thanks -> "Anytime."
        is PipIntent.Unknown -> {
            val today = dayKey()
            val habit = state.habits.firstOrNull { today !in it.doneDays && habitMatches(it.action, intent.text) }
            if (habit != null) {
                state.toggleHabit(habit.id)
                "Checked off: ${habit.action}. After ${habit.anchor}, nice."
            } else HELP
        }
    }
}
