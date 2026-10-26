package com.example.productivitylauncher.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.isDndOn
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.service.Noise
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Look
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.ThemeMode

/**
 * Quick actions: the small switches people reach for every day, without opening Settings.
 * Pull the top of Home down (or tap the date) to open. Swipe up, tap outside or tap the handle to close.
 */
@Composable
fun QuickPanel(state: LauncherState, visible: Boolean, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrim by animateFloatAsState(if (visible) 0.32f else 0f, Motion.enter(300), label = "scrim")

    Box(modifier.fillMaxSize()) {
        if (visible || scrim > 0.001f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrim))
                    .then(if (visible) Modifier.clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss) else Modifier),
            )
        }
        AnimatedVisibility(
            visible = visible,
            modifier = Modifier.align(Alignment.TopCenter),
            enter = slideInVertically(Motion.morph(520)) { -it } + fadeIn(Motion.enter(260)),
            exit = slideOutVertically(Motion.exit(320)) { -it } + fadeOut(Motion.exit(220)),
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .background(AppColors.phone, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .border(1.dp, AppColors.line, RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp))
                    .pointerInput(Unit) {
                        var total = 0f
                        detectVerticalDragGestures(
                            onDragStart = { total = 0f },
                            onDragEnd = { if (total < -50f) onDismiss() },
                            onDragCancel = { total = 0f },
                            onVerticalDrag = { _, dy -> total += dy },
                        )
                    }
                    .padding(start = 28.dp, end = 28.dp, top = 22.dp, bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Caption("Quick actions")
                    Caption("Swipe up to close")
                }
                QuickRow(state)
                Rule()
                NoisePlayer()
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Caption("Theme")
                    Segmented(ThemeMode.entries.map { it.label }, state.themeMode.ordinal, { state.setTheme(ThemeMode.entries[it]) })
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Caption("Look")
                    Segmented(Look.entries.map { it.label }, state.look.ordinal, { state.updateLook(Look.entries[it]) })
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(34.dp)
                        .semantics { contentDescription = "Close quick actions" }
                        .clickableRole(onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.width(40.dp).height(4.dp).background(AppColors.dot, RoundedCornerShape(2.dp)))
                }
            }
        }
    }
}

/** The four everyday switches. Used in the quick panel and on the Today page. */
@Composable
fun QuickRow(state: LauncherState) {
    val context = LocalContext.current
    var dnd by remember { mutableStateOf(isDndOn(context)) }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        QuickToggle(Ic.Moon, "Silent", dnd) {
            if (setDnd(context, !dnd)) dnd = !dnd else openDndSettings(context)
        }
        QuickToggle(Ic.Lock, "Lock in", state.lockIn) { state.updateLockIn(!state.lockIn) }
        QuickToggle(Ic.Bolt, "Red button", state.urgeButtonOn) {
            val on = !state.urgeButtonOn
            state.updateUrgeButton(on)
            if (on && !canDrawOverlays(context)) openOverlaySettings(context)
        }
        QuickToggle(Ic.Play, "Animations", state.animations) { state.updateAnimations(!state.animations) }
    }
}

/** A round on/off button with a caption. Filled when on. */
@Composable
private fun QuickToggle(ic: Ic, label: String, on: Boolean, onClick: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val fill by animateColorAsState(if (on) AppColors.text else Color.Transparent, Motion.state(200), label = "qtFill")
    val tint by animateColorAsState(if (on) AppColors.phone else AppColors.text, Motion.state(200), label = "qtTint")
    Column(
        Modifier
            .width(72.dp)
            .semantics { role = Role.Switch; stateDescription = if (on) "On" else "Off"; contentDescription = label }
            .clickableRole({
                haptic.performHapticFeedback(if (on) HapticFeedbackType.ToggleOff else HapticFeedbackType.ToggleOn)
                onClick()
            }, Role.Switch),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            Modifier.size(58.dp).background(fill, CircleShape).border(1.dp, if (on) Color.Transparent else AppColors.line, CircleShape),
            contentAlignment = Alignment.Center,
        ) { AppIcon(ic, tint, size = 22.dp) }
        AppText(label.uppercase(), size = 10.sp, weight = FontWeight.Medium, color = if (on) AppColors.text else AppColors.muted, letterSpacing = 0.8.sp, mono = true, maxLines = 1)
    }
}

/** Previous, play/pause and next. It plays white, pink or brown noise made in code, even with the screen off. */
@Composable
private fun NoisePlayer() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Caption("Noise")
            AppText(Noise.kind.label, size = 17.sp, weight = FontWeight.Medium, display = false)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            PlayerButton(Ic.SkipPrev, "Previous noise", 44, false) { haptic.performHapticFeedback(HapticFeedbackType.ToggleOn); Noise.step(-1) }
            PlayerButton(if (Noise.playing) Ic.Pause else Ic.Play, if (Noise.playing) "Pause noise" else "Play noise", 56, true) { haptic.performHapticFeedback(HapticFeedbackType.ToggleOn); Noise.toggle(context) }
            PlayerButton(Ic.SkipNext, "Next noise", 44, false) { haptic.performHapticFeedback(HapticFeedbackType.ToggleOn); Noise.step(1) }
        }
    }
}

@Composable
private fun PlayerButton(ic: Ic, description: String, size: Int, filled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(size.dp)
            .background(if (filled) AppColors.text else Color.Transparent, CircleShape)
            .border(1.dp, if (filled) Color.Transparent else AppColors.line, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) { AppIcon(ic, if (filled) AppColors.phone else AppColors.text, size = if (filled) 22.dp else 18.dp) }
}
