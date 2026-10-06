package com.example.productivitylauncher.ui.tools

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.theme.AppColors

@Composable
fun ToolsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AppText("Tools", size = 24.sp, color = AppColors.Muted)
    }
}
