package com.example.productivitylauncher.ui.pip

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.PipAction
import com.example.productivitylauncher.data.PipMessage
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors

/** The floating [^_^] bubble. A small dot shows when Pip has something to say. */
@Composable
fun PipBubble(message: PipMessage, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier) {
        Box(
            Modifier
                .size(56.dp)
                .shadow(10.dp, CircleShape, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                .background(AppColors.card, CircleShape)
                .semantics { contentDescription = "Pip, tap to talk" }
                .clickableRole(onClick),
            contentAlignment = Alignment.Center,
        ) {
            AppText(message.face, size = 12.sp, weight = FontWeight.Medium, mono = true)
        }
        if (message.nudge) {
            Box(Modifier.align(Alignment.TopEnd).size(14.dp).background(AppColors.gate, CircleShape))
        }
    }
}

/** Pip's message card, shown over a soft scrim. */
@Composable
fun PipCard(message: PipMessage, onDismiss: () -> Unit, onAction: (PipAction) -> Unit) {
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
                AppText("now", size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
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
