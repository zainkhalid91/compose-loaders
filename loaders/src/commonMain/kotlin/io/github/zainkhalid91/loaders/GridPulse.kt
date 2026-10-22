package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

/** A 3 x 3 grid of tiles that shrink away and return in a diagonal sweep. */
@Composable
public fun GridPulse(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1300,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.85f) { motion ->
        val cell = this.size.minDimension / 3f
        val tile = cell * 0.82f
        onDrawBehind {
            for (row in 0 until 3) {
                for (col in 0 until 3) {
                    val t = fract(motion.progress - (row + col) * 0.08f)
                    val scale = 1f - sin(Pi * (t / 0.6f).coerceIn(0f, 1f))
                    val side = tile * scale
                    if (side <= 0.5f) continue
                    drawRoundRect(
                        color = color.copy(alpha = color.alpha * (0.35f + 0.65f * scale)),
                        topLeft = Offset(col * cell + (cell - side) / 2f, row * cell + (cell - side) / 2f),
                        size = Size(side, side),
                        cornerRadius = CornerRadius(side * 0.2f),
                    )
                }
            }
        }
    }
}
