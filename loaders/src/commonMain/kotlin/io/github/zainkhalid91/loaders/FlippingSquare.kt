package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

// Unit square corners, projected every frame into a reused path.
private val CornerX = floatArrayOf(-1f, 1f, 1f, -1f)
private val CornerY = floatArrayOf(-1f, -1f, 1f, 1f)

/** A tile that flips over its horizontal axis, then over its vertical axis, in 3D. */
@Composable
public fun FlippingSquare(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 1200,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    // The 3D turn is projected by hand rather than with graphicsLayer, whose per-frame updates allocate.
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0f) { motion ->
        val half = this.size.minDimension * 0.31f
        // Rounded corners: shrink the square, then stroke it with a round join.
        val corner = half * 0.2f
        val inner = half - corner
        val camera = half * 7f
        val tile = Path()
        val edge = Stroke(corner * 2f, join = StrokeJoin.Round)
        onDrawBehind {
            val turns = motion.progress * 2f
            val x = FastOutSlowInEasing.transform(turns.coerceAtMost(1f)) * Pi
            val y = FastOutSlowInEasing.transform((turns - 1f).coerceIn(0f, 1f)) * Pi
            tile.rewind()
            for (i in 0 until 4) {
                // Rotate about X, then about Y, then project with a simple pinhole camera.
                val px = CornerX[i] * inner
                val py = CornerY[i] * inner * cos(x)
                val pz = CornerY[i] * inner * sin(x)
                val rx = px * cos(y) + pz * sin(y)
                val rz = -px * sin(y) + pz * cos(y)
                val scale = camera / (camera - rz)
                val sx = center.x + rx * scale
                val sy = center.y + py * scale
                if (i == 0) tile.moveTo(sx, sy) else tile.lineTo(sx, sy)
            }
            tile.close()
            // Darken the tile while it is edge-on so the turn reads as depth.
            val facing = abs(cos(x)) * abs(cos(y))
            val shade = lerp(color, Color.Black.copy(alpha = color.alpha), 0.45f * (1f - facing))
            drawPath(tile, shade)
            drawPath(tile, shade, style = edge)
        }
    }
}
