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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.theme.AppColors

/** The same text lives in docs/PRIVACY.md. Keep the two in sync. */
private data class PolicySection(val title: String, val lines: List<String>)

private val POLICY = listOf(
    PolicySection(
        "The short version",
        listOf(
            "Everything stays on this phone. The app has no internet permission, no account, no ads and no analytics.",
            "Nothing you type or do in the launcher is sent to the developer or to anyone else.",
        ),
    ),
    PolicySection(
        "What is stored on your phone",
        listOf(
            "Your frog, notes, habits, brain dump, water count, gated apps list, settings, and how many times you opened or backed out of a gate.",
            "It is kept in the app's private storage. Cloud backup is turned off for this app.",
        ),
    ),
    PolicySection(
        "Permissions (all optional)",
        listOf(
            "Usage access: reads how long apps were used today, only to show your screen time and gated time. The numbers are worked out on the phone.",
            "Display over other apps: draws the small floating timer on top of a gated app.",
            "Do Not Disturb access: turns Do Not Disturb on for focus sessions and the Moon button.",
            "Calendar: reads today's events for the Agenda widget. Read only.",
            "Default home app: lets the launcher open when you press Home.",
            "The launcher still works if you say no. Each permission only turns off the feature that needs it.",
        ),
    ),
    PolicySection(
        "What we never do",
        listOf(
            "We do not sell, share, upload or track your data, and we do not show ads.",
        ),
    ),
    PolicySection(
        "Your control",
        listOf(
            "Change or remove any permission at any time in Android Settings, or from Settings in this app.",
            "Settings, Delete all my data removes your tasks, notes and settings. To protect you from a weak moment, gated apps and gate rules are removed 24 hours later.",
            "Uninstalling the app removes everything.",
        ),
    ),
    PolicySection(
        "Open source and contact",
        listOf(
            "The code is public, so you can check all of this: github.com/tajmulansa/minimal-launcher. Questions can be asked there as an issue.",
            "Last updated: October 2026.",
        ),
    ),
)

@Composable
fun PrivacyScreen(nav: Nav) {
    Column(Modifier.fillMaxSize()) {
        PageHeader("Privacy policy", onBack = nav.back)
        PrivacyBody(Modifier.weight(1f))
    }
}

/** The policy text, used on its own page and inside setup. */
@Composable
fun PrivacyBody(modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        POLICY.forEach { section ->
            CeramicCard(padding = androidx.compose.foundation.layout.PaddingValues(20.dp)) {
                AppText(section.title, size = 17.sp, weight = FontWeight.SemiBold)
                section.lines.forEach { line ->
                    AppText(line, size = 15.sp, color = AppColors.muted, lineHeight = 22.sp)
                }
            }
        }
    }
}
