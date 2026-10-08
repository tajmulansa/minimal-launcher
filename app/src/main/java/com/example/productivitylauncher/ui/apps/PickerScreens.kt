package com.example.productivitylauncher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.letterOf
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppBadge
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors

/** Pick which apps are gated. Adding is instant, removing waits 24 hours. */
@Composable
fun GatedPickerScreen(state: LauncherState, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Gated apps", onBack = nav.back)
        GatedPickerBody(state, Modifier.weight(1f))
    }
}

@Composable
fun GatedPickerBody(state: LauncherState, modifier: Modifier = Modifier) {
    Column(modifier) {
        AppText(
            "Pick the apps that steal your time. They get a calm gate before they open. Removing a gate takes 24 hours.",
            size = 14.sp, color = AppColors.muted, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.apps, key = { it.packageName + "/" + it.activityName }) { app ->
                val gated = app.packageName in state.gated
                val pending = app.packageName in state.pendingRemoval
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .background(if (gated) AppColors.gateSoft else AppColors.card, RadiusMd)
                        .clickableRole({
                            if (!gated) state.addGated(app.packageName)
                            else if (pending) state.cancelRemoveGated(app.packageName)
                            else state.requestRemoveGated(app.packageName)
                        })
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppBadge(letterOf(app.label), gated, size = 40.dp)
                    Column(Modifier.weight(1f)) {
                        AppText(app.label, size = 17.sp, weight = FontWeight.Medium, maxLines = 1, color = if (gated) AppColors.gate else AppColors.text)
                        if (pending) AppText("Removal in 24 hours. Tap to keep.", size = 12.sp, color = AppColors.muted)
                    }
                    CheckCircle(gated && !pending, size = 28.dp)
                }
            }
        }
    }
}

/** Pick up to six apps for the Home page. */
@Composable
fun HomePickerScreen(state: LauncherState, nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Home apps", onBack = nav.back)
        HomePickerBody(state, Modifier.weight(1f))
    }
}

@Composable
fun HomePickerBody(state: LauncherState, modifier: Modifier = Modifier) {
    Column(modifier) {
        AppText(
            "Choose up to six apps for your Home page. Keep only what helps you work. (${state.homeApps.size} of 6)",
            size = 14.sp, color = AppColors.muted, lineHeight = 20.sp, modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp),
        )
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.apps.filter { it.packageName !in state.gated }, key = { it.packageName + "/" + it.activityName }) { app ->
                val on = app.packageName in state.homeApps
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .background(AppColors.card, RadiusMd)
                        .clickableRole({
                            if (on) state.setHomeApps(state.homeApps - app.packageName)
                            else if (state.homeApps.size < 6) state.setHomeApps(state.homeApps + app.packageName)
                        })
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppBadge(letterOf(app.label), false, size = 40.dp)
                    AppText(app.label, Modifier.weight(1f), size = 17.sp, weight = FontWeight.Medium, maxLines = 1)
                    CheckCircle(on, size = 28.dp)
                }
            }
        }
    }
}
