package com.example.productivitylauncher.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.data.AppEntry
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.isDndOn
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openMessages
import com.example.productivitylauncher.data.openPhone
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppBadge
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.GatedTag
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.RadiusMd
import com.example.productivitylauncher.ui.components.RadiusPill
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Inter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

/** Home page: round clock, date, screen time, six apps and the dock. */
@Composable
fun HomeScreen(
    state: LauncherState,
    nav: Nav,
    onOpenApp: (AppEntry) -> Unit,
    page: Int,
) {
    val context = LocalContext.current
    val now by produceState(initialValue = Date()) {
        while (true) {
            value = Date()
            delay(60_000L - System.currentTimeMillis() % 60_000L)
        }
    }
    val day = SimpleDateFormat("EEEE", Locale.getDefault()).format(now)
    val month = SimpleDateFormat("d MMMM", Locale.getDefault()).format(now)

    var dnd by remember { mutableStateOf(isDndOn(context)) }

    Column(Modifier.fillMaxSize()) {
        // A big round clock in the middle, with the date under it. Tap the clock to open Settings.
        Column(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnalogClock(
                now,
                Modifier.semantics { contentDescription = "Clock. Opens settings." }.clickableRole({ nav.go(Route.Settings) }),
                diameter = 124.dp,
            )
            Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AppText(day, size = 22.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.6).sp)
                AppText(month.uppercase(), size = 12.sp, weight = FontWeight.Medium, color = AppColors.muted, letterSpacing = 1.5.sp)
            }
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(top = 18.dp, bottom = 18.dp)
                .clickableRole({ if (state.usageAccess) nav.go(Route.Week) else nav.go(Route.Settings) }),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            val st = state.screenTimeMs
            AppText(if (st != null) formatMillis(st) else "--", size = 48.sp, letterSpacing = (-2).sp)
            AppText(
                if (st != null) "SCREEN TIME TODAY" else "ALLOW USAGE ACCESS TO SEE SCREEN TIME",
                size = 12.sp, weight = FontWeight.SemiBold, color = AppColors.muted, letterSpacing = 2.sp, align = TextAlign.Center,
            )
        }

        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val shown = state.homeApps.mapNotNull { state.appByPackage(it) }
            shown.forEach { app ->
                AppRow(app, gated = state.isGated(app.packageName), onClick = { onOpenApp(app) })
            }
            if (shown.size < 6) {
                CeramicCard(shape = RadiusMd, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp), onClick = { nav.go(Route.HomePicker) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Box(Modifier.size(44.dp).background(AppColors.phone, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                            AppIcon(Ic.Plus, AppColors.muted, size = 20.dp)
                        }
                        AppText(if (shown.isEmpty()) "Choose your six apps" else "Add an app", size = 17.sp, weight = FontWeight.Medium, color = AppColors.muted)
                    }
                }
            }
            CeramicCard(shape = RadiusMd, padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 16.dp), onClick = { nav.go(Route.Apps) }) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(Modifier.size(44.dp).background(AppColors.phone, RoundedCornerShape(11.dp)), contentAlignment = Alignment.Center) {
                        AppIcon(Ic.Search, AppColors.muted, size = 20.dp)
                    }
                    AppText("All apps", Modifier.weight(1f), size = 17.sp, weight = FontWeight.Medium, color = AppColors.muted)
                    AppIcon(Ic.Chevron, AppColors.muted, size = 18.dp)
                }
            }
        }

        Dots(page)
        Dock(
            dnd = dnd,
            onPhone = { openPhone(context) },
            onMessages = { openMessages(context) },
            onDnd = {
                if (setDnd(context, !dnd)) dnd = !dnd else openDndSettings(context)
            },
            onFocus = { nav.go(Route.Focus) },
        )
    }
}

@Composable
private fun AppRow(app: AppEntry, gated: Boolean, onClick: () -> Unit) {
    CeramicCard(
        shape = RadiusMd,
        color = if (gated) AppColors.gateSoft else AppColors.card,
        padding = androidx.compose.foundation.layout.PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        onClick = onClick,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AppBadge(com.example.productivitylauncher.data.letterOf(app.label), gated)
            AppText(
                app.label, Modifier.weight(1f), size = 17.sp,
                weight = if (gated) FontWeight.SemiBold else FontWeight.Medium,
                color = if (gated) AppColors.gate else AppColors.text, maxLines = 1,
            )
            if (gated) GatedTag()
        }
    }
}

@Composable
fun Dots(page: Int, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        for (i in 0..1) {
            val on = i == page
            Box(
                Modifier
                    .padding(horizontal = 5.dp)
                    .size(width = if (on) 22.dp else 8.dp, height = 8.dp)
                    .background(if (on) AppColors.text else AppColors.dot, RoundedCornerShape(4.dp)),
            )
        }
    }
}

@Composable
private fun Dock(dnd: Boolean, onPhone: () -> Unit, onMessages: () -> Unit, onDnd: () -> Unit, onFocus: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, bottom = 20.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .shadow(10.dp, RadiusPill, ambientColor = Color(0x1A000000), spotColor = Color(0x1A000000))
                .background(AppColors.card, RadiusPill)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                DockButton(Ic.Phone, "Phone", false, onPhone)
                DockButton(Ic.Message, "Messages", false, onMessages)
            }
            Box(Modifier.width(2.dp).height(24.dp).background(AppColors.phone))
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                DockButton(Ic.Moon, if (dnd) "Do not disturb, on" else "Do not disturb, off", dnd, onDnd)
                DockButton(Ic.Target, "Focus", false, onFocus)
            }
        }
    }
}

@Composable
private fun DockButton(ic: Ic, description: String, active: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .background(if (active) AppColors.phone else Color.Transparent, RoundedCornerShape(14.dp))
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, if (active) AppColors.text else AppColors.muted, size = 24.dp)
    }
}

/** The round clock. */
@Composable
fun AnalogClock(now: Date, modifier: Modifier = Modifier, diameter: androidx.compose.ui.unit.Dp = 76.dp) {
    val cal = Calendar.getInstance().apply { time = now }
    val minutes = cal.get(Calendar.MINUTE) + cal.get(Calendar.SECOND) / 60f
    val hours = (cal.get(Calendar.HOUR) % 12) + minutes / 60f
    val ring = AppColors.line
    val ink = AppColors.text
    val dot = AppColors.gate
    Box(
        modifier
            .size(diameter)
            .shadow(4.dp, CircleShape, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .background(AppColors.card, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        val measurer = rememberTextMeasurer()
        val numeralStyle = TextStyle(color = AppColors.muted, fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = (diameter.value * 0.095f).sp)
        Canvas(Modifier.size(diameter)) {
            val c = Offset(size.width / 2, size.height / 2)
            val outer = size.minDimension / 2 - 6.dp.toPx()
            drawCircle(ring, radius = outer, center = c, style = Stroke(width = 2.dp.toPx()))
            // Sixty tiny marks, a longer one for each hour, a bold one at 12, 3, 6 and 9.
            for (i in 0 until 60) {
                val a = Math.toRadians((i * 6 - 90).toDouble())
                val hour = i % 5 == 0
                val quarter = i % 15 == 0
                val len = if (quarter) 7.dp.toPx() else if (hour) 5.dp.toPx() else 2.dp.toPx()
                val from = Offset(c.x + ((outer - len) * Math.cos(a)).toFloat(), c.y + ((outer - len) * Math.sin(a)).toFloat())
                val to = Offset(c.x + (outer * Math.cos(a)).toFloat(), c.y + (outer * Math.sin(a)).toFloat())
                drawLine(if (quarter) ink else if (hour) AppColors.muted else AppColors.dot, from, to, strokeWidth = if (quarter) 2.5.dp.toPx() else if (hour) 1.5.dp.toPx() else 1.dp.toPx(), cap = StrokeCap.Round)
            }
            // Numerals at 12, 3, 6 and 9.
            val numeralRadius = outer - 17.dp.toPx()
            listOf("12" to 0, "3" to 90, "6" to 180, "9" to 270).forEach { (label, deg) ->
                val a = Math.toRadians((deg - 90).toDouble())
                val m = measurer.measure(label, numeralStyle)
                val p = Offset(c.x + (numeralRadius * Math.cos(a)).toFloat() - m.size.width / 2f, c.y + (numeralRadius * Math.sin(a)).toFloat() - m.size.height / 2f)
                drawText(m, topLeft = p)
            }
            fun hand(angleDeg: Float, length: Float, width: Float, color: Color = ink) {
                val a = Math.toRadians((angleDeg - 90).toDouble())
                val end = Offset(c.x + (length * Math.cos(a)).toFloat(), c.y + (length * Math.sin(a)).toFloat())
                drawLine(color, c, end, strokeWidth = width, cap = StrokeCap.Round)
            }
            hand(hours * 30f, outer * 0.45f, 5.dp.toPx())
            hand(minutes * 6f, outer * 0.72f, 3.dp.toPx())
            drawCircle(dot, radius = 4.5.dp.toPx(), center = c)
            drawCircle(AppColors.card, radius = 1.5.dp.toPx(), center = c)
        }
    }
}
