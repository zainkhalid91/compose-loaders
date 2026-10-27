package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.floor

// Slots of a 2 x 2 grid in clockwise order: top-left, top-right, bottom-right, bottom-left.
private val SlotCol = intArrayOf(0, 1, 1, 0)
private val SlotRow = intArrayOf(0, 0, 1, 1)

// Three tiles and a hole: the hole walks clockwise and each move slides the next tile into it.
// After four moves the tiles have rotated one place, so twelve moves bring every tile home.
private const val Moves = 12

/** A tiny sliding puzzle: three tiles take turns sliding into the empty corner. */
@Composable
public fun SlidingTiles(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.7f),
    tertiaryColor: Color = color.copy(alpha = color.alpha * 0.45f),
    size: Dp = LoaderDefaults.size,
    cornerRadius: Dp = Dp.Unspecified,
    durationMillis: Int = 350,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    // durationMillis is one slide; the loop is all twelve.
    LoaderCanvas(modifier, size, size, durationMillis * Moves, progress, config, restPose = 0.04f) { motion ->
        val cell = this.size.minDimension / 2f
        val gap = cell * 0.1f
        val tile = Size(cell - gap, cell - gap)
        val corner = CornerRadius(if (cornerRadius == Dp.Unspecified) cell * 0.2f else cornerRadius.toPx())
        // Where each tile sits before each move, and which tile moves, worked out once.
        val slots = Array(Moves + 1) { IntArray(3) }
        val mover = IntArray(Moves)
        val start = intArrayOf(0, 1, 2)
        var hole = 3
        for (m in 0..Moves) {
            for (t in 0 until 3) slots[m][t] = start[t]
            if (m == Moves) break
            val from = (hole + 1) % 4
            val moving = start.indexOf(from)
            mover[m] = moving
            start[moving] = hole
            hole = from
        }
        onDrawBehind {
            val step = motion.progress * Moves
            val m = floor(step).toInt().coerceAtMost(Moves - 1)
            val f = FastOutSlowInEasing.transform(step - m)
            for (t in 0 until 3) {
                val from = slots[m][t]
                val to = if (mover[m] == t) slots[m + 1][t] else from
                val x = lerp(SlotCol[from].toFloat(), SlotCol[to].toFloat(), f) * cell + gap / 2f
                val y = lerp(SlotRow[from].toFloat(), SlotRow[to].toFloat(), f) * cell + gap / 2f
                val tint = when (t) {
                    0 -> color
                    1 -> secondaryColor
                    else -> tertiaryColor
                }
                drawRoundRect(tint, Offset(x, y), tile, corner)
            }
        }
    }
}
