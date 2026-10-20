package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/** Five hanging balls: the outer ones swing out and knock the energy across the row. */
@Composable
public fun NewtonCradle(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1100,
) {
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val balls = 5
        // Sized so an outer ball at full swing still fits inside the bounds.
        val radius = d / 16f
        val pivotY = center.y - d * 0.25f
        val length = d * 0.42f
        val firstX = center.x - (balls - 1) * radius
        val swing = 25f * Pi / 180f * sin(progress * TwoPi)
        val thin = (d * 0.02f).coerceAtLeast(1f)
        drawLine(
            color.copy(alpha = color.alpha * 0.6f),
            Offset(firstX - radius * 2f, pivotY),
            Offset(firstX + (balls + 1) * radius * 2f - radius * 2f, pivotY),
            strokeWidth = thin * 2f,
            cap = StrokeCap.Round,
        )
        for (i in 0 until balls) {
            val angle = when (i) {
                0 -> swing.coerceAtMost(0f)
                balls - 1 -> swing.coerceAtLeast(0f)
                else -> 0f
            }
            val pivot = Offset(firstX + i * radius * 2f, pivotY)
            val ball = Offset(pivot.x + length * sin(angle), pivot.y + length * cos(angle))
            drawLine(color.copy(alpha = color.alpha * 0.45f), pivot, ball, strokeWidth = thin)
            drawCircle(color, radius * 0.94f, ball)
        }
    }
}
