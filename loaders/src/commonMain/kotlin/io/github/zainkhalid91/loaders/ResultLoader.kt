package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.Dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.sqrt

/** Where a piece of work is. Drives [ResultLoader] and [LoadingButton]. */
public enum class LoaderState {
    /** Nothing is happening. A [ResultLoader] draws nothing; a [LoadingButton] shows its label. */
    Idle,

    /** Work is in progress: the loader runs. */
    Loading,

    /** Work finished: the loader closes into a tick. */
    Success,

    /** Work failed: the loader closes into a cross and gives a short shake. */
    Error,
}

/**
 * A loader that ends in a result instead of just disappearing. While [state] is
 * [LoaderState.Loading] it runs [loader]; on [LoaderState.Success] the loader shrinks away, a
 * ring closes round it and a tick draws itself in. [LoaderState.Error] draws a cross and shakes.
 *
 * ```
 * ResultLoader(state = when (upload) {
 *     is Uploading -> LoaderState.Loading
 *     is Uploaded -> LoaderState.Success
 *     is Failed -> LoaderState.Error
 * })
 * ```
 *
 * Any loader in the library can be the [loader]. The result plays a confirm or reject haptic
 * (turn it off with [haptics]) and a screen reader announces [successLabel] or [errorLabel].
 * In reduced motion, or when the screen first appears already finished, the result shows at once.
 *
 * @param showRing Draws the closing ring round the tick. Turn it off when the result sits on its
 *   own background, as in [LoadingButton].
 */
@Composable
public fun ResultLoader(
    state: LoaderState,
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    successColor: Color = LoaderDefaults.successColor,
    errorColor: Color = LoaderDefaults.errorColor,
    size: Dp = LoaderDefaults.size,
    strokeWidth: Dp = Dp.Unspecified,
    showRing: Boolean = true,
    successLabel: String = LoaderDefaults.successLabel,
    errorLabel: String = LoaderDefaults.errorLabel,
    haptics: Boolean = true,
    config: LoaderConfig = LocalLoaderConfig.current,
    loader: @Composable () -> Unit = {
        ExpressiveArc(color = color, size = size, strokeWidth = strokeWidth, config = config.copy(announce = false))
    },
) {
    ResultLoaderContent(
        state, modifier, color, successColor, errorColor, size, strokeWidth, showRing,
        successLabel, errorLabel, haptics, config, shakeMark = true, loader = loader,
    )
}

/** [ResultLoader], plus whether the mark shakes on error (a [LoadingButton] shakes as a whole). */
@Composable
internal fun ResultLoaderContent(
    state: LoaderState,
    modifier: Modifier,
    color: Color,
    successColor: Color,
    errorColor: Color,
    size: Dp,
    strokeWidth: Dp,
    showRing: Boolean,
    successLabel: String,
    errorLabel: String,
    haptics: Boolean,
    config: LoaderConfig,
    shakeMark: Boolean,
    loader: @Composable () -> Unit,
) {
    val instant = config.reducedMotion == ReducedMotion.On || LocalFrozenFrame.current != null
    val isResult = state == LoaderState.Success || state == LoaderState.Error
    // 0 while loading, 1 once the result is fully drawn.
    val reveal = remember { Animatable(if (isResult) 1f else 0f) }
    val shake = remember { Animatable(1f) }
    val haptic = LocalHapticFeedback.current
    var lastState by remember { mutableStateOf<LoaderState?>(null) }
    LaunchedEffect(state, instant) {
        val appearing = lastState == null
        val changed = lastState != state
        lastState = state
        if (!isResult) {
            reveal.snapTo(0f)
            shake.snapTo(1f)
            return@LaunchedEffect
        }
        // A screen that opens on a finished result shouldn't buzz or replay the animation.
        if (appearing || instant || !changed) {
            reveal.snapTo(1f)
            if (!appearing && changed && haptics) haptic.performHapticFeedback(state.haptic())
            return@LaunchedEffect
        }
        if (haptics) haptic.performHapticFeedback(state.haptic())
        coroutineScope {
            launch { reveal.animateTo(1f, tween(RevealMillis, easing = LinearEasing)) }
            if (state == LoaderState.Error && shakeMark) {
                launch {
                    shake.snapTo(0f)
                    delay(RevealMillis * 6L / 10L)
                    shake.animateTo(1f, tween(420, easing = LinearEasing))
                }
            }
        }
    }

    // Announce on the frame after appearing, as a live region only speaks when its content changes.
    var spoken by remember { mutableStateOf(!config.announce) }
    if (!spoken) LaunchedEffect(Unit) { spoken = true }
    val description = when (state) {
        LoaderState.Idle -> ""
        LoaderState.Loading -> config.label
        LoaderState.Success -> successLabel
        LoaderState.Error -> errorLabel
    }

    Box(
        modifier
            .clearAndSetSemantics {
                if (spoken) contentDescription = description
                if (state == LoaderState.Loading) progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
                if (config.announce && state != LoaderState.Idle) liveRegion = LiveRegionMode.Polite
            }
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        val loaderGone by remember { derivedStateOf { reveal.value >= LoaderOutEnd } }
        val loaderVisible = state == LoaderState.Loading || (isResult && !loaderGone)
        if (loaderVisible) {
            Box(
                Modifier.graphicsLayer {
                    val out = smoothstep(0f, LoaderOutEnd, reveal.value)
                    alpha = 1f - out
                    scaleX = 1f - 0.4f * out
                    scaleY = scaleX
                },
            ) { loader() }
        }
        if (isResult) {
            val markColor = if (state == LoaderState.Success) successColor else errorColor
            val success = state == LoaderState.Success
            Spacer(
                Modifier.matchParentSize().drawWithCache {
                    val d = this.size.minDimension
                    val stroke = strokePx(strokeWidth, d * 0.08f)
                    val ringStyle = Stroke(stroke, cap = StrokeCap.Round)
                    val inset = stroke / 2f
                    val ringTopLeft = Offset(center.x - d / 2f + inset, center.y - d / 2f + inset)
                    val ringSize = Size(d - stroke, d - stroke)
                    // Without a ring the mark can use more of the space.
                    val markScale = if (showRing) 1f else 1.35f
                    onDrawBehind {
                        val r = reveal.value
                        val wobble = (1f - shake.value)
                        val dx = sin(shake.value * Pi * 6f) * wobble * d * 0.07f
                        translate(dx) {
                            if (showRing) {
                                val ring = FastOutSlowInEasing.transform(smoothstep(0.1f, 0.55f, r))
                                val tint = lerp(color, markColor, smoothstep(0.3f, 0.6f, r))
                                if (ring > 0f) drawArc(tint, -90f, 360f * ring, false, ringTopLeft, ringSize, style = ringStyle)
                            }
                            val mark = FastOutSlowInEasing.transform(smoothstep(0.5f, 0.9f, r))
                            val pop = 1f + 0.12f * sin(Pi * smoothstep(0.8f, 1f, r))
                            scale(pop * markScale) {
                                if (success) drawTick(markColor, mark, d, stroke) else drawCross(markColor, mark, d, stroke)
                            }
                        }
                    }
                },
            )
        }
    }
}

private const val RevealMillis = 700

/** Fraction of the reveal by which the loader has shrunk away. */
private const val LoaderOutEnd = 0.25f

private fun LoaderState.haptic(): HapticFeedbackType =
    if (this == LoaderState.Success) HapticFeedbackType.Confirm else HapticFeedbackType.Reject

private fun dist(ax: Float, ay: Float, bx: Float, by: Float): Float = sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay))

/** Draws the first [t] of a tick, short leg first, as a pen would. Allocation free. */
private fun DrawScope.drawTick(color: Color, t: Float, d: Float, stroke: Float) {
    if (t <= 0f) return
    val ox = center.x - d / 2f
    val oy = center.y - d / 2f
    val ax = ox + d * 0.30f
    val ay = oy + d * 0.52f
    val bx = ox + d * 0.44f
    val by = oy + d * 0.66f
    val cx = ox + d * 0.71f
    val cy = oy + d * 0.37f
    val first = dist(ax, ay, bx, by)
    val second = dist(bx, by, cx, cy)
    val drawn = (first + second) * t
    if (drawn <= first) {
        val k = drawn / first
        drawLine(color, Offset(ax, ay), Offset(lerp(ax, bx, k), lerp(ay, by, k)), stroke, StrokeCap.Round)
    } else {
        val k = (drawn - first) / second
        drawLine(color, Offset(ax, ay), Offset(bx, by), stroke, StrokeCap.Round)
        drawLine(color, Offset(bx, by), Offset(lerp(bx, cx, k), lerp(by, cy, k)), stroke, StrokeCap.Round)
    }
}

/** Draws the first [t] of a cross, one stroke after the other. Allocation free. */
private fun DrawScope.drawCross(color: Color, t: Float, d: Float, stroke: Float) {
    if (t <= 0f) return
    val lo = 0.31f * d
    val hi = 0.69f * d
    val ox = center.x - d / 2f
    val oy = center.y - d / 2f
    val a = (t * 2f).coerceAtMost(1f)
    drawLine(color, Offset(ox + lo, oy + lo), Offset(ox + lerp(lo, hi, a), oy + lerp(lo, hi, a)), stroke, StrokeCap.Round)
    if (t > 0.5f) {
        val b = (t - 0.5f) * 2f
        drawLine(color, Offset(ox + hi, oy + lo), Offset(ox + lerp(hi, lo, b), oy + lerp(lo, hi, b)), stroke, StrokeCap.Round)
    }
}
