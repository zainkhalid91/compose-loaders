package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

// Signal strength over one loop, as a fraction of full: it wanders like a real connection.
private val Strength = floatArrayOf(0.25f, 0.6f, 1f, 0.75f, 0.4f, 0.9f, 0.55f, 0.15f)

/**
 * Bottom-aligned bars of rising height, like a phone's signal meter, with the strength
 * wandering up and down as if the connection is being found.
 */
@Composable
public fun SignalBars(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = color.copy(alpha = color.alpha * 0.2f),
    size: Dp = LoaderDefaults.size,
    bars: Int = 4,
    durationMillis: Int = 3200,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(bars in 2..8) { "bars must be 2 to 8, was $bars" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.33f) { motion ->
        val d = this.size.minDimension
        val barWidth = d / (bars + (bars - 1) * 0.4f)
        val corner = CornerRadius(barWidth * 0.25f)
        onDrawBehind {
            val step = motion.progress * Strength.size
            val k = floor(step).toInt() % Strength.size
            val f = FastOutSlowInEasing.transform(step - floor(step))
            val level = lerp(Strength[k], Strength[(k + 1) % Strength.size], f) * bars
            for (i in 0 until bars) {
                val barHeight = d * (i + 1f) / bars
                val topLeft = Offset(i * barWidth * 1.4f, d - barHeight)
                val barSize = Size(barWidth, barHeight)
                drawRoundRect(trackColor, topLeft, barSize, corner)
                val lit = (level - i).coerceIn(0f, 1f)
                if (lit > 0f) drawRoundRect(color.copy(alpha = color.alpha * lit), topLeft, barSize, corner)
            }
        }
    }
}
