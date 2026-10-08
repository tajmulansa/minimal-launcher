package com.example.productivitylauncher.data

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

/** Hosts other apps' home screen widgets inside the launcher's widget page. */
class WidgetHost(context: Context) {
    val manager: AppWidgetManager = AppWidgetManager.getInstance(context)
    val host = AppWidgetHost(context, HOST_ID)

    fun start() { runCatching { host.startListening() } }
    fun stop() { runCatching { host.stopListening() } }

    fun providers(): List<AppWidgetProviderInfo> =
        runCatching { manager.installedProviders }.getOrDefault(emptyList())

    fun allocate(): Int = host.allocateAppWidgetId()
    fun delete(id: Int) { runCatching { host.deleteAppWidgetId(id) } }

    private companion object {
        const val HOST_ID = 1024
    }
}
