package com.example.productivitylauncher.ui.pip

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import kotlinx.coroutines.delay
import kotlin.random.Random

/** How Pip rests against the screen edge. */
object PipDock {
    /** Seconds of nobody touching Pip before it tucks itself away. */
    const val IdleMillis = 5_000L
    val Size = 56.dp

    /** Gap between the bubble and the screen edge while it is out. */
    val Gap = 28.dp

    /** Where the chat call-out sits: just above the bubble (which is 110dp from the bottom). */
    val CalloutBottom = 110.dp + Size + 4.dp

    /** Keeps the call-out's tail over the middle of the bubble. */
    val TailEnd = Gap + Size / 2 - 20.dp - 8.dp
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
    // Pip blinks now and then while it is out: the face closes its eyes for a moment.
    var blink by remember { mutableStateOf(false) }
    LaunchedEffect(docked) {
        blink = false
        if (!docked) {
            while (true) {
                delay(Random.nextLong(2_200L, 5_000L))
                blink = true
                delay(150L)
                blink = false
                // Sometimes a quick double blink.
                if (Random.nextInt(4) == 0) {
                    delay(160L)
                    blink = true
                    delay(130L)
                    blink = false
                }
            }
        }
    }
    val pulse by rememberInfiniteTransition(label = "dot").animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(650), RepeatMode.Reverse),
        label = "pulse",
    )
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
            AppText(if (blink) "[-_-]" else message.face, size = 12.sp, weight = FontWeight.Medium, mono = true)
        }
        if (message.nudge) {
            // Docked, the right half is off screen, so the dot moves to the left half that still shows.
            Box(
                Modifier
                    .align(if (docked) Alignment.TopStart else Alignment.TopEnd)
                    .padding(start = if (docked) 12.dp else 0.dp)
                    .size(16.dp)
                    .scale(pulse)
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

/**
 * Pip's message as a chat call-out above the bubble, like a messaging app: first three
 * bouncing "typing" dots, then the message types itself out, then the buttons appear.
 */
@Composable
fun PipCard(message: PipMessage, timeLabel: String, onDismiss: () -> Unit, onAction: (PipAction) -> Unit) {
    var typing by remember { mutableStateOf(true) }
    var shown by remember { mutableIntStateOf(0) }
    LaunchedEffect(message.text) {
        typing = true
        shown = 0
        delay(1_100L)
        typing = false
        while (shown < message.text.length) {
            shown++
            delay(24L)
        }
    }
    val done = !typing && shown >= message.text.length
    val shape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)

    // A transparent layer: tapping anywhere else closes the call-out. No dark scrim.
    Box(
        Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss),
        contentAlignment = Alignment.BottomEnd,
    ) {
        Column(
            Modifier.padding(start = 20.dp, end = 20.dp, bottom = PipDock.CalloutBottom).widthIn(max = 320.dp),
            horizontalAlignment = Alignment.End,
        ) {
            Column(
                Modifier
                    .shadow(12.dp, shape, ambientColor = Color(0x26000000), spotColor = Color(0x26000000))
                    .background(AppColors.card, shape)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = {})
                    .padding(horizontal = 16.dp, vertical = 14.dp),
            ) {
                if (typing) {
                    TypingDots(Modifier.padding(vertical = 6.dp, horizontal = 4.dp))
                } else {
                    AppText("PIP", size = 11.sp, weight = FontWeight.Bold, color = AppColors.focus, letterSpacing = 1.sp, modifier = Modifier.padding(bottom = 4.dp))
                    // The full text is laid out invisibly so the bubble keeps its size while the text types in.
                    Box {
                        AppText(message.text, size = 16.sp, lineHeight = 23.sp, modifier = Modifier.alpha(0f))
                        AppText(message.text.take(shown), size = 16.sp, lineHeight = 23.sp)
                    }
                    if (done) {
                        AppText(timeLabel, size = 11.sp, color = AppColors.muted, modifier = Modifier.align(Alignment.End).padding(top = 6.dp))
                        Row(Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            if (message.primaryLabel != null) {
                                CButton(message.primaryLabel, { onAction(message.action) }, Modifier.weight(1f), BtnKind.Accent, small = true)
                            }
                            CButton(if (message.primaryLabel != null) "Later" else "Thanks", onDismiss, Modifier.weight(1f), BtnKind.Soft, small = true)
                        }
                    }
                }
            }
            // The tail of the call-out, pointing down at Pip.
            val tail = AppColors.card
            Canvas(Modifier.padding(end = PipDock.TailEnd).size(width = 16.dp, height = 9.dp)) {
                val path = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    lineTo(size.width / 2f, size.height)
                    close()
                }
                drawPath(path, tail)
            }
        }
    }
}

/** Three dots that bounce one after another, the "typing" sign. */
@Composable
private fun TypingDots(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "typing")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 0..2) {
            val a by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(450, delayMillis = i * 150), RepeatMode.Reverse),
                label = "dot$i",
            )
            Box(Modifier.size(9.dp).alpha(a).background(AppColors.muted, CircleShape))
        }
    }
}
