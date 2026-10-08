package com.example.productivitylauncher.ui.apps

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.filterApps
import com.example.productivitylauncher.data.letterOf
import com.example.productivitylauncher.data.openAppInfo
import com.example.productivitylauncher.data.sectionOf
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppBadge
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Chip
import com.example.productivitylauncher.ui.components.GatedTag
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Inter

private enum class Filter(val label: String) { All("All"), Work("Work"), Gated("Gated") }

/** All apps: search, filter chips, A to Z list. Long-press an app for options. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppListScreen(state: LauncherState, nav: Nav, onOpenApp: (AppEntry) -> Unit) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(Filter.All) }
    var options by remember { mutableStateOf<AppEntry?>(null) }

    val base = when (filter) {
        Filter.All -> state.apps
        Filter.Work -> state.apps.filter { !state.isGated(it.packageName) }
        Filter.Gated -> state.apps.filter { state.isGated(it.packageName) }
    }
    val shown = filterApps(base, query)

    Column(Modifier.fillMaxSize()) {
        PageHeader("All apps", onBack = nav.back)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .background(AppColors.card, RadiusPill)
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            AppIcon(Ic.Search, AppColors.muted, size = 20.dp)
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(color = AppColors.text, fontSize = 16.sp, fontFamily = Inter),
                cursorBrush = SolidColor(AppColors.text),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box { if (query.isEmpty()) AppText("Search apps", color = AppColors.muted); inner() }
                },
            )
        }
        Row(Modifier.padding(start = 28.dp, end = 28.dp, top = 14.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Filter.entries.forEach { f -> Chip(f.label, filter == f, { filter = f }) }
        }

        if (shown.isEmpty()) {
            AppText(
                if (filter == Filter.Gated && query.isEmpty()) "No gated apps yet. Long-press an app to gate it." else "No apps match.",
                color = AppColors.muted, modifier = Modifier.padding(32.dp),
            )
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            var last = ""
            val grouped = shown.map { app ->
                val sec = sectionOf(app.label)
                val header = if (sec != last) sec else null
                last = sec
                app to header
            }
            items(grouped, key = { it.first.packageName + "/" + it.first.activityName }) { (app, header) ->
                if (header != null) AppText(header, size = 13.sp, weight = FontWeight.Bold, color = AppColors.muted, modifier = Modifier.padding(start = 4.dp, top = 10.dp))
                val gated = state.isGated(app.packageName)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 60.dp)
                        .background(if (gated) AppColors.gateSoft else AppColors.card, RadiusMd)
                        .combinedClickable(role = Role.Button, onClick = { onOpenApp(app) }, onLongClick = { options = app })
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    AppBadge(letterOf(app.label), gated, size = 40.dp)
                    AppText(
                        app.label, Modifier.weight(1f), size = 17.sp, maxLines = 1,
                        weight = if (gated) FontWeight.SemiBold else FontWeight.Medium,
                        color = if (gated) AppColors.gate else AppColors.text,
                    )
                    if (gated) GatedTag()
                }
            }
        }
    }

    val target = options
    if (target != null) {
        val gated = state.isGated(target.packageName)
        val onHome = target.packageName in state.homeApps
        val pending = target.packageName in state.pendingRemoval
        AlertDialog(
            onDismissRequest = { options = null },
            containerColor = AppColors.card,
            title = { AppText(target.label, size = 20.sp, weight = FontWeight.SemiBold) },
            text = {
                Column {
                    if (gated) {
                        AppText(
                            if (pending) "Gate removal is waiting for its 24 hour delay." else "Removing a gate takes 24 hours, so a weak moment can't undo it.",
                            size = 14.sp, color = AppColors.muted, modifier = Modifier.padding(bottom = 8.dp),
                        )
                    }
                    OptionRow(if (gated) (if (pending) "Keep the gate" else "Remove gate (in 24 hours)") else "Gate this app") {
                        if (!gated) state.addGated(target.packageName)
                        else if (pending) state.cancelRemoveGated(target.packageName)
                        else state.requestRemoveGated(target.packageName)
                        options = null
                    }
                    if (!gated) {
                        OptionRow(if (onHome) "Remove from Home" else "Add to Home") {
                            if (onHome) state.setHomeApps(state.homeApps - target.packageName)
                            else state.setHomeApps((state.homeApps + target.packageName).takeLast(6))
                            options = null
                        }
                    }
                    OptionRow("App info") { openAppInfo(context, target.packageName); options = null }
                }
            },
            confirmButton = { TextButton(onClick = { options = null }) { AppText("Close", weight = FontWeight.SemiBold) } },
        )
    }
}

@Composable
private fun OptionRow(text: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().heightIn(min = 48.dp).combinedClickableSimple(onClick),
        contentAlignment = Alignment.CenterStart,
    ) { AppText(text, size = 16.sp, weight = FontWeight.Medium) }
}

@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableSimple(onClick: () -> Unit): Modifier =
    this.then(Modifier.combinedClickable(role = Role.Button, onClick = onClick))
