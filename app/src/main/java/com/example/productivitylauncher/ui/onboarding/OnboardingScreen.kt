package com.example.productivitylauncher.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import com.example.productivitylauncher.ui.theme.Motion
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.productivitylauncher.data.UsageReader
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.hasCalendarPermission
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.ui.settings.PRIVACY_SECTIONS
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatTimeOfDay
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.data.isDefaultLauncher
import com.example.productivitylauncher.data.openDefaultLauncherSettings
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.apps.GatedPickerBody
import com.example.productivitylauncher.ui.apps.HomePickerBody
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.theme.Look
import com.example.productivitylauncher.ui.theme.ThemeMode

/** First run: welcome, pick gated apps, pick six Home apps, ask your name, choose a theme. */
@Composable
fun OnboardingScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(state.userName) }
    var wake by remember { mutableIntStateOf(state.wakeMin) }
    var bed by remember { mutableIntStateOf(state.bedMin) }
    val last = 6

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
            for (i in 0..last) {
                val w by animateDpAsState(if (i == step) 22.dp else 8.dp, Motion.press(), label = "dot")
                Box(
                    Modifier.padding(horizontal = 4.dp).size(width = w, height = 8.dp)
                        .background(if (i == step) AppColors.text else AppColors.dot, RoundedCornerShape(4.dp)),
                )
            }
        }
        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            transitionSpec = {
                val dir = if (targetState > initialState) 1 else -1
                (slideInHorizontally(Motion.enter(300)) { (it * 0.14f).toInt() * dir } + fadeIn(Motion.enter(260))) togetherWith
                    (slideOutHorizontally(Motion.enter(300)) { -(it * 0.14f).toInt() * dir } + fadeOut(Motion.state(140)))
            },
            label = "step",
        ) { shownStep ->
            when (shownStep) {
                0 -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(28.dp), verticalArrangement = Arrangement.Center) {
                    Title("A quieter phone for studying.")
                    Body("Still keeps what you need close and puts a short pause in front of what pulls you away.", Modifier.padding(top = 12.dp, bottom = 24.dp))
                    WelcomePoint("One list, in order", "Write your tasks once. Home always shows the next one.")
                    WelcomePoint("Distractions wait", "Social and game apps get a short pause, so you open them on purpose.")
                    WelcomePoint("Focus when you need it", "A focus timer, Lock In, calming noise and a red button for hard moments.")
                    WelcomePoint("Private by design", "No account, no ads, no internet. Everything stays on this phone.")
                }
                1 -> Column(Modifier.fillMaxSize()) {
                    Title("Pause your distractions.", Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                    GatedPickerBody(state, Modifier.weight(1f))
                }
                2 -> Column(Modifier.fillMaxSize()) {
                    Title("Pick your Home apps.", Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                    HomePickerBody(state, Modifier.weight(1f))
                }
                3 -> Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
                    Title("What should we call you?")
                    Body("Home greets you by name. It stays on this phone.", Modifier.padding(top = 8.dp, bottom = 24.dp))
                    AppField(name, { name = it }, "Your name", fill = AppColors.card)
                }
                4 -> Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
                    Title("Your day.")
                    Body("When do you wake up and go to bed? Home greets you to match, and your evening shutdown is set for an hour before bed.", Modifier.padding(top = 8.dp, bottom = 28.dp))
                    SleepRow("Wake up", formatTimeOfDay(wake), { wake = (wake - 15 + 1440) % 1440 }, { wake = (wake + 15) % 1440 })
                    SleepRow("Bedtime", formatTimeOfDay(bed), { bed = (bed - 15 + 1440) % 1440 }, { bed = (bed + 15) % 1440 })
                }
                6 -> PermissionsStep(state, Modifier.fillMaxSize())
                else -> Column(Modifier.fillMaxSize().padding(28.dp), verticalArrangement = Arrangement.Center) {
                    Title("Make it yours.")
                    Body("Pick a theme and a look. You can change both any time in Quick actions.", Modifier.padding(top = 8.dp, bottom = 24.dp))
                    Segmented(ThemeMode.entries.map { it.label }, state.themeMode.ordinal, { state.setTheme(ThemeMode.entries[it]) })
                    Spacer(Modifier.height(12.dp))
                    Segmented(Look.entries.map { it.label }, state.look.ordinal, { state.updateLook(Look.entries[it]) })
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CButton(
                if (step == last) "Start" else "Next",
                {
                    if (step == 3) state.updateUserName(name)
                    if (step == 4) { state.updateWake(wake); state.updateBed(bed); state.updateEveningHour(bed / 60 - 1) }
                    if (step == last) {
                        state.finishOnboarding()
                        nav.go(Route.Main)
                    } else step += 1
                },
                Modifier.fillMaxWidth(), BtnKind.Primary,
            )
            if (step > 0) TextLink("Back", { step -= 1 })
            else if (step == 0) TextLink("Skip setup", { state.finishOnboarding(); nav.go(Route.Main) })
        }
    }
}

@Composable
private fun Title(text: String, modifier: Modifier = Modifier) {
    AppText(text, modifier, size = 34.sp, weight = FontWeight.Medium)
}

@Composable
private fun Body(text: String, modifier: Modifier = Modifier) {
    AppText(text, modifier, size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, display = false)
}

@Composable
private fun SleepRow(label: String, value: String, onMinus: () -> Unit, onPlus: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        AppText(label, size = 17.sp, display = false)
        Stepper(value, onMinus, onPlus, valueWidth = 84.dp)
    }
}

@Composable
private fun WelcomePoint(title: String, body: String) {
    Column(Modifier.padding(bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        AppText(title, size = 17.sp, weight = FontWeight.Medium, display = false)
        AppText(body, size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, display = false)
    }
}

@Composable
private fun PermRow(title: String, why: String, granted: Boolean, onAllow: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(title, size = 16.sp, weight = FontWeight.SemiBold)
            AppText(why, size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
        }
        if (granted) AppText("Allowed", size = 15.sp, weight = FontWeight.SemiBold, color = AppColors.focus)
        else CButton("Allow", onAllow, kind = BtnKind.Accent, small = true)
    }
}

/** Last setup step: privacy promise first, then every permission the app can use, each optional. */
@Composable
private fun PermissionsStep(state: LauncherState, modifier: Modifier) {
    val context = LocalContext.current
    val tick = state.resumeTick
    var calendar by remember(tick) { mutableStateOf(hasCalendarPermission(context)) }
    val calLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { calendar = it }
    val usage = remember(tick, state.usageAccess) { UsageReader(context).hasAccess() }
    val overlay = remember(tick) { canDrawOverlays(context) }
    val dnd = remember(tick) { hasDndAccess(context) }
    val home = remember(tick) { isDefaultLauncher(context) }
    var showPolicy by remember { mutableStateOf(false) }

    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp)) {
        Title("Your privacy first.")
        AppText(
            "Everything stays on this phone. There is no internet permission, no account, no ads and no tracking. Every permission below is optional, and you can take it back any time in Android settings.",
            size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        PermRow("Usage access", "Reads how long apps were used, on this phone only, to show screen time and your gated time.", usage) { openUsageAccessSettings(context) }
        PermRow("Display over other apps", "Lets the small timer show over a gated app, and the optional red button show over every app.", overlay) { openOverlaySettings(context) }
        PermRow("Do Not Disturb", "Lets the moon button and focus sessions silence notifications.", dnd) { openDndSettings(context) }
        PermRow("Home app", "Make this your launcher so it opens when you press Home.", home) { openDefaultLauncherSettings(context) }
        TextLink(if (showPolicy) "Hide privacy policy" else "Read the full privacy policy", { showPolicy = !showPolicy })
        if (showPolicy) {
            PRIVACY_SECTIONS.forEach { (title, body) ->
                AppText(title, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                AppText(body, size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
    }
}
