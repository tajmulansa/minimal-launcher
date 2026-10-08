package com.example.productivitylauncher.ui.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.DAY_MS
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.RadiusLg
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
private fun StepCard(number: Int, title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().shadow(6.dp, RadiusLg, ambientColor = Color(0x14000000), spotColor = Color(0x14000000)).background(AppColors.card, RadiusLg).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(22.dp).background(AppColors.phone, CircleShape), contentAlignment = Alignment.Center) {
                AppText("$number", size = 11.sp, weight = FontWeight.Bold)
            }
            AppText(title.uppercase(), size = 12.sp, weight = FontWeight.Bold, color = AppColors.muted, letterSpacing = 1.5.sp)
        }
        content()
    }
}

/** Evening shutdown: review the day, clear your head, choose tomorrow's frog. */
@Composable
fun EveningScreen(state: LauncherState, nav: Nav) {
    var tomorrow by remember { mutableStateOf(state.tomorrowFrog) }
    var dump by remember { mutableStateOf("") }
    var done by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Evening", onBack = nav.back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (done) {
                Spacer(Modifier.height(40.dp))
                AppText("Day closed", size = 32.sp, weight = FontWeight.SemiBold, letterSpacing = (-1).sp, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                AppText("Your mind is clear and tomorrow's frog is ready.", size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp, modifier = Modifier.fillMaxWidth())
                AppText("[-_-] phone down. sleep well.", size = 14.sp, color = AppColors.muted, mono = true, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                CButton("Back to Home", { nav.back() }, Modifier.fillMaxWidth().padding(top = 16.dp), BtnKind.Primary)
                return@Column
            }
            StepCard(1, "How did today go?") {
                if (state.frog.isBlank()) {
                    AppText("You didn't set a frog today. That's okay, tomorrow is a fresh start.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp)
                } else {
                    Row(Modifier.fillMaxWidth().clickableRole({ state.setFrogDone(!state.frogDone) }), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        CheckCircle(state.frogDone)
                        AppText(state.frog, Modifier.weight(1f), size = 18.sp, weight = FontWeight.SemiBold, strike = state.frogDone, color = if (state.frogDone) AppColors.muted else AppColors.text)
                    }
                    AppText(if (state.frogDone) "Frog eaten. Well done." else "Not finished? Leave tomorrow's frog empty and it carries over.", size = 14.sp, color = AppColors.muted)
                }
            }
            StepCard(2, "Empty your head") {
                AppField(dump, { dump = it }, "Anything still on your mind?", onDone = { state.addDump(dump); dump = "" })
                if (state.dumps.isNotEmpty()) AppText("${state.dumps.size} thought${if (state.dumps.size == 1) "" else "s"} saved in your brain dump.", size = 13.sp, color = AppColors.muted)
            }
            StepCard(3, "Tomorrow's frog") {
                AppField(tomorrow, { tomorrow = it }, "The one thing for tomorrow")
            }
            CButton("Close the day", {
                if (dump.isNotBlank()) state.addDump(dump)
                state.updateTomorrowFrog(tomorrow.trim())
                state.finishEvening()
                done = true
            }, Modifier.fillMaxWidth(), BtnKind.Accent)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/** Weekly review: screen time by day, frogs eaten, gates backed out of. */
@Composable
fun WeekScreen(state: LauncherState, nav: Nav) {
    val context = LocalContext.current
    val now = System.currentTimeMillis()
    val days = (6 downTo 0).map { now - it * DAY_MS }
    val keys = days.map { dayKey(it) }
    val frogs = keys.count { it in state.eatenDays }
    val backed = keys.sumOf { state.backedOutOn(it) }
    val week = state.weekMs
    val goalMs = state.screenGoalMin * 60_000L

    Column(Modifier.fillMaxSize()) {
        PageHeader("This week", onBack = nav.back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StepCardPlain("Screen time") {
                if (week.size == 7) {
                    val max = maxOf(goalMs, week.max()).coerceAtLeast(1L)
                    Row(Modifier.fillMaxWidth().height(150.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
                        week.forEachIndexed { i, ms ->
                            val today = i == 6
                            Column(Modifier.weight(1f).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                                val frac = (ms.toFloat() / max).coerceIn(0.04f, 1f)
                                if (frac < 1f) Spacer(Modifier.weight(1f - frac))
                                Box(Modifier.fillMaxWidth().weight(frac).background(if (today) AppColors.text else AppColors.dot, RoundedCornerShape(10.dp)))
                                Spacer(Modifier.height(8.dp))
                                AppText(SimpleDateFormat("EEE", Locale.getDefault()).format(Date(days[i])).take(3), size = 12.sp, weight = FontWeight.SemiBold, color = if (today) AppColors.text else AppColors.muted)
                            }
                        }
                    }
                    AppText("Goal: ${formatMillis(goalMs)} a day or less", size = 12.sp, color = AppColors.muted)
                    val avg = week.sum() / 7
                    AppText("Average ${formatMillis(avg)} a day", size = 14.sp, weight = FontWeight.SemiBold)
                } else {
                    AppText("Allow usage access to see how your week went.", size = 15.sp, color = AppColors.muted)
                    CButton("Allow usage access", { openUsageAccessSettings(context) }, kind = BtnKind.Soft, small = true)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("$frogs", "frogs eaten in the last 7 days", Modifier.weight(1f), good = true)
                StatCard("$backed", "times you backed out of a gate", Modifier.weight(1f), good = true)
            }
            AppText(
                if (backed > 0) "[^o^] every time you backed out, you won a little time back." else "[^_^] small steps add up. See you tomorrow.",
                size = 14.sp, color = AppColors.muted, mono = true, align = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            )
        }
    }
}

@Composable
private fun StepCardPlain(title: String, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxWidth().shadow(6.dp, RadiusLg, ambientColor = Color(0x14000000), spotColor = Color(0x14000000)).background(AppColors.card, RadiusLg).padding(22.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        AppText(title.uppercase(), size = 12.sp, weight = FontWeight.Bold, color = AppColors.muted, letterSpacing = 1.5.sp)
        content()
    }
}

@Composable
private fun StatCard(value: String, label: String, modifier: Modifier, good: Boolean) {
    Column(
        modifier.shadow(6.dp, RadiusMd, ambientColor = Color(0x14000000), spotColor = Color(0x14000000)).background(AppColors.card, RadiusMd).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        AppText(value, size = 30.sp, weight = FontWeight.Medium, letterSpacing = (-1).sp, color = if (good) AppColors.focus else AppColors.text)
        AppText(label, size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
    }
}
