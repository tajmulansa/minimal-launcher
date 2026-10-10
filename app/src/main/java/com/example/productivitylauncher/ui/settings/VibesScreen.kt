package com.example.productivitylauncher.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.Accent
import com.example.productivitylauncher.data.ClockFace
import com.example.productivitylauncher.data.HomeLayout
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.PRESET_VIBES
import com.example.productivitylauncher.data.PipTone
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.ThemePicker
import com.example.productivitylauncher.ui.components.VibeCard
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.home.AnalogClock
import com.example.productivitylauncher.ui.theme.AppColors
import java.util.Date

/** Settings > Vibe: pick a ready-made Vibe, or change any part of it and save your own. */
@Composable
fun VibesScreen(state: LauncherState, nav: Nav) {
    val look = state.look
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        PageHeader("Vibes", onBack = nav.back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // A live preview: everything on this screen is already wearing the chosen Vibe.
            CeramicCard {
                Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    AnalogClock(Date(), diameter = 110.dp, face = look.clock)
                    AppText(state.activeVibe?.name ?: "Custom", size = 18.sp, weight = FontWeight.SemiBold)
                    AppText("Pip: \"${look.tone.sample}\"", size = 13.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 19.sp)
                }
            }

            Caption("Vibes", Modifier.padding(top = 8.dp))
            PRESET_VIBES.forEach { v -> VibeCard(v, state.activeVibe?.id == v.id, { state.updateLook(v.look) }) }
            state.customVibes.forEach { v ->
                VibeCard(v, state.activeVibe?.id == v.id, { state.updateLook(v.look) }, trailing = {
                    TextLink("Delete", { state.deleteVibe(v.id) })
                })
            }

            Caption("Make it yours", Modifier.padding(top = 12.dp))
            Section("Colours") {
                ThemePicker(look.theme, { state.updateLook(look.copy(theme = it)) })
            }
            Section("Accent") {
                AccentRow(look.accent, state, { state.updateLook(look.copy(accent = it)) })
            }
            Section("Clock face") {
                Segmented(ClockFace.entries.map { it.label }, look.clock.ordinal, { state.updateLook(look.copy(clock = ClockFace.entries[it])) })
            }
            Section("Home apps") {
                Segmented(HomeLayout.entries.map { it.label }, look.layout.ordinal, { state.updateLook(look.copy(layout = HomeLayout.entries[it])) })
            }
            Section("How Pip talks") {
                Segmented(PipTone.entries.map { it.label }, look.tone.ordinal, { state.updateLook(look.copy(tone = PipTone.entries[it])) })
                AppText("\"${look.tone.sample}\"", size = 14.sp, color = AppColors.muted, lineHeight = 20.sp)
            }

            if (state.activeVibe == null) {
                Section("Save as your own Vibe") {
                    AppField(name, { name = it }, "Name it, e.g. Finals week", onDone = { state.saveVibe(name); name = "" })
                    CButton("Save Vibe", { state.saveVibe(name); name = "" }, Modifier.fillMaxWidth(), BtnKind.Accent, small = true, enabled = name.isNotBlank())
                }
            }
            AppText("A Vibe changes how the launcher looks and how Pip talks. It never changes your gate rules.", size = 13.sp, color = AppColors.muted, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    CeramicCard(padding = androidx.compose.foundation.layout.PaddingValues(18.dp)) {
        AppText(title, size = 15.sp, weight = FontWeight.SemiBold)
        content()
    }
}

@Composable
private fun AccentRow(selected: Accent, state: LauncherState, onSelect: (Accent) -> Unit) {
    val base = com.example.productivitylauncher.ui.theme.resolvePalette(state.look.theme)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Accent.entries.forEach { a ->
            val value = if (base.dark) a.dark else a.light
            val color = if (value != null) Color(value) else base.focus
            val on = a == selected
            Box(
                Modifier
                    .size(38.dp)
                    .border(BorderStroke(if (on) 3.dp else 1.dp, if (on) AppColors.text else AppColors.dot), CircleShape)
                    .padding(5.dp)
                    .background(color, CircleShape)
                    .semantics { contentDescription = a.label + " accent" + if (on) ", selected" else "" }
                    .clickableRole({ onSelect(a) }, Role.RadioButton),
                contentAlignment = Alignment.Center,
            ) {
                if (a == Accent.Default) AppText("A", size = 12.sp, weight = FontWeight.Bold, color = base.onFocus)
            }
        }
    }
}
