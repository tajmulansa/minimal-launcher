package com.example.productivitylauncher.data

import android.app.Activity
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

/** Small wrappers around Android system actions, so screens stay readable. */

fun launchApp(context: Context, app: AppEntry): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_LAUNCHER)
        component = ComponentName(app.packageName, app.activityName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    }
    return runCatching { context.startActivity(intent); true }.getOrElse {
        Toast.makeText(context, "Could not open ${app.label}", Toast.LENGTH_SHORT).show()
        false
    }
}

fun openPhone(context: Context) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

fun openMessages(context: Context) {
    runCatching {
        val i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MESSAGING)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }.onFailure {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    }
}

fun openAppInfo(context: Context, pkg: String) {
    runCatching {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$pkg"))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}

fun openSettingsAction(context: Context, action: String, pkgUri: Boolean = false) {
    runCatching {
        val i = Intent(action)
        if (pkgUri) i.data = Uri.parse("package:${context.packageName}")
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(i)
    }
}

fun openUsageAccessSettings(context: Context) = openSettingsAction(context, Settings.ACTION_USAGE_ACCESS_SETTINGS)
fun openOverlaySettings(context: Context) = openSettingsAction(context, Settings.ACTION_MANAGE_OVERLAY_PERMISSION, pkgUri = true)
fun openDndSettings(context: Context) = openSettingsAction(context, Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
fun openDefaultLauncherSettings(context: Context) = openSettingsAction(context, Settings.ACTION_HOME_SETTINGS)

fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

private fun notificationManager(context: Context) =
    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

fun hasDndAccess(context: Context): Boolean = notificationManager(context).isNotificationPolicyAccessGranted

fun isDndOn(context: Context): Boolean =
    hasDndAccess(context) &&
        notificationManager(context).currentInterruptionFilter != NotificationManager.INTERRUPTION_FILTER_ALL

/** Returns false when Do Not Disturb access has not been granted yet. */
fun setDnd(context: Context, on: Boolean): Boolean {
    val nm = notificationManager(context)
    if (!nm.isNotificationPolicyAccessGranted) return false
    nm.setInterruptionFilter(
        if (on) NotificationManager.INTERRUPTION_FILTER_PRIORITY else NotificationManager.INTERRUPTION_FILTER_ALL,
    )
    return true
}

fun isDefaultLauncher(context: Context): Boolean {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
    val resolved = context.packageManager.resolveActivity(intent, 0)
    return resolved?.activityInfo?.packageName == context.packageName
}

fun goHome(activity: Activity) {
    activity.startActivity(
        Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
    )
}
