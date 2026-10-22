package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

/** The classic system spinner: rounded spokes whose opacity steps around the clock. */
@Composable
public fun SpokeSpinner(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    spokes: Int = 12,
    durationMillis: Int = 1000,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(spokes >= 3) { "spokes must be at least 3, was $spokes" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val r = this.size.minDimension / 2f
        val stroke = strokePx(strokeWidth, r * 0.16f)
        val inner = Offset(center.x, center.y - r * 0.45f)
        val outer = Offset(center.x, center.y - r + stroke / 2f)
        onDrawBehind {
            val head = floor(motion.progress * spokes).toInt()
            for (i in 0 until spokes) {
                val age = ((head - i + spokes) % spokes) / spokes.toFloat()
                rotate(i * 360f / spokes) {
                    drawLine(color.copy(alpha = color.alpha * (1f - age * 0.85f)), inner, outer, stroke, StrokeCap.Round)
                }
            }
        }
    }
}
