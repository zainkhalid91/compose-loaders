package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.Dp
import kotlin.math.sin

/**
 * A chat bubble that gently breathes while its dots light up one after another, like someone typing.
 * The loader is wider than it is tall; [size] sets the width.
 */
@Composable
public fun TypingBubble(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    dotColor: Color = Color.White,
    size: Dp = LoaderDefaults.size * 1.5f,
    dotCount: Int = 3,
    showTail: Boolean = true,
    durationMillis: Int = 1400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(dotCount >= 1) { "dotCount must be at least 1, was $dotCount" }
    LoaderCanvas(modifier, size, size * 0.66f, durationMillis, progress, config, restPose = 0.1f) { motion ->
        val w = this.size.width
        val h = this.size.height
        val tail = h * 0.14f
        val bubbleSize = Size(w - tail, h - tail)
        val bubbleTopLeft = Offset(tail, 0f)
        val corner = CornerRadius(bubbleSize.height / 2f)
        val dot = bubbleSize.height * 0.11f
        val gap = dot * 3.2f
        val firstX = bubbleTopLeft.x + bubbleSize.width / 2f - gap * (dotCount - 1) / 2f
        val dotY = bubbleSize.height / 2f
        val pivot = Offset(bubbleTopLeft.x + bubbleSize.width / 2f, bubbleSize.height / 2f)
        onDrawBehind {
            val p = motion.progress
            scale(1f + 0.03f * sin(p * TwoPi), pivot = pivot) {
                drawRoundRect(color, bubbleTopLeft, bubbleSize, corner)
                if (showTail) {
                    drawCircle(color, tail * 0.9f, Offset(tail * 1.6f, h - tail * 1.9f))
                    drawCircle(color, tail * 0.5f, Offset(tail * 0.5f, h - tail * 0.5f))
                }
                for (i in 0 until dotCount) {
                    // Each dot brightens and rises a little in turn, then rests dim.
                    val t = fract(p - i * 0.18f)
                    val lift = if (t < 0.4f) sin(t / 0.4f * Pi) else 0f
                    drawCircle(
                        dotColor.copy(alpha = dotColor.alpha * (0.35f + 0.65f * lift)),
                        dot,
                        Offset(firstX + i * gap, dotY - lift * dot * 0.8f),
                    )
                }
            }
        }
    }
}
