package com.example.productivitylauncher.ui.apps

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.filterApps
import com.example.productivitylauncher.data.loadLaunchableApps
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.theme.AppColors

/** Searchable, text-only list of every launchable app. */
@Composable
fun AppListScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val apps = remember { loadLaunchableApps(context.packageManager, context.packageName) }
    var query by remember { mutableStateOf("") }
    val shown = remember(query, apps) { filterApps(apps, query) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            AppText("> all apps", size = 30.sp, weight = FontWeight.Bold, letterSpacing = (-1).sp)
            AppText("${apps.size} apps", size = 13.sp, color = AppColors.Muted)
        }

        BasicTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            textStyle = TextStyle(
                color = AppColors.Text,
                fontSize = 16.sp,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(AppColors.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .semantics { contentDescription = "Search apps" },
            decorationBox = { innerTextField ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppText("> ", size = 16.sp, color = AppColors.Muted)
                    Box(Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            AppText("type to search", size = 16.sp, color = AppColors.Muted)
                        }
                        innerTextField()
                    }
                }
            },
        )
        Rule()

        LazyColumn(Modifier.weight(1f)) {
            items(shown, key = { "${it.packageName}/${it.activityName}" }) { app ->
                AppText(
                    text = app.label.lowercase(),
                    size = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(role = Role.Button) { launchApp(context, app) }
                        .wrapContentHeight(Alignment.CenterVertically),
                )
            }
        }

        Rule()
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onClose),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText("‹ home", size = 14.sp, color = AppColors.Muted)
        }
    }
}

private fun launchApp(context: Context, app: AppEntry) {
    val intent = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setComponent(ComponentName(app.packageName, app.activityName))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    // The app may have been uninstalled or disabled since the list was loaded.
    runCatching { context.startActivity(intent) }
}
