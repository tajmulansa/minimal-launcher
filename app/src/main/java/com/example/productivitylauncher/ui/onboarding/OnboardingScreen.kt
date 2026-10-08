package com.example.productivitylauncher.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.ThemeMode

/** First run: meet Pip, pick gated apps, pick six Home apps, set the first frog, choose a theme. */
@Composable
fun OnboardingScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var frog by remember { mutableStateOf(state.frog) }
    val last = 5

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 8.dp), horizontalArrangement = Arrangement.Center) {
            for (i in 0..last) {
                Box(
                    Modifier.padding(horizontal = 4.dp).size(width = if (i == step) 22.dp else 8.dp, height = 8.dp)
                        .background(if (i == step) AppColors.text else AppColors.dot, RoundedCornerShape(4.dp)),
                )
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth()) {
            when (step) {
                0 -> Column(Modifier.weight(1f).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(120.dp).background(AppColors.card, CircleShape), contentAlignment = Alignment.Center) {
                        AppText("[^_^]", size = 28.sp, weight = FontWeight.Medium, mono = true)
                    }
                    AppText("Hi, I'm Pip.", size = 32.sp, weight = FontWeight.SemiBold, letterSpacing = (-1).sp, modifier = Modifier.padding(top = 28.dp))
                    AppText(
                        "This launcher helps you do one important thing a day. It keeps distracting apps calm and hard to open, and everything stays on this phone.",
                        size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.padding(top = 12.dp),
                    )
                }
                1 -> Column(Modifier.weight(1f)) {
                    AppText("Gate your distractions", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                    GatedPickerBody(state, Modifier.weight(1f))
                }
                2 -> Column(Modifier.weight(1f)) {
                    AppText("Pick your six", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp))
                    HomePickerBody(state, Modifier.weight(1f))
                }
                3 -> Column(Modifier.weight(1f).padding(28.dp), verticalArrangement = Arrangement.Center) {
                    AppText("What's your frog today?", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
                    AppText("The one task that matters most. Do it first and the day is already a win.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
                    AppField(frog, { frog = it }, "e.g. Finish chemistry chapter 4", fill = AppColors.card)
                }
                5 -> PermissionsStep(state, Modifier.weight(1f))
                else -> Column(Modifier.weight(1f).padding(28.dp), verticalArrangement = Arrangement.Center) {
                    AppText("Make it yours", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
                    AppText("Choose a look. You can change it any time in Settings.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, bottom = 20.dp))
                    Segmented(
                        listOf("Auto", "Light", "Dark"),
                        when (state.themeMode) { ThemeMode.Auto -> 0; ThemeMode.Light -> 1; ThemeMode.Dark -> 2 },
                        { state.setTheme(ThemeMode.entries[it]) },
                    )
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            CButton(
                if (step == last) "Start" else "Next",
                {
                    if (step == 3 && frog.isNotBlank()) state.updateFrog(frog.trim())
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
        if (granted) AppText("Allowed", size = 14.sp, weight = FontWeight.SemiBold, color = AppColors.focus)
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
        AppText("Your privacy first", size = 26.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp)
        AppText(
            "Everything stays on this phone. This app has no internet permission, no accounts, no ads and no tracking. Each permission below is optional and you can take it back any time in Android settings.",
            size = 15.sp, color = AppColors.muted, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp, bottom = 12.dp),
        )
        PermRow("Usage access", "Reads how long apps were used, on this phone only, to show screen time and your gated time.", usage) { openUsageAccessSettings(context) }
        PermRow("Floating timer", "Lets the small timer show over a gated app while your session runs.", overlay) { openOverlaySettings(context) }
        PermRow("Do Not Disturb", "Lets the moon button and focus sessions silence notifications.", dnd) { openDndSettings(context) }
        PermRow("Calendar (read only)", "Shows today's events in the Agenda widget. Nothing is copied or sent.", calendar) { calLauncher.launch(Manifest.permission.READ_CALENDAR) }
        PermRow("Home app", "Make this your launcher so it opens when you press Home.", home) { openDefaultLauncherSettings(context) }
        TextLink(if (showPolicy) "Hide privacy policy" else "Read the full privacy policy", { showPolicy = !showPolicy })
        if (showPolicy) {
            PRIVACY_SECTIONS.forEach { (title, body) ->
                AppText(title, size = 15.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
                AppText(body, size = 14.sp, color = AppColors.muted, lineHeight = 20.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }
        androidx.compose.foundation.layout.Spacer(Modifier.height(12.dp))
    }
}
