package com.example.productivitylauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.DarkPalette
import com.example.productivitylauncher.ui.theme.LightPalette
import com.example.productivitylauncher.ui.theme.Palette
import com.example.productivitylauncher.ui.theme.ThemeMode
import com.example.productivitylauncher.ui.theme.paletteOf

/** A grid of small previews, one per theme. Tap one to use it. */
@Composable
fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ThemeMode.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { mode -> ThemeSwatch(mode, mode == selected, { onSelect(mode) }, Modifier.weight(1f)) }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ThemeSwatch(mode: ThemeMode, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Column(
        modifier
            .semantics { contentDescription = mode.label + " theme" + if (selected) ", selected" else "" }
            .clickableRole(onClick, Role.RadioButton),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val shape = RoundedCornerShape(14.dp)
        Box(
            Modifier
                .fillMaxWidth()
                .height(68.dp)
                .clip(shape)
                .border(BorderStroke(if (selected) 2.5.dp else 1.dp, if (selected) AppColors.focus else AppColors.dot), shape),
        ) {
            val p = paletteOf(mode)
            if (p != null) {
                SwatchFace(p, Modifier.fillMaxSize())
            } else {
                // Auto follows the phone, so show a light half and a dark half.
                Row(Modifier.fillMaxSize()) {
                    SwatchFace(LightPalette, Modifier.weight(1f).fillMaxHeight())
                    SwatchFace(DarkPalette, Modifier.weight(1f).fillMaxHeight())
                }
            }
        }
        AppText(mode.label, size = 13.sp, weight = if (selected) FontWeight.SemiBold else FontWeight.Medium, color = if (selected) AppColors.text else AppColors.muted, maxLines = 1)
    }
}

@Composable
private fun SwatchFace(p: Palette, modifier: Modifier) {
    Box(modifier.background(p.phone).padding(8.dp)) {
        Column(
            Modifier.fillMaxSize().background(p.card, RoundedCornerShape(6.dp)).padding(6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(Modifier.size(8.dp).background(p.focus, CircleShape))
            Box(Modifier.fillMaxWidth(0.7f).height(4.dp).background(p.text, RoundedCornerShape(2.dp)))
            Box(Modifier.fillMaxWidth(0.45f).height(4.dp).background(p.muted, RoundedCornerShape(2.dp)))
        }
    }
}
