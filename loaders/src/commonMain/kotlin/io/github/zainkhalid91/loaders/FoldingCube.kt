package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp

// Quarter order round the diamond, as (column, row) of a 2 x 2 grid: clockwise from top-left.
private val QuarterCol = intArrayOf(0, 1, 1, 0)
private val QuarterRow = intArrayOf(0, 0, 1, 1)

/**
 * A diamond made of four quarters that fold in one after another around the centre, hold as a
 * whole cube face, then fold away in the same order.
 */
@Composable
public fun FoldingCube(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    gap: Float = 0.06f,
    durationMillis: Int = 2400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(gap in 0f..0.3f) { "gap must be between 0 and 0.3, was $gap" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.42f) { motion ->
        // The diamond's side, so its corners just touch the bounds once rotated 45 degrees.
        val side = this.size.minDimension / 1.42f
        val quarter = side / 2f
        val inset = quarter * gap
        val tile = Size(quarter - inset * 2f, quarter - inset * 2f)
        val origin = Offset(center.x - quarter, center.y - quarter)
        onDrawBehind {
            rotate(45f) {
                for (i in 0 until 4) {
                    val t = fract(motion.progress - i * 0.1f)
                    // Fold in over the first quarter of a turn, hold, fold out, stay hidden.
                    val fold = when {
                        t < 0.2f -> FastOutSlowInEasing.transform(t / 0.2f)
                        t < 0.55f -> 1f
                        t < 0.75f -> 1f - FastOutSlowInEasing.transform((t - 0.55f) / 0.2f)
                        else -> 0f
                    }
                    if (fold <= 0.01f) continue
                    val col = QuarterCol[i]
                    val row = QuarterRow[i]
                    val topLeft = Offset(origin.x + col * quarter + inset, origin.y + row * quarter + inset)
                    // Each quarter hinges on the edge it shares with the centre of the cube.
                    val hinge = Offset(center.x, center.y)
                    val unfoldingIn = t < 0.4f
                    scale(
                        scaleX = if (unfoldingIn) 1f else fold,
                        scaleY = if (unfoldingIn) fold else 1f,
                        pivot = hinge,
                    ) {
                        // A half-folded quarter is turned away from the light, so it is darker.
                        drawRect(lerp(color, Color.Black.copy(alpha = color.alpha), 0.4f * (1f - fold)), topLeft, tile)
                    }
                }
            }
        }
    }
}
