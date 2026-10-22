package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

/** A row of dots that hop one after another and rest on the baseline between hops. */
@Composable
public fun BouncingDots(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    dotCount: Int = 3,
    durationMillis: Int = 1200,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(dotCount >= 1) { "dotCount must be at least 1, was $dotCount" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.25f) { motion ->
        // Dots of diameter D separated by gaps of D / 2 fill the width.
        val diameter = this.size.width / (dotCount + (dotCount - 1) / 2f)
        val radius = diameter / 2f
        val amplitude = this.size.height * 0.3f
        val baseline = center.y + amplitude / 2f
        onDrawBehind {
            for (i in 0 until dotCount) {
                val t = fract(motion.progress - i * 0.12f)
                val jump = if (t < 0.5f) sin(t * TwoPi) else 0f
                drawCircle(
                    color = color.copy(alpha = color.alpha * (0.5f + 0.5f * jump)),
                    radius = radius,
                    center = Offset(radius + i * diameter * 1.5f, baseline - jump * amplitude),
                )
            }
        }
    }
}
