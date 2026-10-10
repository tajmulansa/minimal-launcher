package com.example.productivitylauncher.ui.settings

import androidx.compose.foundation.layout.Arrangement
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
        }
    }
}
