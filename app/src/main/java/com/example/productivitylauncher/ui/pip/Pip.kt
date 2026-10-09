package com.example.productivitylauncher.ui.pip

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.PipAction
import com.example.productivitylauncher.data.PipMessage
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors

/** How Pip rests against the screen edge. */
object PipDock {
    /** Seconds of nobody touching Pip before it tucks itself away. */
    const val IdleMillis = 5_000L
    val Size = 56.dp

    /** Gap between the bubble and the screen edge while it is out. */
    val Gap = 28.dp
}

/**
 * The floating [^_^] bubble. When [docked] it slides half way off the right edge and fades,
 * and a red dot on the part that is still visible tells you Pip has something to say.
 * Touching it while docked is up to the caller: it should slide it back out.
 */
@Composable
fun PipBubble(message: PipMessage, docked: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val slide by animateDpAsState(if (docked) PipDock.Gap + PipDock.Size / 2 else 0.dp, tween(280), label = "pipSlide")
    val fade by animateFloatAsState(if (docked) 0.5f else 1f, tween(280), label = "pipFade")
    val label = when {
        !docked -> "Pip, tap to talk"
        message.nudge -> "Pip is tucked away and has a message, tap to bring it out"
        else -> "Pip is tucked away, tap to bring it out"
    }
    // The outer box is a little wider than the bubble so the half that stays on screen is easy to hit.
    Box(
        modifier
            .padding(end = PipDock.Gap)
            .offset { IntOffset(slide.roundToPx(), 0) }
            .semantics { contentDescription = label }
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, role = Role.Button, onClick = onClick),
    ) {
        Box(
            Modifier
                .padding(start = 12.dp)
                .size(PipDock.Size)
                .alpha(fade)
                .shadow(10.dp, CircleShape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                .background(AppColors.card, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            AppText(message.face, size = 12.sp, weight = FontWeight.Medium, mono = true)
        }
        if (message.nudge) {
            // Docked, the right half is off screen, so the dot moves to the left half that still shows.
            Box(
                Modifier
                    .align(if (docked) Alignment.TopStart else Alignment.TopEnd)
                    .padding(start = if (docked) 12.dp else 0.dp)
                    .size(16.dp)
                    .background(AppColors.gate, CircleShape)
                    .border(2.dp, AppColors.phone, CircleShape),
            )
        }
    }
}

/** A small speech chip beside the bubble, shown briefly right after something good happens. */
@Composable
fun PipChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .shadow(8.dp, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp), ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
            .background(AppColors.card, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
            .clickableRole(onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        AppText(text, size = 13.sp, lineHeight = 18.sp, maxLines = 4)
    }
}

/** Pip's message card, shown over a soft scrim. */
@Composable
fun PipCard(message: PipMessage, timeLabel: String, onDismiss: () -> Unit, onAction: (PipAction) -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 112.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(26.dp, 26.dp, 6.dp, 26.dp), ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
                .background(AppColors.card, RoundedCornerShape(26.dp, 26.dp, 6.dp, 26.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .padding(22.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                AppText("PIP  ${message.face}", size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted, letterSpacing = 0.5.sp)
                AppText(timeLabel, size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
            }
            AppText(message.text, size = 16.sp, lineHeight = 24.sp)
            Row(Modifier.fillMaxWidth().padding(top = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (message.primaryLabel != null) {
                    CButton(message.primaryLabel, { onAction(message.action) }, Modifier.weight(1f), BtnKind.Accent, small = true)
                }
                CButton(if (message.primaryLabel != null) "Later" else "Thanks", onDismiss, Modifier.weight(1f), BtnKind.Soft, small = true)
            }
        }
    }
}
