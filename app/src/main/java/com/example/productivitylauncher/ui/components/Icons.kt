package com.example.productivitylauncher.ui.components

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Line icons drawn from SVG path data (24x24, stroke 2), so no icon library is needed. */
enum class Ic(val path: String, val filled: Boolean = false) {
    Phone("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"),
    Message("M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"),
    Moon("M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"),
    Target("M12 2a10 10 0 1 0 0 20 10 10 0 0 0 0-20z M12 6a6 6 0 1 0 0 12 6 6 0 0 0 0-12z M12 10a2 2 0 1 0 0 4 2 2 0 0 0 0-4z"),
    Search("M11 3a8 8 0 1 0 0 16 8 8 0 0 0 0-16z M21 21l-4.35-4.35"),
    Plus("M12 5v14 M5 12h14"),
    Minus("M5 12h14"),
    Check("M20 6L9 17l-5-5"),
    Back("M19 12H5 M12 19l-7-7 7-7"),
    Chevron("M9 18l6-6-6-6"),
    Close("M18 6L6 18 M6 6l12 12"),
    Sliders("M4 21v-7 M4 10V3 M12 21v-9 M12 8V3 M20 21v-5 M20 12V3 M1 14h6 M9 8h6 M17 16h6"),
    Drop("M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z", filled = true),
    Play("M6 4l14 8-14 8z", filled = true),
    Pause("M6 4h4v16H6z M14 4h4v16h-4z", filled = true),
    Lock("M5 11h14v10H5z M7 11V7a5 5 0 0 1 10 0v4"),
    Trash("M3 6h18 M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6 M10 11v6 M14 11v6 M9 6V4a1 1 0 0 1 1-1h4a1 1 0 0 1 1 1v2"),
    Calendar("M3 4h18v18H3z M16 2v4 M8 2v4 M3 10h18"),
    Bulb("M9 21h6 M12 3a6 6 0 0 0-4 10.5V17h8v-3.5A6 6 0 0 0 12 3z"),
    Chart("M18 20V10 M12 20V4 M6 20v-6"),
    Shield("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"),
    Home("M3 11l9-8 9 8 M5 10v10h5v-6h4v6h5V10"),
    Grid("M4 4h6v6H4z M14 4h6v6h-6z M4 14h6v6H4z M14 14h6v6h-6z"),
    Gear("M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z M19.4 15a1.65 1.65 0 0 0 .33 1.82l.06.06a2 2 0 1 1-2.83 2.83l-.06-.06a1.65 1.65 0 0 0-1.82-.33 1.65 1.65 0 0 0-1 1.51V21a2 2 0 1 1-4 0v-.09a1.65 1.65 0 0 0-1-1.51 1.65 1.65 0 0 0-1.82.33l-.06.06a2 2 0 1 1-2.83-2.83l.06-.06a1.65 1.65 0 0 0 .33-1.82 1.65 1.65 0 0 0-1.51-1H3a2 2 0 1 1 0-4h.09a1.65 1.65 0 0 0 1.51-1 1.65 1.65 0 0 0-.33-1.82l-.06-.06a2 2 0 1 1 2.83-2.83l.06.06a1.65 1.65 0 0 0 1.82.33h0a1.65 1.65 0 0 0 1-1.51V3a2 2 0 1 1 4 0v.09a1.65 1.65 0 0 0 1 1.51h0a1.65 1.65 0 0 0 1.82-.33l.06-.06a2 2 0 1 1 2.83 2.83l-.06.06a1.65 1.65 0 0 0-.33 1.82v0a1.65 1.65 0 0 0 1.51 1H21a2 2 0 1 1 0 4h-.09a1.65 1.65 0 0 0-1.51 1z"),
}

private val cache = HashMap<Ic, ImageVector>()

private fun vectorOf(ic: Ic): ImageVector = cache.getOrPut(ic) {
    ImageVector.Builder(
        name = ic.name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).addPath(
        pathData = addPathNodes(ic.path),
        fill = if (ic.filled) SolidColor(Color.Black) else null,
        stroke = if (ic.filled) null else SolidColor(Color.Black),
        strokeLineWidth = 2f,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    ).build()
}

@Composable
fun AppIcon(ic: Ic, tint: Color, modifier: Modifier = Modifier, size: Dp = 24.dp, description: String? = null) {
    val v = remember(ic) { vectorOf(ic) }
    Icon(imageVector = v, contentDescription = description, tint = tint, modifier = modifier.then(Modifier.sizeCompat(size)))
}
