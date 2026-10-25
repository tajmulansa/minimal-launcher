package com.example.productivitylauncher.ui.widgets

import android.Manifest
import android.appwidget.AppWidgetHostView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import com.example.productivitylauncher.ui.theme.Motion
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.productivitylauncher.data.LauncherState
import com.example.productivitylauncher.data.LauncherWidget
import com.example.productivitylauncher.data.URGE_LOCK_MINUTES
import com.example.productivitylauncher.data.WidgetHost
import com.example.productivitylauncher.data.clockLabel
import com.example.productivitylauncher.data.dayKey
import com.example.productivitylauncher.data.daysUntil
import com.example.productivitylauncher.data.formatClock
import com.example.productivitylauncher.data.formatMillis
import com.example.productivitylauncher.data.hasCalendarPermission
import com.example.productivitylauncher.data.hasDndAccess
import com.example.productivitylauncher.data.hasTorch
import com.example.productivitylauncher.data.isDndOn
import com.example.productivitylauncher.data.observeTorch
import com.example.productivitylauncher.data.openDndSettings
import com.example.productivitylauncher.data.openGrayscaleSettings
import com.example.productivitylauncher.data.openUsageAccessSettings
import com.example.productivitylauncher.data.setDnd
import com.example.productivitylauncher.data.setTorch
import com.example.productivitylauncher.data.todayEvents
import com.example.productivitylauncher.ui.Nav
import com.example.productivitylauncher.ui.Route
import com.example.productivitylauncher.ui.components.AppField
import com.example.productivitylauncher.ui.components.AppIcon
import com.example.productivitylauncher.ui.components.AppText
import com.example.productivitylauncher.ui.components.BtnKind
import com.example.productivitylauncher.ui.components.CButton
import com.example.productivitylauncher.ui.components.Caption
import com.example.productivitylauncher.ui.components.CeramicCard
import com.example.productivitylauncher.ui.components.CheckCircle
import com.example.productivitylauncher.ui.components.Ic
import com.example.productivitylauncher.ui.components.IconButtonLarge
import com.example.productivitylauncher.ui.components.PageHeader
import com.example.productivitylauncher.ui.components.RadiusLg
import com.example.productivitylauncher.ui.components.Rule
import com.example.productivitylauncher.ui.components.Stepper
import com.example.productivitylauncher.ui.components.TextLink
import com.example.productivitylauncher.ui.components.clickableRole
import com.example.productivitylauncher.ui.home.Dots
import com.example.productivitylauncher.ui.home.Hair
import androidx.compose.foundation.lazy.items
import com.example.productivitylauncher.ui.theme.AppColors
import kotlinx.coroutines.delay
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyGridState

/** Second page: your widgets as flat sections separated by hairlines. Edit mode: touch and hold a section, drag it, tap the minus to remove it. */
@Composable
fun WidgetsScreen(state: LauncherState, nav: Nav, host: WidgetHost, page: Int, onOpenHome: () -> Unit = {}) {
    var editing by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val reorderState = sh.calvin.reorderable.rememberReorderableLazyListState(listState) { from, to ->
        val a = from.key as? String
        val b = to.key as? String
        if (a != null && b != null) state.reorderWidget(a, b)
    }

    Column(Modifier.fillMaxSize()) {
        PageHeader("Today", trailing = {
            if (editing) {
                CButton("Done", { editing = false }, kind = BtnKind.Primary, small = true)
            } else {
                IconButtonLarge(Ic.Edit, "Edit widgets", { editing = true })
                IconButtonLarge(Ic.Plus, "Add widget", { nav.go(Route.AddWidget) })
                IconButtonLarge(Ic.Gear, "Settings", { nav.go(Route.Settings) })
            }
        })
        androidx.compose.foundation.lazy.LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(start = 28.dp, end = 28.dp, top = 8.dp, bottom = 12.dp),
        ) {
            if (editing) {
                item(key = "hint") {
                    AppText("Touch and hold a section, then drag it. Tap the minus to remove it.", size = 13.sp, color = AppColors.muted, lineHeight = 18.sp, modifier = Modifier.padding(bottom = 16.dp))
                }
            }
            if (state.widgets.isEmpty() && !editing) {
                item(key = "empty") {
                    AppText("Nothing here yet. Tap + to add a widget.", color = AppColors.muted, modifier = Modifier.padding(vertical = 24.dp))
                }
            }
            items(state.widgets, key = { it }) { id ->
                ReorderableItem(reorderState, key = id) { isDragging ->
                    val dragHandle = Modifier.longPressDraggableHandle(
                        onDragStarted = { haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
                    )
                    val index = state.widgets.indexOf(id)
                    Column {
                        if (index > 0) Hair(index, Modifier.padding(vertical = 22.dp))
                        WidgetFrame(id, index, editing, isDragging, dragHandle, state, nav, host)
                    }
                }
            }
            if (editing) {
                item(key = "add") {
                    Row(
                        Modifier.padding(top = 22.dp).fillMaxWidth().height(56.dp).border(1.dp, AppColors.line, RoundedCornerShape(28.dp)).clickableRole({ nav.go(Route.AddWidget) }),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppIcon(Ic.Plus, AppColors.muted, size = 18.dp)
                        AppText("  Add widget", size = 15.sp, weight = FontWeight.Medium, color = AppColors.muted)
                    }
                }
            }
        }
        Dots(page, onSelect = { if (it == 0) onOpenHome() })
        Spacer(Modifier.height(8.dp))
    }
}

/** One widget. While editing it shrinks a little, ignores taps, can be dragged, and shows a minus to remove it. */
@Composable
private fun WidgetFrame(id: String, index: Int, editing: Boolean, isDragging: Boolean, dragHandle: Modifier, state: LauncherState, nav: Nav, host: WidgetHost) {
    val scale by animateFloatAsState(if (isDragging) 1.04f else if (editing) 0.97f else 1f, Motion.lift(), label = "tile")
    // Tiles fade and rise in one after another the first time they appear.
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80L * index.coerceIn(0, 6)); shown = true }
    val appear by animateFloatAsState(if (shown) 1f else 0f, Motion.enter(450), label = "appear")
    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale; alpha = appear; translationY = (1f - appear) * 18.dp.toPx() }) {
        if (id.startsWith("ext:")) ExternalWidget(id.removePrefix("ext:").toIntOrNull(), state, host)
        else when (LauncherWidget.byId(id)) {
            LauncherWidget.Frog -> TodoWidget(state)
            LauncherWidget.Quick -> QuickActionsWidget(state, nav)
            LauncherWidget.Focus -> FocusTile(state, nav)
            LauncherWidget.Water -> WaterTile(state)
            LauncherWidget.Budget -> BudgetTile(state)
            LauncherWidget.Exam -> ExamTile(state)
            LauncherWidget.Dump -> DumpTile(state, nav)
            LauncherWidget.Note -> NoteWidget(state)
            LauncherWidget.Agenda -> AgendaWidget()
            LauncherWidget.Habits -> HabitsWidget(state)
            null -> Unit
        }
        if (editing) {
            // Covers the widget so taps do nothing while arranging; touch and hold starts a drag.
            Box(Modifier.matchParentSize().then(dragHandle))
            Box(
                Modifier.align(Alignment.TopStart).size(48.dp).clickableRole({
                    if (id.startsWith("ext:")) id.removePrefix("ext:").toIntOrNull()?.let { host.delete(it) }
                    state.removeWidgetId(id)
                }),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(24.dp).background(AppColors.text, CircleShape), contentAlignment = Alignment.Center) {
                    AppIcon(Ic.Minus, AppColors.phone, size = 14.dp, description = "Remove widget")
                }
            }
        }
    }
}
