package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    color: Color = LoaderDefaults.color,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.5f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1400,
) {
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val stroke = d * 0.08f
        // Each ring looks the same every 180 degrees, so whole half-turns keep the loop seamless.
        arcPair(color, d / 2f - stroke / 2f, progress * 360f, stroke)
        arcPair(secondaryColor, d * 0.3f, -progress * 540f, stroke)
    }
}

private fun DrawScope.arcPair(color: Color, radius: Float, angle: Float, stroke: Float) {
    val topLeft = Offset(center.x - radius, center.y - radius)
    val arcSize = Size(radius * 2f, radius * 2f)
    for (offset in floatArrayOf(0f, 180f)) {
        drawArc(color, angle + offset, 80f, false, topLeft, arcSize, style = Stroke(stroke, cap = StrokeCap.Round))
    }
}
