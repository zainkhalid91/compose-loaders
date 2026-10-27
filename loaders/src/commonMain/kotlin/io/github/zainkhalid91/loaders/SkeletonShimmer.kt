package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A placeholder for content that is on its way: an avatar and text lines, with a highlight
 * sweeping across them. Use it where the real layout will appear.
 */
@Composable
public fun SkeletonShimmer(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    baseColor: Color = color.copy(alpha = color.alpha * 0.18f),
    highlightColor: Color = color.copy(alpha = color.alpha * 0.5f),
    width: Dp = 200.dp,
    height: Dp = 56.dp,
    lines: Int = 3,
    showAvatar: Boolean = true,
    durationMillis: Int = 1400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(lines >= 1) { "lines must be at least 1, was $lines" }
    LoaderCanvas(modifier, width, height, durationMillis, progress, config, restPose = 0.4f) { motion ->
        val w = this.size.width
        val h = this.size.height
        val avatar = if (showAvatar) h else 0f
        val left = if (showAvatar) avatar + h * 0.25f else 0f
        val lineHeight = h / (lines * 1.7f)
        val shapes = Path()
        if (showAvatar) shapes.addOval(Rect(0f, 0f, avatar, avatar))
        for (i in 0 until lines) {
            // Lines get shorter going down, like a title and the start of a paragraph.
            val lineWidth = (w - left) * (1f - 0.22f * i).coerceAtLeast(0.3f)
            val top = i * lineHeight * 1.7f + lineHeight * 0.35f
            shapes.addRoundRect(RoundRect(left, top, left + lineWidth, top + lineHeight, CornerRadius(lineHeight / 2f)))
        }
        val band = w * 0.45f
        val shine = Brush.linearGradient(
            listOf(Color.Transparent, highlightColor, Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(band, h * 0.35f),
        )
        val shineSize = Size(band, h)
        onDrawBehind {
            drawPath(shapes, baseColor)
            clipPath(shapes) {
                translate(left = -band + motion.progress * (w + band * 2f) - band / 2f) { drawRect(shine, Offset.Zero, shineSize) }
            }
        }
    }
}
