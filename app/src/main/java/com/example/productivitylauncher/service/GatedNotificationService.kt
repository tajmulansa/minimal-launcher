package com.example.productivitylauncher.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.example.productivitylauncher.data.shouldBlockNotification

/**
 * Hides notifications from gated apps, so they cannot pull you back in.
 * It only looks at which app a notification came from and never reads its text.
 * Android only runs it after you allow Notification access for this app.
 */
class GatedNotificationService : NotificationListenerService() {

    private fun blockIfGated(sbn: StatusBarNotification) {
        // Ongoing notifications (music players, navigation) belong to a running service, so leave them.
        if (sbn.isOngoing) return
        val prefs = getSharedPreferences("launcher", MODE_PRIVATE)
        val gated = prefs.getStringSet("gated", emptySet()) ?: emptySet()
        val enabled = prefs.getInt("block_notifs", 1) == 1
        if (shouldBlockNotification(sbn.packageName, gated, enabled, packageName)) {
            cancelNotification(sbn.key)
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        blockIfGated(sbn)
    }

    override fun onListenerConnected() {
        // Clear what is already waiting when access is first allowed.
        runCatching { activeNotifications?.forEach { blockIfGated(it) } }
    }
}
