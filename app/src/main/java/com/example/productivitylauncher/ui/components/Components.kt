package com.example.productivitylauncher.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.productivitylauncher.ui.theme.AppColors
import com.example.productivitylauncher.ui.theme.Inter

/** Shapes: softened from the first Ceramic draft (10 / 16 / 22 / 16) after device feedback. */
val RadiusSm = RoundedCornerShape(10.dp)
val RadiusMd = RoundedCornerShape(16.dp)
val RadiusLg = RoundedCornerShape(22.dp)
val RadiusPill = RoundedCornerShape(16.dp)

fun Modifier.sizeCompat(s: Dp): Modifier = this.then(Modifier.size(s))

/** The only text composable screens should use, so font and colours stay consistent. */
@Composable
fun AppText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    weight: FontWeight = FontWeight.Normal,
    color: Color = AppColors.text,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    mono: Boolean = false,
    strike: Boolean = false,
) {
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = if (mono) FontFamily.Monospace else Inter,
        letterSpacing = letterSpacing,
        textAlign = align,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textDecoration = if (strike) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
    )
}

/** Small uppercase caption used above sections. */
@Composable
fun Caption(text: String, modifier: Modifier = Modifier, color: Color = AppColors.muted) {
    AppText(text.uppercase(), modifier, size = 12.sp, weight = FontWeight.Bold, color = color, letterSpacing = 1.5.sp)
}

@Composable
fun Rule(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(AppColors.line))
}

/** A soft white (or dark) card with the Ceramic shadow. */
@Composable
fun CeramicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RadiusLg,
    color: Color = AppColors.card,
    padding: PaddingValues = PaddingValues(22.dp),
    borderColor: Color? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val base = modifier
        .shadow(6.dp, shape, clip = false, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
        .background(color, shape)
        .then(if (borderColor != null) Modifier.border(2.dp, borderColor, shape) else Modifier)
    val clickable = if (onClick != null) base.then(Modifier.clickableRole(onClick)) else base
    Column(clickable.padding(padding), verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
}

fun Modifier.clickableRole(onClick: () -> Unit, role: Role = Role.Button): Modifier =
    this.then(Modifier.clickable(role = role, onClick = onClick))

enum class BtnKind { Primary, Accent, Soft, Gate }

@Composable
fun CButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: BtnKind = BtnKind.Primary,
    enabled: Boolean = true,
    small: Boolean = false,
    leading: Ic? = null,
) {
    val bg = when (kind) {
        BtnKind.Primary -> AppColors.primary
        BtnKind.Accent -> AppColors.focus
        BtnKind.Soft -> AppColors.card
        BtnKind.Gate -> AppColors.gate
    }
    val fg = when (kind) {
        BtnKind.Primary -> AppColors.onPrimary
        BtnKind.Accent -> AppColors.onFocus
        BtnKind.Soft -> AppColors.text
        BtnKind.Gate -> AppColors.onGate
    }
    val shape = if (small) RoundedCornerShape(12.dp) else RadiusMd
    Row(
        modifier
            .alpha(if (enabled) 1f else 0.45f)
            .then(if (kind == BtnKind.Soft) Modifier.shadow(4.dp, shape, ambientColor = Color(0x14000000), spotColor = Color(0x14000000)) else Modifier)
            .background(bg, shape)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier)
            .heightIn(min = if (small) 44.dp else 56.dp)
            .padding(horizontal = if (small) 18.dp else 22.dp, vertical = if (small) 10.dp else 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            AppIcon(leading, fg, size = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        AppText(text, size = if (small) 14.sp else 16.sp, weight = FontWeight.SemiBold, color = fg, align = TextAlign.Center)
    }
}

/** Plain underlined-style text button, for quiet actions like Continue. */
@Composable
fun TextLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    Box(
        modifier
            .alpha(if (enabled) 1f else 0.5f)
            .heightIn(min = 44.dp)
            .then(if (enabled) Modifier.clickableRole(onClick) else Modifier)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text, size = 15.sp, weight = FontWeight.Medium, color = AppColors.muted)
    }
}

@Composable
fun IconButtonLarge(ic: Ic, description: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(48.dp)
            .shadow(4.dp, RoundedCornerShape(14.dp), ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .background(AppColors.card, RoundedCornerShape(14.dp))
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) {
        AppIcon(ic, AppColors.text, size = 20.dp)
    }
}

/** Page title row with an optional back button and trailing action. */
@Composable
fun PageHeader(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
) {
    Row(
        modifier.fillMaxWidth().padding(start = 28.dp, end = 28.dp, top = 12.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) IconButtonLarge(Ic.Back, "Back", onBack)
        AppText(title, Modifier.weight(1f), size = if (onBack != null) 24.sp else 32.sp, weight = FontWeight.SemiBold, letterSpacing = (-0.8).sp, maxLines = 1)
        if (trailing != null) trailing()
    }
}

@Composable
fun Chip(text: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .heightIn(min = 44.dp)
            .shadow(3.dp, RadiusPill, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
            .background(if (selected) AppColors.primary else AppColors.card, RadiusPill)
            .clickableRole(onClick, Role.Tab)
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppText(text, size = 14.sp, weight = FontWeight.SemiBold, color = if (selected) AppColors.onPrimary else AppColors.muted)
    }
}

/** Segmented control, e.g. Auto / Light / Dark. */
@Composable
fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth().background(AppColors.line, RadiusPill).padding(3.dp)) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(if (on) AppColors.card else Color.Transparent, RadiusPill)
                    .clickableRole({ onSelect(i) }, Role.Tab),
                contentAlignment = Alignment.Center,
            ) {
                AppText(label, size = 13.sp, weight = FontWeight.SemiBold, color = if (on) AppColors.text else AppColors.muted)
            }
        }
    }
}

@Composable
fun Toggle(on: Boolean, onChange: (Boolean) -> Unit, description: String) {
    Box(
        Modifier
            .size(width = 52.dp, height = 32.dp)
            .background(if (on) AppColors.focus else AppColors.dot, RoundedCornerShape(16.dp))
            .semantics { contentDescription = description }
            .clickable(role = Role.Switch) { onChange(!on) }
            .padding(3.dp),
        contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(Modifier.size(26.dp).background(if (on) AppColors.onFocus else AppColors.card, CircleShape))
    }
}

/** Round check used by the frog and habits. */
@Composable
fun CheckCircle(checked: Boolean, size: Dp = 34.dp) {
    Box(
        Modifier
            .size(size)
            .then(
                if (checked) Modifier.background(AppColors.focus, CircleShape)
                else Modifier.border(BorderStroke(2.5.dp, AppColors.focus), CircleShape),
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) AppIcon(Ic.Check, AppColors.onFocus, size = size * 0.52f)
    }
}

/** Round "- 5 +" control. */
@Composable
fun Stepper(value: String, onMinus: () -> Unit, onPlus: () -> Unit, modifier: Modifier = Modifier, valueWidth: Dp = 72.dp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StepBtn(Ic.Minus, "Less", onMinus)
        AppText(value, Modifier.width(valueWidth), size = 17.sp, weight = FontWeight.SemiBold, align = TextAlign.Center)
        StepBtn(Ic.Plus, "More", onPlus)
    }
}

@Composable
private fun StepBtn(ic: Ic, description: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .background(AppColors.phone, CircleShape)
            .semantics { contentDescription = description }
            .clickableRole(onClick),
        contentAlignment = Alignment.Center,
    ) { AppIcon(ic, AppColors.text, size = 18.dp) }
}

/** The round-square letter icon used for apps. Gated apps use the terracotta colour. */
@Composable
fun AppBadge(letter: String, gated: Boolean, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val fontSize = with(LocalDensity.current) { (size.toPx() * 0.4f).toSp() }
    Box(
        modifier
            .size(size)
            .background(if (gated) AppColors.gate else AppColors.phone, RoundedCornerShape(size * 0.24f)),
        contentAlignment = Alignment.Center,
    ) {
        AppText(letter, size = fontSize, weight = FontWeight.SemiBold, color = if (gated) AppColors.onGate else AppColors.text)
    }
}

/** "GATED" tag shown next to gated apps. Red is never the only signal. */
@Composable
fun GatedTag(modifier: Modifier = Modifier) {
    Row(
        modifier.background(AppColors.gate.copy(alpha = 0.16f), RoundedCornerShape(8.dp)).padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AppIcon(Ic.Lock, AppColors.gate, size = 11.dp)
        AppText("GATED", size = 10.sp, weight = FontWeight.ExtraBold, color = AppColors.gate, letterSpacing = 1.5.sp)
    }
}

/** A text input in the Ceramic style. */
@Composable
fun AppField(
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    onDone: (() -> Unit)? = null,
    fill: Color = AppColors.phone,
    mono: Boolean = false,
    center: Boolean = false,
) {
    BasicTextField(
        value = value,
        onValueChange = onChange,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = AppColors.text,
            fontSize = 16.sp,
            fontFamily = if (mono) FontFamily.Monospace else Inter,
            textAlign = if (center) TextAlign.Center else TextAlign.Start,
        ),
        cursorBrush = SolidColor(AppColors.text),
        keyboardOptions = KeyboardOptions(imeAction = if (onDone != null) ImeAction.Done else ImeAction.Default),
        keyboardActions = KeyboardActions(onDone = { onDone?.invoke() }),
        modifier = modifier.fillMaxWidth(),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().heightIn(min = 52.dp).background(fill, RoundedCornerShape(14.dp)).padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = if (center) Alignment.Center else Alignment.CenterStart,
            ) {
                if (value.isEmpty()) AppText(placeholder, color = AppColors.muted, mono = mono)
                inner()
            }
        },
    )
}

/** Settings row: title, optional subtitle and a trailing slot. */
@Composable
fun SettingRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    titleColor: Color = AppColors.text,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .then(if (onClick != null) Modifier.clickableRole(onClick) else Modifier)
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            AppText(title, size = 16.sp, weight = FontWeight.Medium, color = titleColor)
            if (subtitle != null) AppText(subtitle, size = 13.sp, color = AppColors.muted, lineHeight = 18.sp)
        }
        trailing()
    }
}

/** Value text with a chevron, used as the trailing part of a navigation row. */
@Composable
fun ValueChevron(value: String, color: Color = AppColors.muted) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (value.isNotEmpty()) AppText(value, size = 15.sp, weight = FontWeight.Medium, color = color)
        AppIcon(Ic.Chevron, color, size = 18.dp)
    }
}

@Composable
fun SettingsGroup(label: String, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Caption(label, Modifier.padding(start = 16.dp))
        Column(
            Modifier
                .fillMaxWidth()
                .shadow(6.dp, RadiusLg, ambientColor = Color(0x14000000), spotColor = Color(0x14000000))
                .background(AppColors.card, RadiusLg)
                .padding(horizontal = 22.dp),
            content = content,
        )
    }
}

@Composable
fun Divider() {
    Box(Modifier.fillMaxWidth().height(1.dp).background(AppColors.line))
}
