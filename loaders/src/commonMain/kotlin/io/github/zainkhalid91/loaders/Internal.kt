package io.github.zainkhalid91.loaders

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor

internal const val TwoPi: Float = (2 * PI).toFloat()
internal const val Pi: Float = PI.toFloat()

/** One reduced-motion opacity breath, in nanoseconds. Slow on purpose: it should calm, not flash. */
private const val PulseNanos = 2_400_000_000L

/**
 * The animated state of one loader. Draw code reads [progress] (0 to 1, wrapping) and the layer
 * reads [alpha]; both are primitive float states, so reading them allocates nothing.
 */
@Stable
internal class LoaderMotion(initial: Float) {
    var progress: Float by mutableFloatStateOf(initial)
    var alpha: Float by mutableFloatStateOf(1f)
}

/**
 * Drives a loader from the frame clock rather than an infinite transition, which gives three things
 * an infinite transition can't: [LoaderConfig.speed] changes without a jump, pausing keeps the
 * current frame, and a system animator scale of 0 never freezes the loader (a frozen loader looks
 * like a hung app). A non-zero system scale is still honoured, so 2x slows loaders down as expected.
 *
 * @param restPose the frame shown in reduced motion; pick one where the loader is easy to recognise.
 */
@Composable
internal fun rememberLoaderMotion(durationMillis: Int, config: LoaderConfig, restPose: Float = 0.15f): LoaderMotion {
    require(durationMillis > 0) { "durationMillis must be positive, was $durationMillis" }
    val motion = remember { LoaderMotion(restPose) }
    LaunchedEffect(durationMillis, config.speed, config.isRunning, config.reducedMotion, config.reverse, restPose) {
        if (!config.isRunning) {
            motion.alpha = 1f
            return@LaunchedEffect
        }
        val systemScale = coroutineContext[MotionDurationScale]
        val periodNanos = durationMillis * 1_000_000.0 / config.speed
        val direction = if (config.reverse) -1f else 1f
        var last = -1L
        var pulse = 0L
        // One lambda for the whole run, so the loop itself doesn't allocate per frame.
        val onFrame: (Long) -> Unit = { now ->
            val dt = if (last < 0L) 0L else now - last
            last = now
            val scale = systemScale?.scaleFactor ?: 1f
            val reduced = when (config.reducedMotion) {
                ReducedMotion.On -> true
                ReducedMotion.Off -> false
                ReducedMotion.Auto -> scale == 0f
            }
            if (reduced) {
                pulse = (pulse + dt) % PulseNanos
                motion.progress = restPose
                motion.alpha = 0.6f + 0.4f * cos(TwoPi * pulse / PulseNanos)
            } else {
                motion.alpha = 1f
                val stretch = if (scale > 0f) scale else 1f
                motion.progress = fract(motion.progress + direction * (dt / (periodNanos * stretch)).toFloat())
            }
        }
        while (true) withFrameNanos(onFrame)
    }
    return motion
}

/**
 * Size, accessibility and the reduced-motion layer for one loader. The caller's modifier comes first,
 * so a size set there wins over the default.
 */
@Composable
internal fun Modifier.loader(width: Dp, height: Dp, motion: LoaderMotion, config: LoaderConfig, progress: Float?): Modifier {
    // A live region only speaks when its content changes, so the label arrives one frame late
    // to make the loader announce itself when it appears.
    var spoken by remember { mutableStateOf(!config.announce) }
    if (!spoken) LaunchedEffect(Unit) { spoken = true }
    val description = if (spoken) config.label else ""
    return semantics(mergeDescendants = true) {
        contentDescription = description
        progressBarRangeInfo = if (progress == null) {
            ProgressBarRangeInfo.Indeterminate
        } else {
            ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f)
        }
        if (config.announce) liveRegion = LiveRegionMode.Polite
    }
        .size(width, height)
        .graphicsLayer { alpha = motion.alpha }
}

/**
 * The shape shared by most loaders: a [Spacer] whose drawing objects (strokes, paths, brushes) are
 * built once per size in [onBuild] and reused every frame. Only `motion.progress` changes per frame.
 */
@Composable
internal fun LoaderCanvas(
    modifier: Modifier,
    width: Dp,
    height: Dp,
    durationMillis: Int,
    progress: Float?,
    config: LoaderConfig,
    restPose: Float = 0.15f,
    onBuild: CacheDrawScope.(motion: LoaderMotion) -> DrawResult,
) {
    val motion = rememberLoaderMotion(durationMillis, config, restPose)
    Spacer(modifier.loader(width, height, motion, config, progress).drawWithCache { onBuild(motion) })
}

/** Centre of the drawing area while building cached objects, matching DrawScope.center. */
internal val CacheDrawScope.center: Offset get() = size.center

/** Converts an optional stroke width, falling back to a fraction of the loader's size. */
internal fun CacheDrawScope.strokePx(strokeWidth: Dp, fallback: Float): Float =
    if (strokeWidth == Dp.Unspecified) fallback else strokeWidth.toPx()

/** Fractional part, always in [0, 1). */
internal fun fract(x: Float): Float = x - floor(x)

internal fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

internal fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
