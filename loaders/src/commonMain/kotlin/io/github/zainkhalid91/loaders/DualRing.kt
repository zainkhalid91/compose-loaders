package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

/** Two pairs of arcs on nested rings, turning in opposite directions at different speeds. */
@Composable
public fun DualRing(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.5f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config) { motion ->
        val d = this.size.minDimension
        val stroke = strokePx(strokeWidth, d * 0.08f)
        val style = Stroke(stroke, cap = StrokeCap.Round)
        val outer = d / 2f - stroke / 2f
        val inner = d * 0.3f
        onDrawBehind {
            // Each ring looks the same every 180 degrees, so whole half-turns keep the loop seamless.
            arcPair(color, outer, motion.progress * 360f, style)
            arcPair(secondaryColor, inner, -motion.progress * 540f, style)
        }
    }
}

private fun DrawScope.arcPair(color: Color, radius: Float, angle: Float, style: Stroke) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)
    drawArc(color, angle, 80f, false, topLeft, arcSize, style = style)
    drawArc(color, angle + 180f, 80f, false, topLeft, arcSize, style = style)
}
