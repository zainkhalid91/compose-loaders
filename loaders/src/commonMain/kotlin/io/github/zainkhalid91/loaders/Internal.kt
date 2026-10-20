package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.progressSemantics
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlin.math.PI
import kotlin.math.floor

internal const val TwoPi: Float = (2 * PI).toFloat()
internal const val Pi: Float = PI.toFloat()

/**
 * A value that runs from 0 to 1 every [durationMillis] and then wraps.
 *
 * Loaders read it only inside draw or graphicsLayer lambdas, so each frame
 * re-draws without recomposing.
 */
@Composable
internal fun rememberLoopProgress(durationMillis: Int, label: String = "LoaderLoop"): State<Float> {
    require(durationMillis > 0) { "durationMillis must be positive, was $durationMillis" }
    val transition = rememberInfiniteTransition(label)
    return transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis, easing = LinearEasing)),
        label = label,
    )
}

/** Indeterminate progress semantics plus a default size that the caller's modifier can override. */
internal fun Modifier.loader(size: Dp): Modifier = progressSemantics().size(size)

internal fun Modifier.loader(width: Dp, height: Dp): Modifier = progressSemantics().size(width, height)

/** Fractional part, always in [0, 1). */
internal fun fract(x: Float): Float = x - floor(x)

internal fun lerp(a: Float, b: Float, t: Float): Float = a + (b - a) * t

internal fun smoothstep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
