package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A single pendulum swinging from a pivot, leaving fading afterimages behind its bob,
 * fastest at the bottom of the swing.
 */
@Composable
public fun Pendulum(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    swingDegrees: Float = 50f,
    trail: Int = 5,
    durationMillis: Int = 1600,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(swingDegrees in 5f..80f) { "swingDegrees must be between 5 and 80, was $swingDegrees" }
    require(trail >= 0) { "trail must not be negative, was $trail" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.1f) { motion ->
        val d = this.size.minDimension
        val bob = d * 0.11f
        val pivot = Offset(center.x, d * 0.12f)
        // Long enough to fill the space, short enough that the bob stays inside at full swing.
        val amplitude = swingDegrees * Pi / 180f
        val length = minOf(d * 0.88f - bob - pivot.y, (d / 2f - bob) / sin(amplitude))
        val rod = strokePx(strokeWidth, d * 0.035f)
        onDrawBehind {
            // Afterimages first, oldest and faintest at the back.
            for (j in trail downTo 1) {
                val angle = amplitude * cos(TwoPi * (motion.progress - j * 0.018f))
                drawCircle(
                    color.copy(alpha = color.alpha * 0.4f * (1f - j / (trail + 1f))),
                    bob * (1f - 0.08f * j),
                    Offset(pivot.x + length * sin(angle), pivot.y + length * cos(angle)),
                )
            }
            val angle = amplitude * cos(TwoPi * motion.progress)
            val at = Offset(pivot.x + length * sin(angle), pivot.y + length * cos(angle))
            drawLine(color.copy(alpha = color.alpha * 0.7f), pivot, at, rod, StrokeCap.Round)
            drawCircle(color, rod * 1.2f, pivot)
            drawCircle(color, bob, at)
        }
    }
}
