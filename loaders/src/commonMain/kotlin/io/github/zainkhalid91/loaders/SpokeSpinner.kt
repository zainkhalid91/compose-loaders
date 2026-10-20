package io.github.zainkhalid91.loaders

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

/** The classic system spinner: rounded spokes whose opacity steps around the clock. */
@Composable
public fun SpokeSpinner(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    size: Dp = LoaderDefaults.size,
    spokes: Int = 12,
    durationMillis: Int = 1000,
) {
    require(spokes >= 3) { "spokes must be at least 3, was $spokes" }
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val r = this.size.minDimension / 2f
        val stroke = r * 0.16f
        val head = floor(progress * spokes).toInt()
        for (i in 0 until spokes) {
            val age = ((head - i + spokes) % spokes) / spokes.toFloat()
            rotate(i * 360f / spokes) {
                drawLine(
                    color = color.copy(alpha = color.alpha * (1f - age * 0.85f)),
                    start = Offset(center.x, center.y - r * 0.45f),
                    end = Offset(center.x, center.y - r + stroke / 2f),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
