package com.example.productivitylauncher.ui.steady

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.text.format.DateFormat
import android.view.WindowManager
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Motion
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.delay

private fun Context.findActivity(): Activity? {
    var c: Context? = this
    while (c is ContextWrapper) { if (c is Activity) return c; c = c.baseContext }
    return null
}

/**
 * Steady mode: a quiet landscape flip clock for the desk. Two flat outlined cards (hours, minutes) with a hairline
 * across the middle, today's date in mono caps, and a column of round buttons: Phone, Messages, Exit.
 * The screen turns landscape and stays awake while it is open; both are put back on exit.
 */
@Composable
fun SteadyScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val activity = context.findActivity()
        val previous = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val controller = activity?.window?.let { w -> WindowInsetsControllerCompat(w, w.decorView) }
        controller?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller?.hide(WindowInsetsCompat.Type.systemBars())
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            controller?.show(WindowInsetsCompat.Type.systemBars())
            activity?.requestedOrientation = previous
        }
    }

    var now by remember { mutableStateOf(Calendar.getInstance()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Calendar.getInstance()
            delay(60_000L - (System.currentTimeMillis() % 60_000L) + 20L)
        }
    }
    val use24 = DateFormat.is24HourFormat(context)
    val hour24 = now.get(Calendar.HOUR_OF_DAY)
    val shownHour = if (use24) hour24 else (hour24 % 12).let { if (it == 0) 12 else it }
    val hh = "%02d".format(shownHour)
    val mm = "%02d".format(now.get(Calendar.MINUTE))
    val date = remember(now.get(Calendar.DAY_OF_YEAR)) {
        java.text.SimpleDateFormat("EEEE d MMMM", Locale.getDefault()).format(now.time)
    }
    val meridiem = if (use24) "" else if (hour24 < 12) " · AM" else " · PM"

    Box(Modifier.fillMaxSize().background(AppColors.phone).padding(horizontal = 28.dp, vertical = 20.dp)) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
                val gap = 16.dp
                val cardH = minOf(maxHeight * 0.66f, (maxWidth - gap) / 2 / 1.1f)
                Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                        FlipCard(hh, cardH)
                        FlipCard(mm, cardH)
                    }
                    Box(Modifier.height(18.dp))
                    Caption(date + meridiem)
                }
            }
            Column(Modifier.padding(start = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                RoundButton(Ic.Phone, "Phone") { openPhone(context) }
                RoundButton(Ic.Message, "Messages") { openMessages(context) }
                RoundButton(Ic.Close, "Exit Steady mode") { nav.back() }
            }
        }
    }
}

@Composable
private fun RoundButton(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .border(1.dp, AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 20.dp)
    }
}

/** One flat outlined card with a hairline across the middle. When [text] changes the card flips like a paper flap. */
@Composable
private fun FlipCard(text: String, height: Dp) {
    var shown by remember { mutableStateOf(text) }
    var previous by remember { mutableStateOf(text) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(text) {
        if (text == shown) return@LaunchedEffect
        previous = shown
        shown = text
        if (Motion.enabled) {
            progress.snapTo(0f)
            progress.animateTo(1f, Motion.slow(700))
        } else progress.snapTo(1f)
    }
    val width = height * 1.1f
    val fontSize = (height.value * 0.74f).sp
    val shape = RoundedCornerShape(18.dp)
    val p = progress.value
    Box(Modifier.size(width, height).border(1.dp, AppColors.line, shape).clipToBounds()) {
        // Static layers: the new top half and the old bottom half.
        Half(shown, height, fontSize, top = true, Modifier.align(Alignment.TopCenter))
        Half(previous.takeIf { p < 0.5f } ?: shown, height, fontSize, top = false, Modifier.align(Alignment.BottomCenter))
        // Old top flap folds down, then the new bottom flap lands.
        if (p < 0.5f) {
            Half(
                previous, height, fontSize, top = true,
                Modifier.align(Alignment.TopCenter).graphicsLayer {
                    rotationX = -90f * (p * 2f)
                    transformOrigin = TransformOrigin(0.5f, 1f)
                    cameraDistance = 14f * density
                },
            )
        } else if (p < 1f) {
            Half(
                shown, height, fontSize, top = false,
                Modifier.align(Alignment.BottomCenter).graphicsLayer {
                    rotationX = 90f * (1f - (p - 0.5f) * 2f)
                    transformOrigin = TransformOrigin(0.5f, 0f)
                    cameraDistance = 14f * density
                },
            )
        }
        Box(Modifier.align(Alignment.Center).fillMaxWidth().height(1.dp).background(AppColors.line))
    }
}

/** The top or bottom half of a card's digits, drawn on the card's own background so flaps hide what is behind them. */
@Composable
private fun Half(text: String, cardHeight: Dp, fontSize: androidx.compose.ui.unit.TextUnit, top: Boolean, modifier: Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .layout { measurable, c ->
                val full = cardHeight.roundToPx()
                val p = measurable.measure(Constraints.fixed(c.maxWidth, full))
                layout(c.maxWidth, full / 2) { p.place(0, if (top) 0 else -full / 2) }
            }
            .clipToBounds()
            .background(AppColors.phone),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            AppText(
                text, size = fontSize, weight = FontWeight.Light, align = TextAlign.Center,
                lineHeight = fontSize,
            )
        }
    }
}
