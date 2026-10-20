package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.floor
import kotlin.math.sin

/** Two blocks chasing each other around the corners of a square, tumbling as they go. */
@Composable
public fun ChasingBlocks(
    modifier: Modifier = Modifier,
    color: Color = LoaderDefaults.color,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.55f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1800,
) {
    val progress by rememberLoopProgress(durationMillis)
    Canvas(modifier.loader(size)) {
        val d = this.size.minDimension
        val block = d * 0.3f
        val travel = d - block
        val corners = arrayOf(Offset(0f, 0f), Offset(travel, 0f), Offset(travel, travel), Offset(0f, travel))
        val quarters = progress * 4f
        val k = floor(quarters).toInt()
        val f = FastOutSlowInEasing.transform(quarters - k)
        for ((start, tint) in listOf(0 to color, 2 to secondaryColor)) {
            val from = corners[(start + k) % 4]
            val to = corners[(start + k + 1) % 4]
            val scale = 1f - 0.45f * sin(Pi * f)
            val side = block * scale
            val centre = Offset(lerp(from.x, to.x, f) + block / 2f, lerp(from.y, to.y, f) + block / 2f)
            rotate(-90f * (k + f), pivot = centre) {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(centre.x - side / 2f, centre.y - side / 2f),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(side * 0.18f),
                )
            }
        }
    }
}
