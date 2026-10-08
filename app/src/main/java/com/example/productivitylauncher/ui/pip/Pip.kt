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
import com.example.productivitylauncher.data.PipAsk
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

/** A small speech chip next to the bubble, shown briefly when Pip has something worth saying. */
@Composable
fun PipPreview(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val first = text.substringBefore(". ").let { if (it.length < text.length && !it.endsWith(".")) "$it." else it }
    Box(
        modifier
            .shadow(8.dp, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp), ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
            .background(AppColors.card, RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
            .clickableRole(onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        AppText(first, size = 13.sp, lineHeight = 18.sp, maxLines = 3)
    }
}

/** Pip's message card: the message, quick actions, and two questions you can ask. */
@Composable
fun PipCard(
    message: PipMessage,
    answer: String?,
    onAsk: (PipAsk) -> Unit,
    onClose: () -> Unit,
    onLater: () -> Unit,
    onAction: (PipAction) -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0x66000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onClose),
        contentAlignment = Alignment.BottomCenter,
    ) {
        Column(
            Modifier
                .padding(start = 20.dp, end = 20.dp, bottom = 112.dp)
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp), ambientColor = Color(0x33000000), spotColor = Color(0x33000000))
                .background(AppColors.card, RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                .padding(22.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                AppText("PIP  ${message.face}", size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted, letterSpacing = 0.5.sp)
                AppText("now", size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted)
            }
            AppText(answer ?: message.text, size = 16.sp, lineHeight = 24.sp)
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CButton("What now?", { onAsk(PipAsk.WhatNow) }, Modifier.weight(1f), BtnKind.Soft, small = true)
                CButton("How am I doing?", { onAsk(PipAsk.HowAmI) }, Modifier.weight(1f), BtnKind.Soft, small = true)
            }
            Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (answer == null && message.primaryLabel != null) {
                    CButton(message.primaryLabel, { onAction(message.action) }, Modifier.weight(1f), BtnKind.Accent, small = true)
                }
                CButton(if (message.nudge && answer == null) "Later" else "Thanks", if (message.nudge && answer == null) onLater else onClose, Modifier.weight(1f), BtnKind.Primary, small = true)
            }
        }
    }
}
