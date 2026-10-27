package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.asin

/**
 * A row of dominoes topples one into the next, the last one falls flat, then the row fades
 * and stands back up. The loader is wider than it is tall; [size] sets the width.
 */
@Composable
public fun DominoFall(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size * 1.6f,
    count: Int = 5,
    durationMillis: Int = 2200,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(count in 2..10) { "count must be 2 to 10, was $count" }
    LoaderCanvas(modifier, size, size * 0.6f, durationMillis, progress, config, restPose = 0.3f) { motion ->
        val w = this.size.width
        val h = this.size.height
        // Leave room on the right for the last domino to lie flat.
        val tileHeight = h * 0.82f
        val spacing = (w - tileHeight) / (count - 1 + 0.35f)
        val tileWidth = spacing * 0.5f
        val ground = h * 0.95f
        val tile = Size(tileWidth, tileHeight)
        val corner = CornerRadius(tileWidth * 0.3f)
        // A domino leans until its top edge rests on the next one.
        val lean = asin(((spacing - tileWidth) / tileHeight).coerceIn(0f, 1f)) * 180f / Pi
        onDrawBehind {
            val p = motion.progress
            // Topple over the first 70%, rest, fade out, then stand back up while fading in.
            val visible = 1f - smoothstep(0.8f, 0.88f, p) + smoothstep(0.92f, 1f, p)
            val reset = p >= 0.9f
            for (i in 0 until count) {
                val start = i * 0.11f
                val fall = if (reset) 0f else FastOutLinearInEasing.transform(((p - start) / 0.13f).coerceIn(0f, 1f))
                val angle = fall * if (i == count - 1) 90f else lean
                val left = i * spacing
                val pivot = Offset(left + tileWidth, ground)
                rotate(angle, pivot) {
                    drawRoundRect(color.copy(alpha = color.alpha * visible), Offset(left, ground - tileHeight), tile, corner)
                }
            }
        }
    }
}
