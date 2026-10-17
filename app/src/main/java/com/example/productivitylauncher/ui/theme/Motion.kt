package com.example.productivitylauncher.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * One motion vocabulary for the whole app. Strong curves (the built-in ones are too weak),
 * short durations (UI stays under 300 ms), and springs for anything a finger can interrupt.
 * Compose follows the system "animator duration scale", so people who turn animations off get none.
 */
object Motion {
    /** Strong ease-out for things that enter, exit or respond to a tap. */
    val EaseOut = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)

    /** Strong ease-in-out for things that move or morph on screen. */
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)

    /** Press feedback: quick and settled. Interruptible, so a second tap never restarts from zero. */
    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.85f, stiffness = 700f)

    /** A colour or small state change. */
    fun <T> state(ms: Int = 180): FiniteAnimationSpec<T> = tween(ms, easing = EaseOut)

    /** Something appearing on a screen the person sees now and then. */
    fun <T> enter(ms: Int = 240): FiniteAnimationSpec<T> = tween(ms, easing = EaseOut)

    /** A bar or ring filling to its value. */
    fun <T> fill(ms: Int = 360): FiniteAnimationSpec<T> = tween(ms, easing = EaseOut)

    /** A small, gentle spring for drag lift and similar. Bounce stays low. */
    fun <T> lift(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.8f, stiffness = 500f)

    /** Pressed scale: subtle, 0.95 to 0.98. */
    const val PressScale = 0.97f
}
