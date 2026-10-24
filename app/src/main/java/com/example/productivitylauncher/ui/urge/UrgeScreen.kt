package com.example.productivitylauncher.ui.urge

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import com.example.productivitylauncher.ui.theme.Motion
import com.example.productivitylauncher.ui.theme.enterStagger
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors

/** Shown full screen while the red button's timer runs. Only a call button and a message button work. */
@Composable
fun UrgeScreen(remainingMillis: Long) {
    val context = LocalContext.current
    val totalSeconds = ((remainingMillis + 999L) / 1000L).coerceAtLeast(0L)
    val clock = "%d:%02d".format(totalSeconds / 60L, totalSeconds % 60L)
    // A slow ring that breathes in and out behind the timer, to give the eyes somewhere calm to rest.
    val breathe = rememberInfiniteTransition(label = "breathe")
    val phase by breathe.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4500, easing = Motion.EaseInOut), RepeatMode.Reverse),
        label = "phase",
    )
    val ringColor = AppColors.gate
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(300.dp)
                .graphicsLayer { scaleX = 0.8f + 0.28f * phase; scaleY = 0.8f + 0.28f * phase; alpha = 0.55f + 0.45f * phase }
                .background(ringColor.copy(alpha = 0.07f), CircleShape),
        )
        Box(
            Modifier
                .size(210.dp)
                .graphicsLayer { scaleX = 0.9f + 0.18f * phase; scaleY = 0.9f + 0.18f * phase }
                .background(ringColor.copy(alpha = 0.07f), CircleShape),
        )
    }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        AppText("TAKE A BREAK", Modifier.enterStagger(0), size = 13.sp, weight = FontWeight.Bold, color = AppColors.gate, letterSpacing = 2.sp)
        Spacer(Modifier.height(16.dp))
        AppText(clock, size = 88.sp, weight = FontWeight.Bold, mono = true)
        Spacer(Modifier.height(12.dp))
        AppText(
            modifier = Modifier.enterStagger(2),
            text = "This feeling passes. Put the phone down, breathe, or talk to someone.",
            size = 16.sp, color = AppColors.muted, align = TextAlign.Center, lineHeight = 24.sp,
        )
        Spacer(Modifier.height(44.dp))
        Row(Modifier.enterStagger(3), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            UrgeButton(Ic.Phone, "Call") { openPhone(context) }
            UrgeButton(Ic.Message, "Message") { openMessages(context) }
        }
    }
}

@Composable
private fun UrgeButton(icon: Ic, label: String, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .size(88.dp)
                .background(AppColors.card, RoundedCornerShape(20.dp))
                .clickableRole(onClick, Role.Button),
            contentAlignment = Alignment.Center,
        ) { AppIcon(icon, AppColors.text, size = 34.dp) }
        AppText(label, size = 14.sp, weight = FontWeight.SemiBold, color = AppColors.muted, modifier = Modifier.padding(top = 10.dp))
    }
}
