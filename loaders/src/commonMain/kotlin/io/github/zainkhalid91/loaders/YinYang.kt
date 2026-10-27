package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp

/**
 * A yin-yang that turns half a revolution, pauses, and turns again, so it eases rather than spins.
 * Both halves are separate shapes, so translucent colours never overlap.
 */
@Composable
public fun YinYang(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.3f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 2400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0f) { motion ->
        val r = this.size.minDimension / 2f
        val c = center
        fun disc(at: Offset, radius: Float) = Path().apply { addOval(Rect(at, radius)) }
        val whole = disc(c, r)
        val leftHalf = Path().apply {
            arcTo(Rect(c, r), 90f, 180f, true)
            close()
        }
        val topBulge = disc(Offset(c.x, c.y - r / 2f), r / 2f)
        val bottomBulge = disc(Offset(c.x, c.y + r / 2f), r / 2f)
        val topDot = disc(Offset(c.x, c.y - r / 2f), r * 0.14f)
        val bottomDot = disc(Offset(c.x, c.y + r / 2f), r * 0.14f)
        // Dark half: left half plus the top bulge, minus the bottom bulge, with its dot cut out
        // and the opposite dot added.
        val dark = Path.combine(PathOperation.Union, leftHalf, topBulge)
            .let { Path.combine(PathOperation.Difference, it, bottomBulge) }
            .let { Path.combine(PathOperation.Difference, it, topDot) }
            .let { Path.combine(PathOperation.Union, it, bottomDot) }
        val light = Path.combine(PathOperation.Difference, whole, dark)
        onDrawBehind {
            // Two half turns per loop, each eased, with a pause between them.
            val half = motion.progress * 2f
            val k = if (half < 1f) 0f else 1f
            val turn = (k + FastOutSlowInEasing.transform(((half - k) / 0.7f).coerceAtMost(1f))) * 180f
            rotate(turn, c) {
                drawPath(dark, color)
                drawPath(light, secondaryColor)
            }
        }
    }
}
