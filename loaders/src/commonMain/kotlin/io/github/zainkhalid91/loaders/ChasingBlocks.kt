package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.floor
import kotlin.math.sin

/** Two blocks chasing each other around the corners of a square, tumbling as they go. */
@Composable
public fun ChasingBlocks(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.55f),
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1800,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0f) { motion ->
        val d = this.size.minDimension
        val block = d * 0.3f
        val travel = d - block
        onDrawBehind {
            val quarters = motion.progress * 4f
            val k = floor(quarters).toInt()
            val f = FastOutSlowInEasing.transform(quarters - k)
            block(color, 0, k, f, block, travel)
            block(secondaryColor, 2, k, f, block, travel)
        }
    }
}

/** Corners in travel order: top-left, top-right, bottom-right, bottom-left. */
private fun corner(index: Int, travel: Float): Offset = when (index % 4) {
    0 -> Offset(0f, 0f)
    1 -> Offset(travel, 0f)
    2 -> Offset(travel, travel)
    else -> Offset(0f, travel)
}

private fun DrawScope.block(tint: Color, start: Int, k: Int, f: Float, block: Float, travel: Float) {
    val from = corner(start + k, travel)
    val to = corner(start + k + 1, travel)
    val side = block * (1f - 0.45f * sin(Pi * f))
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
