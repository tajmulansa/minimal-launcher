package com.example.productivitylauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.GatedTag
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Intro
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.enterStagger
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/** Home: a rolling clock, your next task, a few plain app names and the dock. Pull the top down for Quick actions. */
@Composable
fun HomeScreen(
    state: LauncherState,
    nav: Nav,
    onOpenApp: (AppEntry) -> Unit,
    page: Int,
    onOpenWidgets: () -> Unit,
    onOpenQuick: () -> Unit,
) {
    val context = LocalContext.current
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val dateLine = SimpleDateFormat("EEEE, d MMM", Locale.getDefault()).format(now)
    val cal = Calendar.getInstance().apply { time = now }
    val hour24 = cal.get(Calendar.HOUR_OF_DAY)
    val use24 = DateFormat.is24HourFormat(context)
    val shownHour = if (use24) hour24 else (hour24 % 12).let { if (it == 0) 12 else it }
    val minute = cal.get(Calendar.MINUTE)
    val greeting = when (hour24) {
        in 5..11 -> "Good morning."
        in 12..16 -> "Good afternoon."
        in 17..21 -> "Good evening."
        else -> "Rest well."
    }

    Column(Modifier.fillMaxSize()) {
        // Top row. Tap the date or pull this area down to open Quick actions.
        Column(
            Modifier.pointerInput(Unit) {
                var total = 0f
                detectVerticalDragGestures(
                    onDragStart = { total = 0f },
                    onDragEnd = { if (total > 70f) onOpenQuick() },
                    onDragCancel = { total = 0f },
                    onVerticalDrag = { _, dy -> total += dy },
                )
            },
        ) {
            Row(
                Modifier.fillMaxWidth().padding(start = 28.dp, end = 20.dp, top = 12.dp).enterStagger(0, once = true),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier.semantics { contentDescription = "Quick actions" }.clickableRole(onOpenQuick).padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Caption(dateLine)
                    AppIcon(Ic.Chevron, AppColors.muted, size = 12.dp, modifier = Modifier.graphicsLayer { rotationZ = 90f })
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val st = state.screenTimeMs
                    Caption(
                        if (st != null) formatMillis(st) else "Usage off",
                        Modifier.clickableRole({ if (state.usageAccess) nav.go(Route.Week) else nav.go(Route.Settings) }).padding(vertical = 8.dp),
                    )
                    Box(
                        Modifier
                            .size(40.dp)
                            .border(1.dp, AppColors.line, CircleShape)
                            .semantics { contentDescription = "Settings" }
                            .clickableRole({ nav.go(Route.Settings) }),
                        contentAlignment = Alignment.Center,
                    ) { AppIcon(Ic.Sliders, AppColors.text, size = 18.dp) }
                }
            }

            // The clock.
            Column(Modifier.padding(start = 28.dp, end = 28.dp, top = 22.dp).enterStagger(1, once = true)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hs = shownHour.toString().padStart(2, '0')
                    Odometer(hs[0].digitToInt(), 0, 92.sp)
                    Odometer(hs[1].digitToInt(), 90, 92.sp)
                    Colon(92.sp)
                    Odometer(minute / 10, 180, 92.sp)
                    Odometer(minute % 10, 270, 92.sp)
                }
                AppText(greeting, Modifier.padding(top = 6.dp), size = 20.sp, color = AppColors.muted, letterSpacing = (-0.4).sp)
            }
        }

        Hair(0, Modifier.padding(horizontal = 28.dp, vertical = 20.dp))

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
        ) {
            // Next task.
            val frog = state.frog
            Row(
                Modifier.fillMaxWidth().enterStagger(2, once = true),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (frog.isNotBlank()) {
                    val done = state.frogDone
                    Box(Modifier.semantics { contentDescription = if (done) "Mark not done" else "Mark done" }.clickableRole({ state.setFrogDone(!done) })) {
                        CheckCircle(done, size = 30.dp)
                    }
                    Column(Modifier.weight(1f)) {
                        Caption("Next task")
                        AppText(
                            frog, Modifier.padding(top = 2.dp), size = 18.sp, weight = FontWeight.Medium, letterSpacing = (-0.3).sp,
                            color = if (done) AppColors.muted else AppColors.text, maxLines = 2,
                        )
                    }
                } else {
                    Column(Modifier.weight(1f).clickableRole(onOpenWidgets).padding(vertical = 4.dp)) {
                        Caption("Next task")
                        AppText("Set it on your Widgets page", Modifier.padding(top = 2.dp), size = 18.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = (-0.3).sp)
                    }
                    AppIcon(Ic.Chevron, AppColors.muted, size = 18.dp)
                }
            }

            Hair(1, Modifier.padding(vertical = 20.dp))

            val shown = state.homeApps.filter { !state.isGated(it) }.mapNotNull { state.appByPackage(it) }
            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Caption(if (state.lockIn) "Locked in · only your home apps" else "Home apps")
                Caption("${shown.size}")
            }
            shown.forEachIndexed { i, app ->
                Box(Modifier.enterStagger(3 + i, once = true)) {
                    AppRow(app, gated = state.isGated(app.packageName), onClick = { onOpenApp(app) })
                }
            }
            LaunchedEffect(Unit) { delay(900); Intro.played = true }
            if (shown.size < 6 && !state.lockIn) {
                Row(
                    Modifier.fillMaxWidth().clickableRole({ nav.go(Route.HomePicker) }).padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AppIcon(Ic.Plus, AppColors.muted, size = 18.dp)
                    AppText(if (shown.isEmpty()) "Choose your six apps" else "Add an app", size = 18.sp, weight = FontWeight.Medium, color = AppColors.muted)
                }
            }
        }

        Dots(page)
        Dock(
            onApps = { nav.go(Route.Apps) },
            onPhone = { openPhone(context) },
            onMessages = { openMessages(context) },
            onFocus = { nav.go(Route.Focus) },
        )
    }
}

/** One digit that rolls up a strip of 0..9 to its value. With animations off it simply changes. */
@Composable
private fun Odometer(digit: Int, delayMs: Int, size: TextUnit) {
    val density = LocalDensity.current
    val cell = with(density) { size.toDp() } * 1.05f
    var target by remember { mutableStateOf(if (Intro.played) digit else 0) }
    LaunchedEffect(digit) { target = digit }
    val y by animateFloatAsState(
        target.toFloat(),
        if (Motion.enabled) tween<Float>(900, delayMillis = if (Intro.played) 0 else delayMs, easing = Motion.Soft) else snap(),
        label = "digit",
    )
    Box(Modifier.width(with(density) { size.toDp() } * 0.6f).height(cell).clipToBounds()) {
        Column(
            Modifier
                .wrapContentHeight(Alignment.Top, unbounded = true)
                .graphicsLayer { translationY = -y * cell.toPx() },
        ) {
            for (d in 0..9) {
                Box(Modifier.height(cell), contentAlignment = Alignment.Center) {
                    AppText(d.toString(), size = size, weight = FontWeight.Light, letterSpacing = (-4).sp)
                }
            }
        }
    }
}

@Composable
private fun Colon(size: TextUnit) {
    val alpha = if (Motion.enabled) {
        val t = rememberInfiniteTransition(label = "colon")
        val a by t.animateFloat(
            1f, 0.25f,
            infiniteRepeatable(keyframes { durationMillis = 2000; 1f at 0; 1f at 900; 0.25f at 1000; 0.25f at 1900 }, RepeatMode.Restart),
            label = "colonAlpha",
        )
        a
    } else 1f
    Box(Modifier.width(with(LocalDensity.current) { size.toDp() } * 0.3f).graphicsLayer { this.alpha = alpha }, contentAlignment = Alignment.Center) {
        AppText(":", size = size, weight = FontWeight.Light, color = AppColors.muted)
    }
}

/** A hairline that draws itself in from the left. */
@Composable
fun Hair(index: Int, modifier: Modifier = Modifier) {
    var shown by remember { mutableStateOf(Intro.played || !Motion.enabled) }
    LaunchedEffect(Unit) { shown = true }
    val p by animateFloatAsState(
        if (shown) 1f else 0f,
        if (Motion.enabled) tween<Float>(1000, delayMillis = if (Intro.played) 0 else 200 + index * 120, easing = Motion.Soft) else snap(),
        label = "hair",
    )
    Box(
        modifier
            .fillMaxWidth()
            .height(1.dp)
            .graphicsLayer { scaleX = p; transformOrigin = TransformOrigin(0f, 0.5f) }
            .background(AppColors.line),
    )
}

@Composable
private fun AppRow(app: AppEntry, gated: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickableRole(onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AppText(
            app.label, Modifier.weight(1f, fill = false), size = 30.sp, weight = FontWeight.Medium, letterSpacing = (-1).sp,
            color = if (gated) AppColors.gate else AppColors.text, maxLines = 1,
        )
        if (gated) GatedTag()
    }
}

@Composable
fun Dots(page: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0..1) {
            val on = i == page
            val w by animateDpAsState(if (on) 20.dp else 6.dp, Motion.press(), label = "dot")
            val dotColor by animateColorAsState(if (on) AppColors.text else AppColors.dot, Motion.state(), label = "dotColor")
            Box(
                Modifier
                    .padding(horizontal = 4.dp)
                    .size(width = w, height = 6.dp)
                    .background(dotColor, RoundedCornerShape(3.dp)),
            )
        }
    }
}

@Composable
private fun Dock(onApps: () -> Unit, onPhone: () -> Unit, onMessages: () -> Unit, onFocus: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(50.dp)
                .background(AppColors.text, RadiusPill)
                .clickableRole(onApps),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            AppIcon(Ic.Search, AppColors.phone, size = 18.dp)
            AppText("All apps", Modifier.padding(start = 10.dp), size = 16.sp, weight = FontWeight.Medium, color = AppColors.phone)
        }
        DockButton(Ic.Phone, "Phone", onPhone)
        DockButton(Ic.Message, "Messages", onMessages)
        DockButton(Ic.Target, "Focus", onFocus)
    }
}

@Composable
private fun DockButton(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(50.dp)
            .border(1.dp, AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 22.dp)
    }
}
