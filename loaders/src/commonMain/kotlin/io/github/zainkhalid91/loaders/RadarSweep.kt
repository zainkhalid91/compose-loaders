package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

// Blips as (angle in degrees, distance from centre as a fraction of the radius).
private val Blips = floatArrayOf(40f, 0.62f, 165f, 0.82f, 255f, 0.4f)

/** A radar beam sweeping a scope; blips light up as the beam passes and fade before it returns. */
@Composable
public fun RadarSweep(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 2000,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.2f) { motion ->
        val r = this.size.minDimension / 2f
        val stroke = strokePx(strokeWidth, r * 0.05f)
        val scope = Stroke(stroke)
        val grid = Stroke(stroke * 0.5f)
        val beam = r - stroke
        // Transparent at the trailing edge, brightest at the leading edge 90 degrees later.
        val brush = Brush.sweepGradient(0f to Color.Transparent, 0.25f to color.copy(alpha = color.alpha * 0.55f), center = center)
        val beamTopLeft = Offset(center.x - beam, center.y - beam)
        val beamSize = Size(beam * 2f, beam * 2f)
        val faint = color.copy(alpha = color.alpha * 0.25f)
        onDrawBehind {
            drawCircle(color, r - stroke / 2f, style = scope)
            drawCircle(faint, r * 0.55f, style = grid)
            val leading = motion.progress * 360f
            rotate(leading - 90f) { drawArc(brush, 0f, 90f, true, beamTopLeft, beamSize) }
            for (i in Blips.indices step 2) {
                val angle = Blips[i]
                // How long ago the beam passed this blip, as a fraction of a turn.
                val age = fract((leading - angle) / 360f)
                val a = angle * Pi / 180f
                val at = Offset(center.x + r * Blips[i + 1] * cos(a), center.y + r * Blips[i + 1] * sin(a))
                drawCircle(color.copy(alpha = color.alpha * (1f - age)), stroke * 1.4f, at)
            }
            drawCircle(color, stroke)
        }
    }
}
