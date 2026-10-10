package com.example.productivitylauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.Vibe
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.LightPalette
import com.example.productivitylauncher.ui.theme.paletteOf

/** One Vibe as a tappable card: a little colour swatch, its name, and what it sets. */
@Composable
fun VibeCard(
    vibe: Vibe,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    val base = paletteOf(vibe.look.theme) ?: LightPalette
    val accentValue = if (base.dark) vibe.look.accent.dark else vibe.look.accent.light
    val accent = if (accentValue != null) Color(accentValue) else base.focus
    val shape = RadiusMd
    Row(
        modifier
            .fillMaxWidth()
            .background(AppColors.card, shape)
            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) AppColors.focus else AppColors.line), shape)
            .clickableRole(onClick, Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            Modifier.size(48.dp).clip(CircleShape).background(base.phone).border(1.dp, AppColors.dot, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(28.dp).background(base.card, CircleShape), contentAlignment = Alignment.Center) {
                Box(Modifier.size(12.dp).background(accent, CircleShape))
            }
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(vibe.name, size = 16.sp, weight = FontWeight.SemiBold, maxLines = 1)
            AppText(vibe.tagline, size = 13.sp, color = AppColors.muted, maxLines = 1)
            AppText(
                "${vibe.look.theme.label} · ${vibe.look.clock.label} clock · ${vibe.look.layout.label} · Pip ${vibe.look.tone.label.lowercase()}",
                size = 11.sp, color = AppColors.muted, maxLines = 2, lineHeight = 15.sp,
            )
        }
        trailing()
        if (selected) AppIcon(Ic.Check, AppColors.focus, size = 22.dp, description = "Selected")
    }
}
