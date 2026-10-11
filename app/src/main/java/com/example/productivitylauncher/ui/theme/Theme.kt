package com.example.productivitylauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.example.productivitylauncher.R

/** The Ceramic palette (see docs/DESIGN.md). Screens read colours only through [AppColors]. */
class Palette(
    val phone: Color,
    val card: Color,
    val text: Color,
    val muted: Color,
    val focus: Color,
    val onFocus: Color,
    val gate: Color,
    val gateSoft: Color,
    val onGate: Color,
    val line: Color,
    val dot: Color,
    val primary: Color,
    val onPrimary: Color,
    val dark: Boolean,
)

val LightPalette = Palette(
    phone = Color(0xFFF2F0E9),
    card = Color(0xFFFFFFFF),
    text = Color(0xFF363431),
    muted = Color(0xFF6F6C66),
    focus = Color(0xFF4F7165),
    onFocus = Color(0xFFFFFFFF),
    gate = Color(0xFFB85C4E),
    gateSoft = Color(0xFFFAECEB),
    onGate = Color(0xFFFFFFFF),
    line = Color(0xFFEAE8E3),
    dot = Color(0xFFD6D3CD),
    primary = Color(0xFF363431),
    onPrimary = Color(0xFFFFFFFF),
    dark = false,
)

val DarkPalette = Palette(
    phone = Color(0xFF100F0E),
    card = Color(0xFF1B1A18),
    text = Color(0xFFEDEAE3),
    muted = Color(0xFFA09B92),
    focus = Color(0xFF8DB8A7),
    onFocus = Color(0xFF12201B),
    gate = Color(0xFFE58E7E),
    gateSoft = Color(0xFF3A2723),
    onGate = Color(0xFF2A1511),
    line = Color(0xFF292724),
    dot = Color(0xFF403C37),
    primary = Color(0xFFEDEAE3),
    onPrimary = Color(0xFF100F0E),
    dark = true,
)

/** A pale blue notebook page, written on with a blue pen. */
val PenBluePalette = Palette(
    phone = Color(0xFFE9F0FB),
    card = Color(0xFFF7FAFF),
    text = Color(0xFF14284F),
    muted = Color(0xFF51648C),
    focus = Color(0xFF1F4FD1),
    onFocus = Color(0xFFFFFFFF),
    gate = Color(0xFFB8452F),
    gateSoft = Color(0xFFFBE9E4),
    onGate = Color(0xFFFFFFFF),
    line = Color(0xFFD3DEF2),
    dot = Color(0xFFB5C5E4),
    primary = Color(0xFF14284F),
    onPrimary = Color(0xFFFFFFFF),
    dark = false,
)

/** Dark navy, like ink at night. */
val MidnightPalette = Palette(
    phone = Color(0xFF0B1020),
    card = Color(0xFF141B30),
    text = Color(0xFFE6EAF5),
    muted = Color(0xFF8F9ABB),
    focus = Color(0xFF7FA2FF),
    onFocus = Color(0xFF0B1530),
    gate = Color(0xFFFF8C7A),
    gateSoft = Color(0xFF3A2025),
    onGate = Color(0xFF2A1511),
    line = Color(0xFF222B45),
    dot = Color(0xFF34406A),
    primary = Color(0xFFE6EAF5),
    onPrimary = Color(0xFF0B1020),
    dark = true,
)

val LocalPalette = staticCompositionLocalOf { LightPalette }

/** Short accessors, e.g. AppColors.text. */
object AppColors {
    val phone: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.phone
    val card: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.card
    val text: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.text
    val muted: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.muted
    val focus: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.focus
    val onFocus: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onFocus
    val gate: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gate
    val gateSoft: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.gateSoft
    val onGate: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onGate
    val line: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.line
    val dot: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.dot
    val primary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.primary
    val onPrimary: Color @Composable @ReadOnlyComposable get() = LocalPalette.current.onPrimary
}

/** One typeface only: Inter, bundled in res/font (no download, no network). */
val Inter = FontFamily(
    Font(R.font.inter_regular, FontWeight.Normal),
    Font(R.font.inter_medium, FontWeight.Medium),
    Font(R.font.inter_semibold, FontWeight.SemiBold),
    Font(R.font.inter_bold, FontWeight.Bold),
)

/** Theme choice saved in settings. Auto follows the phone's light or dark setting. */
enum class ThemeMode(val label: String) {
    Auto("Auto"),
    Light("Light"),
    Dark("Dark"),
    PenBlue("Pen blue"),
    Midnight("Midnight"),
}

/** The palette for a theme, or null for Auto (which depends on the phone). */
fun paletteOf(mode: ThemeMode): Palette? = when (mode) {
    ThemeMode.Auto -> null
    ThemeMode.Light -> LightPalette
    ThemeMode.Dark -> DarkPalette
    ThemeMode.PenBlue -> PenBluePalette
    ThemeMode.Midnight -> MidnightPalette
}

@Composable
fun resolvePalette(mode: ThemeMode): Palette =
    paletteOf(mode) ?: if (isSystemInDarkTheme()) DarkPalette else LightPalette

@Composable
fun LauncherTheme(palette: Palette, content: @Composable () -> Unit) {
    val p = palette
    val scheme = if (p.dark) {
        darkColorScheme(primary = p.primary, background = p.phone, surface = p.phone, onBackground = p.text, onSurface = p.text)
    } else {
        lightColorScheme(primary = p.primary, background = p.phone, surface = p.phone, onBackground = p.text, onSurface = p.text)
    }
    CompositionLocalProvider(LocalPalette provides p) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
