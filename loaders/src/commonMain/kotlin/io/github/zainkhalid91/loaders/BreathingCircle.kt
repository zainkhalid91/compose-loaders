package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Layered discs that breathe like a meditation guide: in, hold, out, hold. Outer layers follow a
 * moment behind the inner ones. A calm choice for long waits.
 */
@Composable
public fun BreathingCircle(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    layers: Int = 3,
    minScale: Float = 0.45f,
    durationMillis: Int = 5000,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(layers >= 1) { "layers must be at least 1, was $layers" }
    require(minScale in 0.1f..0.95f) { "minScale must be between 0.1 and 0.95, was $minScale" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.45f) { motion ->
        val r = this.size.minDimension / 2f
        onDrawBehind {
            for (l in layers - 1 downTo 0) {
                val breath = breath(fract(motion.progress - l * 0.03f))
                val outer = (l + 1f) / layers
                val radius = r * outer * (minScale + (1f - minScale) * breath)
                drawCircle(color.copy(alpha = color.alpha * (1f - 0.75f * l / layers.coerceAtLeast(2))), radius)
            }
        }
    }
}

/** 0 to 1 over a breath: 40% in, 15% hold, 40% out, 5% rest. */
private fun breath(t: Float): Float = when {
    t < 0.4f -> FastOutSlowInEasing.transform(t / 0.4f)
    t < 0.55f -> 1f
    t < 0.95f -> 1f - FastOutSlowInEasing.transform((t - 0.55f) / 0.4f)
    else -> 0f
}
