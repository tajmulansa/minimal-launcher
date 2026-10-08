package com.example.productivitylauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.GateSetting
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.canDrawOverlays
import com.example.productivitylauncher.data.formatMinutes
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.isDefaultLauncher
import com.example.productivitylauncher.data.openDefaultLauncherSettings
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openOverlaySettings
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Divider
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.components.SettingRow
import com.example.productivitylauncher.ui.components.SettingsGroup
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.Toggle
import com.example.productivitylauncher.ui.components.ValueChevron
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.ThemeMode

@Composable
fun SettingsScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }
    // Re-read permissions every time the user comes back from a system settings page.
    val tick = state.resumeTick
    val usage = remember(tick, state.usageAccess) { state.usageAccess }
    val overlay = remember(tick) { canDrawOverlays(context) }
    val dnd = remember(tick) { hasDndAccess(context) }
    val isHome = remember(tick) { isDefaultLauncher(context) }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Settings", onBack = nav.back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            SettingsGroup("Appearance") {
                SettingRow("Theme", Modifier.padding(bottom = 4.dp)) {}
                Segmented(
                    listOf("Auto", "Light", "Dark"),
                    when (state.themeMode) { ThemeMode.Auto -> 0; ThemeMode.Light -> 1; ThemeMode.Dark -> 2 },
                    { state.setTheme(ThemeMode.entries[it]) },
                    Modifier.padding(bottom = 16.dp),
                )
                Divider()
                SettingRow("Pip", subtitle = "The little [^_^] helper on your Home and Widgets pages") {
                    Toggle(state.pipOn, { state.setPipOn(it) }, "Show Pip")
                }
            }

            SettingsGroup("Gate") {
                GateStepper(state, GateSetting.Limit, "Daily gated limit", "Total time in gated apps per day", state.dailyLimitMin, 15, 15, 480, { formatMinutes(it.toLong()) })
                Divider()
                GateStepper(state, GateSetting.Breaths, "Breaths", "Slow breaths before a gated app", state.breaths, 1, 1, 10, { "$it" })
                Divider()
                GateStepper(state, GateSetting.PhraseWords, "Phrase length", "Words to type at the gate", state.phraseWords, 1, 2, 8, { "$it" })
                Divider()
                val growPending = state.pendingFor(GateSetting.GrowingWait)
                SettingRow(
                    "Longer wait each open",
                    subtitle = if (growPending != null) pendingText(growPending.applyAt) else "Adds a few seconds every time you open the same app in a day",
                ) {
                    Toggle(if (growPending != null) !state.growingWait else state.growingWait, { on ->
                        state.changeGateSetting(GateSetting.GrowingWait, if (on) 1 else 0)
                    }, "Longer wait each open")
                }
                Divider()
                AppText(
                    "Making the gate easier takes 24 hours to apply, so a weak moment can't undo your own rules. Making it stricter works at once.",
                    size = 13.sp, color = AppColors.muted, lineHeight = 19.sp, modifier = Modifier.padding(vertical = 14.dp),
                )
            }

            SettingsGroup("Focus and rhythm") {
                SettingRow("Focus length", subtitle = "Default session for your frog") {
                    Stepper("${state.frogMinutes} min", { state.setFrogMinutes(state.frogMinutes - 5) }, { state.setFrogMinutes(state.frogMinutes + 5) }, valueWidth = 70.dp)
                }
                Divider()
                SettingRow("Do Not Disturb in focus", subtitle = "Silence notifications during a session") {
                    Toggle(state.focusDnd, { state.setFocusDnd(it) }, "Do Not Disturb in focus")
                }
                Divider()
                SettingRow("Lock gated apps in focus") {
                    Toggle(state.focusLockGated, { state.setFocusLockGated(it) }, "Lock gated apps in focus")
                }
                Divider()
                SettingRow("Water goal", subtitle = "Glasses per day") {
                    Stepper("${state.waterGoal}", { state.setWaterGoal(state.waterGoal - 1) }, { state.setWaterGoal(state.waterGoal + 1) }, valueWidth = 40.dp)
                }
                Divider()
                SettingRow("Screen time goal", subtitle = "Shown in your weekly review") {
                    Stepper(formatMinutes(state.screenGoalMin.toLong()), { state.setScreenGoal(state.screenGoalMin - 30) }, { state.setScreenGoal(state.screenGoalMin + 30) }, valueWidth = 70.dp)
                }
                Divider()
                SettingRow("Evening shutdown", subtitle = "Pip reminds you to close the day") {
                    Stepper("${state.eveningHour}:00", { state.setEveningHour(state.eveningHour - 1) }, { state.setEveningHour(state.eveningHour + 1) }, valueWidth = 64.dp)
                }
                Divider()
                SettingRow("Close the day now", onClick = { nav.go(Route.Evening) }) { ValueChevron("") }
                Divider()
                SettingRow("Weekly review", onClick = { nav.go(Route.Week) }) { ValueChevron("") }
            }

            SettingsGroup("Apps") {
                SettingRow("Gated apps", subtitle = "Apps that get a gate", onClick = { nav.go(Route.GatedPicker) }) {
                    ValueChevron("${state.gated.size}", AppColors.gate)
                }
                Divider()
                SettingRow("Home apps", subtitle = "Up to six on your Home page", onClick = { nav.go(Route.HomePicker) }) {
                    ValueChevron("${state.homeApps.size} of 6")
                }
            }

            SettingsGroup("Permissions") {
                SettingRow("Usage access", subtitle = "Screen time and exact gated time", onClick = { openUsageAccessSettings(context) }) {
                    ValueChevron(if (usage) "Allowed" else "Allow", if (usage) AppColors.focus else AppColors.gate)
                }
                Divider()
                SettingRow("Floating timer", subtitle = "Shows time left over gated apps", onClick = { openOverlaySettings(context) }) {
                    ValueChevron(if (overlay) "Allowed" else "Allow", if (overlay) AppColors.focus else AppColors.gate)
                }
                Divider()
                SettingRow("Do Not Disturb access", subtitle = "For the Moon button and focus", onClick = { openDndSettings(context) }) {
                    ValueChevron(if (dnd) "Allowed" else "Allow", if (dnd) AppColors.focus else AppColors.gate)
                }
                Divider()
                SettingRow("Default home app", subtitle = "Make this your launcher", onClick = { openDefaultLauncherSettings(context) }) {
                    ValueChevron(if (isHome) "This app" else "Change", if (isHome) AppColors.focus else AppColors.muted)
                }
            }

            SettingsGroup("Data and privacy") {
                SettingRow("Stays on this phone", subtitle = "This app has no internet permission. Nothing is sent anywhere.") {}
                Divider()
                SettingRow("Delete all my data", titleColor = AppColors.gate, subtitle = "Resets settings, tasks, notes and gated apps", onClick = { confirmReset = true }) {}
            }

            SettingsGroup("About") {
                val version = remember { runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "" }
                SettingRow("Version", subtitle = "Open source, made for students") { AppText(version, size = 15.sp, color = AppColors.muted, weight = FontWeight.Medium) }
            }
            Spacer(Modifier.height(16.dp))
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            containerColor = AppColors.card,
            title = { AppText("Delete all data?", size = 20.sp, weight = FontWeight.SemiBold) },
            text = { AppText("This removes your frog, notes, habits, brain dump, gated apps and settings from this phone. It can't be undone.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp) },
            confirmButton = {
                TextButton(onClick = { state.resetAll(); confirmReset = false; nav.go(Route.Onboarding) }) {
                    AppText("Delete", color = AppColors.gate, weight = FontWeight.SemiBold)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { AppText("Cancel", weight = FontWeight.SemiBold) } },
        )
    }
}

private fun pendingText(applyAt: Long): String {
    val hours = ((applyAt - System.currentTimeMillis()) / 3_600_000L).coerceAtLeast(0) + 1
    return "Change applies in about ${hours}h"
}

@Composable
private fun GateStepper(
    state: LauncherState,
    setting: GateSetting,
    title: String,
    subtitle: String,
    current: Int,
    step: Int,
    min: Int,
    max: Int,
    format: (Int) -> String,
) {
    val pending = state.pendingFor(setting)
    val shown = pending?.value ?: current
    SettingRow(title, subtitle = if (pending != null) "${pendingText(pending.applyAt)}. Now: ${format(current)}" else subtitle) {
        Stepper(
            format(shown),
            { state.changeGateSetting(setting, (shown - step).coerceAtLeast(min)) },
            { state.changeGateSetting(setting, (shown + step).coerceAtMost(max)) },
            valueWidth = 64.dp,
        )
    }
}
