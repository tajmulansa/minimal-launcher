package com.example.productivitylauncher.data

import com.example.productivitylauncher.ui.theme.ThemeMode

// A Vibe is a whole personality for the launcher, not only colours: the palette, an accent,
// the clock face, how the Home apps are laid out, and how Pip talks. Every part can be changed.
// A Vibe never touches the gate rules, so switching Vibe cannot weaken a gate.

enum class ClockFace(val label: String) {
    Classic("Classic"),
    Minimal("Minimal"),
    Numbers("Numbers"),
    Digital("Digital"),
}

enum class HomeLayout(val label: String) {
    Cards("Cards"),
    Text("Text"),
    Grid("Grid"),
}

enum class PipTone(val label: String, val sample: String) {
    Friendly("Friendly", "Your frog is still waiting. Two minutes to start?"),
    Hype("Hype", "Your frog is waiting. Two minutes and you're rolling!"),
    Teasing("Teasing", "Your frog is still sitting there. Two minutes? I'll wait."),
}

/** An accent colour that replaces the palette's main colour. Null means "use the palette's own". */
enum class Accent(val label: String, val light: Long?, val dark: Long?) {
    Default("Default", null, null),
    Blue("Blue", 0xFF2F5DD4, 0xFF8FB0FF),
    Teal("Teal", 0xFF1F8A8A, 0xFF7DD3D3),
    Violet("Violet", 0xFF6B4BC8, 0xFFB7A0FF),
    Rose("Rose", 0xFFC2456E, 0xFFFF9DBA),
    Amber("Amber", 0xFFB7791F, 0xFFF2C46B),
    Green("Green", 0xFF2E8B57, 0xFF86D9A8),
}

/** Everything a Vibe sets. */
data class Look(
    val theme: ThemeMode,
    val accent: Accent,
    val clock: ClockFace,
    val layout: HomeLayout,
    val tone: PipTone,
)

data class Vibe(
    val id: String,
    val name: String,
    val tagline: String,
    val look: Look,
    val custom: Boolean = false,
)

val DEFAULT_LOOK = Look(ThemeMode.Auto, Accent.Default, ClockFace.Classic, HomeLayout.Cards, PipTone.Friendly)

val PRESET_VIBES = listOf(
    Vibe("classic", "Classic", "Calm and clean.", DEFAULT_LOOK),
    Vibe("lockin", "Lock In", "Exam mode. No fluff.", Look(ThemeMode.Midnight, Accent.Default, ClockFace.Digital, HomeLayout.Text, PipTone.Hype)),
    Vibe("softstudy", "Soft Study", "A notebook and a blue pen.", Look(ThemeMode.PenBlue, Accent.Default, ClockFace.Numbers, HomeLayout.Cards, PipTone.Friendly)),
    Vibe("academia", "Dark Academia", "Candlelight and old books.", Look(ThemeMode.Academia, Accent.Default, ClockFace.Numbers, HomeLayout.Text, PipTone.Friendly)),
    Vibe("nightowl", "Night Owl", "Late nights, light roast.", Look(ThemeMode.Dark, Accent.Violet, ClockFace.Minimal, HomeLayout.Grid, PipTone.Teasing)),
)
