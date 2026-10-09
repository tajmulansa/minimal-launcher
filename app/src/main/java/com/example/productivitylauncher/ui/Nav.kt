package com.example.productivitylauncher.ui

/** Every full screen the launcher can show. Home and Widgets are the two pages of [Route.Main]. */
sealed interface Route {
    data object Main : Route
    data object Apps : Route
    data class Gate(val pkg: String) : Route
    data object BrainDump : Route
    data object AddWidget : Route
    data object Focus : Route
    data object Evening : Route
    data object Week : Route
    data object Settings : Route
    data object Privacy : Route
    data object GatedPicker : Route
    data object HomePicker : Route
    data class Locked(val reason: LockReason, val pkg: String) : Route
    data class TimesUp(val pkg: String) : Route
    data object Onboarding : Route
}

enum class LockReason { DailyLimit, Focus }

/** Passed to screens so they can move around without knowing about each other. */
class Nav(val go: (Route) -> Unit, val back: () -> Unit)
