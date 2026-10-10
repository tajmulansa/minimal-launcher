package com.example.productivitylauncher.ui.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.LauncherWidget
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors

/** Add or remove widgets: the launcher's own, or widgets from other apps. */
@Composable
fun AddWidgetScreen(state: LauncherState, nav: Nav, host: WidgetHost) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf<String?>(null) }
    var pendingInfo by remember { mutableStateOf<Pair<Int, AppWidgetProviderInfo>?>(null) }

    fun finishAdd(id: Int) {
        state.addWidgetId("ext:$id")
        pendingInfo = null
        message = "Added. Find it on your Widgets page."
    }

    fun cancelAdd(id: Int) {
        host.delete(id)
        pendingInfo = null
    }

    val configLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val p = pendingInfo
        if (p != null) {
            if (result.resultCode == Activity.RESULT_OK) finishAdd(p.first) else cancelAdd(p.first)
        }
    }

    fun configureOrAdd(id: Int, info: AppWidgetProviderInfo) {
        val cfg = info.configure
        if (cfg == null) {
            finishAdd(id)
        } else {
            pendingInfo = id to info
            val i = Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                .setComponent(cfg)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            val ok = runCatching { configLauncher.launch(i) }.isSuccess
            if (!ok) finishAdd(id)
        }
    }

    val bindLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val p = pendingInfo
        if (p != null) {
            if (result.resultCode == Activity.RESULT_OK) configureOrAdd(p.first, p.second) else cancelAdd(p.first)
        }
    }

    fun begin(info: AppWidgetProviderInfo) {
        val id = host.allocate()
        if (host.manager.bindAppWidgetIdIfAllowed(id, info.provider)) {
            configureOrAdd(id, info)
        } else {
            pendingInfo = id to info
            val i = Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, info.provider)
            val ok = runCatching { bindLauncher.launch(i) }.isSuccess
            if (!ok) { cancelAdd(id); message = "This widget could not be added." }
        }
    }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Add widget", onBack = nav.back)
        Row(Modifier.fillMaxWidth().padding(horizontal = 28.dp)) {
            listOf("From launcher", "From apps").forEachIndexed { i, label ->
                Column(
                    Modifier.weight(1f).heightIn(min = 44.dp).clickableRole({ tab = i; message = null }, Role.Tab),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AppText(label, size = 16.sp, weight = if (tab == i) FontWeight.SemiBold else FontWeight.Medium, color = if (tab == i) AppColors.text else AppColors.muted, modifier = Modifier.padding(vertical = 10.dp), maxLines = 1)
                    Box(Modifier.fillMaxWidth().height(2.dp).background(if (tab == i) AppColors.text else AppColors.line))
                }
            }
        }
        message?.let { AppText(it, size = 14.sp, color = AppColors.focus, modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)) }

        if (tab == 0) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(LauncherWidget.entries.toList(), key = { it.id }) { w ->
                    val added = state.hasWidget(w.id)
                    AddRow(letter = w.letter, title = w.title, subtitle = w.subtitle, added = added, onClick = { state.toggleWidget(w.id) })
                }
            }
        } else {
            // One row per app. Tap an app to see the widgets it has, then add the one you want.
            val groups = remember {
                val pm = context.packageManager
                host.providers()
                    .groupBy { it.provider.packageName }
                    .map { (pkg, list) ->
                        val appLabel = runCatching { pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString() }.getOrDefault(pkg)
                        AppWidgets(pkg, appLabel, list.sortedBy { it.loadLabel(pm).lowercase() })
                    }
                    .sortedBy { it.label.lowercase() }
            }
            var openPkg by remember { mutableStateOf<String?>(null) }
            if (groups.isEmpty()) {
                AppText("No widgets from other apps were found.", color = AppColors.muted, modifier = Modifier.padding(28.dp))
            }
            val density = context.resources.displayMetrics.density
            LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 28.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(groups, key = { it.pkg }) { g ->
                    val open = openPkg == g.pkg
                    Column(Modifier.fillMaxWidth().background(AppColors.card, RadiusMd)) {
                        Row(
                            Modifier.fillMaxWidth().clickableRole({ openPkg = if (open) null else g.pkg }, Role.Button).padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                        ) {
                            Box(Modifier.size(40.dp).background(AppColors.phone, CircleShape), contentAlignment = Alignment.Center) {
                                AppText(g.label.firstOrNull()?.uppercaseChar()?.toString() ?: "?", size = 16.sp, weight = FontWeight.SemiBold)
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                AppText(g.label, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 1)
                                AppText(if (g.widgets.size == 1) "1 widget" else "${g.widgets.size} widgets", size = 13.sp, color = AppColors.muted, maxLines = 1)
                            }
                            Box(Modifier.rotate(if (open) 90f else 0f)) { AppIcon(Ic.Chevron, AppColors.muted, size = 18.dp) }
                        }
                        if (open) {
                            g.widgets.forEach { info ->
                                val cols = ((info.minWidth / density + 30) / 70).toInt().coerceAtLeast(1)
                                val rows = ((info.minHeight / density + 30) / 70).toInt().coerceAtLeast(1)
                                val name = info.loadLabel(context.packageManager)
                                // The picture the widget's app made for this widget, so you can see what you are adding.
                                val preview = remember(info) {
                                    runCatching {
                                        val d = info.loadPreviewImage(context, 0) ?: info.loadIcon(context, 0)
                                        d?.toBitmap(
                                            width = d.intrinsicWidth.coerceIn(1, 1200),
                                            height = d.intrinsicHeight.coerceIn(1, 1200),
                                        )?.asImageBitmap()
                                    }.getOrNull()
                                }
                                Column(
                                    Modifier.fillMaxWidth().clickableRole({ begin(info) }).padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Box(
                                        Modifier.fillMaxWidth().heightIn(min = 72.dp, max = 190.dp).background(AppColors.phone, RoundedCornerShape(14.dp)).padding(10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        if (preview != null) {
                                            Image(preview, contentDescription = "Preview of $name", Modifier.fillMaxWidth().heightIn(max = 170.dp), contentScale = ContentScale.Fit)
                                        } else {
                                            AppText("No preview", size = 13.sp, color = AppColors.muted)
                                        }
                                    }
                                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                            AppText(name, size = 15.sp, weight = FontWeight.Medium, maxLines = 2)
                                            AppText("$cols × $rows", size = 12.sp, color = AppColors.muted)
                                        }
                                        Box(Modifier.size(40.dp).background(AppColors.focus, CircleShape), contentAlignment = Alignment.Center) {
                                            AppIcon(Ic.Plus, AppColors.onFocus, size = 18.dp, description = "Add $name")
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

private class AppWidgets(val pkg: String, val label: String, val widgets: List<AppWidgetProviderInfo>)

@Composable
private fun AddRow(letter: String, title: String, subtitle: String, added: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(AppColors.card, RadiusMd).clickableRole(onClick).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(40.dp).background(AppColors.phone, CircleShape), contentAlignment = Alignment.Center) {
            AppText(letter, size = 16.sp, weight = FontWeight.SemiBold)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            AppText(title, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 1)
            AppText(subtitle, size = 13.sp, color = AppColors.muted, maxLines = 1)
        }
        Box(Modifier.size(44.dp).background(if (added) AppColors.focus else AppColors.phone, CircleShape), contentAlignment = Alignment.Center) {
            AppIcon(if (added) Ic.Check else Ic.Plus, if (added) AppColors.onFocus else AppColors.text, size = 20.dp, description = if (added) "Added" else "Add")
        }
    }
}
