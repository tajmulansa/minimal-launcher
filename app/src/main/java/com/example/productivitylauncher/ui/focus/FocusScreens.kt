package com.example.productivitylauncher.ui.focus

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.components.Caption
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.border
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.SettingRow
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.Toggle
import com.example.productivitylauncher.ui.components.Divider
import com.example.productivitylauncher.ui.theme.AppColors
import kotlinx.coroutines.delay

/** Focus session for the frog: a timer ring, Do Not Disturb, and gated apps locked. */
@Composable
fun FocusScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var completed by remember { mutableStateOf(false) }
    val active = state.focusActive

    LaunchedEffect(state.focusEnd) {
        while (state.focusEnd > System.currentTimeMillis()) {
            now = System.currentTimeMillis()
            delay(500)
        }
        now = System.currentTimeMillis()
        if (state.focusEnd > 0L) {
            // The timer ran out while this screen was open.
            state.stopFocus()
            if (state.focusDnd) setDnd(context, false)
            completed = true
        }
    }

    fun start() {
        if (state.focusDnd) {
            if (hasDndAccess(context)) setDnd(context, true)
        }
        state.startFocus(state.frogMinutes)
    }

    fun stopEarly() {
        state.stopFocus()
        if (state.focusDnd) setDnd(context, false)
    }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Focus", onBack = {
            nav.back()
        })
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            if (completed) {
                CompleteBlock(state, nav, onAgain = { completed = false })
                return@Column
            }
            val left = (state.focusEnd - now).coerceAtLeast(0)
            val total = if (active) state.focusTotalMs else state.frogMinutes * 60_000L
            val progress = if (active && total > 0) (left.toFloat() / total).coerceIn(0f, 1f) else 1f
            val ringTrack = AppColors.line
            val ringColor = AppColors.focus
            Box(Modifier.size(236.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.size(236.dp)) {
                    val w = 2.dp.toPx()
                    val inset = w / 2
                    val arcSize = Size(size.width - w, size.height - w)
                    drawArc(ringTrack, 0f, 360f, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(w))
                    drawArc(ringColor, -90f, 360f * progress, false, topLeft = Offset(inset, inset), size = arcSize, style = Stroke(w, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppText(if (active) formatClock(left) else "%02d:00".format(state.frogMinutes), size = 60.sp, weight = FontWeight.Light, letterSpacing = (-3).sp)
                    Caption(
                        if (state.frog.isBlank()) "No task set" else state.frog,
                        Modifier.padding(horizontal = 32.dp),
                    )
                }
            }
            if (!active) {
                Row(horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.CenterVertically) {
                    StepCircle("−5") { state.updateFrogMinutes(state.frogMinutes - 5) }
                    Box(
                        Modifier
                            .height(56.dp)
                            .widthIn(min = 132.dp)
                            .alpha(if (state.frog.isNotBlank()) 1f else 0.3f)
                            .background(AppColors.text, RadiusPill)
                            .then(if (state.frog.isNotBlank()) Modifier.clickableRole({ start() }) else Modifier),
                        contentAlignment = Alignment.Center,
                    ) { AppText("Start", size = 16.sp, weight = FontWeight.Medium, color = AppColors.phone, display = false) }
                    StepCircle("+5") { state.updateFrogMinutes(state.frogMinutes + 5) }
                }
                Column(Modifier.fillMaxWidth()) {
                    Rule()
                    SettingRow("Do Not Disturb", subtitle = if (hasDndAccess(context)) "Silence notifications while you focus" else "Needs permission, tap to allow", onClick = if (hasDndAccess(context)) null else ({ openDndSettings(context) })) {
                        Toggle(state.focusDnd, { state.updateFocusDnd(it) }, "Do Not Disturb during focus")
                    }
                    SettingRow("Lock gated apps", subtitle = "They stay closed until you're done") {
                        Toggle(state.focusLockGated, { state.updateFocusLockGated(it) }, "Lock gated apps during focus")
                    }
                }
            } else {
                AppText("[-_-] shh. Just this one thing.", size = 15.sp, color = AppColors.muted, mono = true)
                Box(
                    Modifier.height(56.dp).widthIn(min = 132.dp).border(1.dp, AppColors.line, RadiusPill).clickableRole({ stopEarly() }),
                    contentAlignment = Alignment.Center,
                ) { AppText("End early", size = 16.sp, weight = FontWeight.Medium, display = false, modifier = Modifier.padding(horizontal = 28.dp)) }
            }
        }
    }
}

@Composable
private fun StepCircle(text: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).border(1.dp, AppColors.line, CircleShape).clickableRole(onClick), contentAlignment = Alignment.Center) {
        AppText(text, size = 12.sp, mono = true)
    }
}

@Composable
private fun CompleteBlock(state: LauncherState, nav: Nav, onAgain: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Caption("Focus complete")
        AppText("Did you finish it?", size = 34.sp, weight = FontWeight.Medium)
        AppText("\"${state.frog}\"", size = 16.sp, color = AppColors.muted, lineHeight = 24.sp, display = false)
        Spacer(Modifier.height(16.dp))
        CButton("Yes, done", { state.setFrogDone(true); nav.back() }, Modifier.fillMaxWidth(), BtnKind.Primary, leading = Ic.Check)
        CButton("Not yet", { nav.back() }, Modifier.fillMaxWidth(), BtnKind.Soft)
        TextLink("Another session", onAgain, Modifier.align(Alignment.CenterHorizontally))
    }
}
