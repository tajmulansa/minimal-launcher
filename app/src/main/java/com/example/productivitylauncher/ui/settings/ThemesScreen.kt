package com.example.productivitylauncher.ui.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.Segmented
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.Accent
import com.example.productivitylauncher.ui.theme.Backdrop
import com.example.productivitylauncher.ui.theme.LocalPalette
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.ThemePicker
import com.example.productivitylauncher.ui.theme.AppColors

/** Settings > Theme > Themes: pick how the launcher looks. */
@Composable
fun ThemesScreen(state: LauncherState, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Themes", onBack = nav.back)
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AppText("Pick a look. Auto follows your phone's light or dark setting.", size = 15.sp, color = AppColors.muted, lineHeight = 22.sp)
            ThemePicker(state.themeMode, { state.setTheme(it) })

            AppText("Accent colour", size = 17.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            AppText("Used for checks, rings and highlights.", size = 14.sp, color = AppColors.muted)
            val dark = LocalPalette.current.dark
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Accent.entries.forEach { a ->
                    val swatch = (if (dark) a.dark else a.light) ?: AppColors.muted
                    val on = a == state.accent
                    Box(
                        Modifier.size(38.dp)
                            .then(if (a == Accent.Default) Modifier.border(2.dp, AppColors.muted, CircleShape) else Modifier.background(swatch, CircleShape))
                            .semantics { contentDescription = a.label + " accent" + if (on) ", selected" else "" }
                            .clickableRole({ state.setAccent(a) }, Role.RadioButton),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (on) AppIcon(Ic.Check, if (a == Accent.Default) AppColors.muted else if (dark) Color(0xFF0E1412) else Color.White, size = 18.dp)
                    }
                }
            }
            AppText(state.accent.label, size = 13.sp, color = AppColors.muted)

            AppText("Background", size = 17.sp, weight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            AppText("A soft glow behind frosted cards, a gentle gradient, or plain. Notebook keeps its ruled paper.", size = 14.sp, color = AppColors.muted, lineHeight = 20.sp)
            Segmented(Backdrop.entries.map { it.label }, Backdrop.entries.indexOf(state.backdrop), { state.setBackdrop(Backdrop.entries[it]) })
        }
    }
}
