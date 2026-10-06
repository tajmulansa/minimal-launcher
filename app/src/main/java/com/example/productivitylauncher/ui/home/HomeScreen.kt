package com.example.productivitylauncher.ui.home

import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.FrogStore
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.theme.AppColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/**
 * Home screen: clock, the one important task ("frog"), the pet's line, and a way to the app list.
 * This is a placeholder layout, see docs/DESIGN.md.
 */
@Composable
fun HomeScreen(onOpenApps: () -> Unit) {
    val context = LocalContext.current
    val frogStore = remember { FrogStore(context) }
    var frog by remember { mutableStateOf(frogStore.get()) }

    // Ticks once per minute, aligned to the start of the minute, so we do not wake up needlessly.
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val timePattern = if (DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm a"
    val time = SimpleDateFormat(timePattern, Locale.getDefault()).format(now)
    val date = SimpleDateFormat("EEE dd MMM", Locale.getDefault()).format(now).lowercase()

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
    ) {
        AppText(time, size = 68.sp, weight = FontWeight.Bold, letterSpacing = (-3).sp)
        AppText(date, size = 13.sp, color = AppColors.Muted, modifier = Modifier.padding(top = 8.dp))
        Rule(Modifier.padding(vertical = 22.dp))

        AppText("> YOUR FROG", size = 12.sp, color = AppColors.Muted, letterSpacing = 3.sp)
        BasicTextField(
            value = frog,
            onValueChange = {
                frog = it
                frogStore.set(it)
            },
            textStyle = TextStyle(
                color = AppColors.Text,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
            ),
            cursorBrush = SolidColor(AppColors.Text),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            decorationBox = { innerTextField ->
                Box {
                    if (frog.isEmpty()) {
                        AppText(
                            "what is the one thing?",
                            size = 26.sp,
                            weight = FontWeight.Bold,
                            color = AppColors.Muted,
                        )
                    }
                    innerTextField()
                }
            },
        )

        Spacer(Modifier.weight(1f))

        // TODO(pet): replace this static line with the real pet engine, see docs/DESIGN.md.
        AppText("[^_^] morning! start with just 2 minutes.", size = 15.sp)
        Rule(Modifier.padding(vertical = 16.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(role = Role.Button, onClick = onOpenApps),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppText("all apps ›", size = 14.sp, color = AppColors.Muted)
        }
    }
}
