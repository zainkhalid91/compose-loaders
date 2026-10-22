package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

// Sides of each shape in the cycle; 0 means a circle.
private val Shapes = intArrayOf(3, 4, 6, 0)
private const val Samples = 90

/** A slowly turning shape that melts from triangle to square to hexagon to circle and back. */
@Composable
public fun MorphingPolygon(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    durationMillis: Int = 900,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, size, size, durationMillis * Shapes.size, progress, config, restPose = 0.3f) { motion ->
        val radius = this.size.minDimension / 2f
        val path = Path()
        onDrawBehind {
            val cycle = motion.progress * Shapes.size
            val k = floor(cycle).toInt()
            // Hold each shape for a beat, then morph into the next one.
            val morph = FastOutSlowInEasing.transform(((cycle - k - 0.35f) / 0.65f).coerceIn(0f, 1f))
            val from = Shapes[k % Shapes.size]
            val to = Shapes[(k + 1) % Shapes.size]
            // Morphing through a circle keeps every in-between shape convex instead of lumpy.
            val via = if (morph < 0.5f) morph * 2f else (morph - 0.5f) * 2f
            val spin = motion.progress * TwoPi - Pi / 2f
            path.rewind()
            for (i in 0 until Samples) {
                val theta = i * TwoPi / Samples
                val r = radius * if (morph < 0.5f) {
                    lerp(polygonRadius(from, theta), polygonRadius(0, theta), via)
                } else {
                    lerp(polygonRadius(0, theta), polygonRadius(to, theta), via)
                }
                val x = center.x + r * cos(theta + spin)
                val y = center.y + r * sin(theta + spin)
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, color)
        }
    }
}

/** Distance from centre to the edge of a regular polygon with a vertex at angle 0, circumradius 1. */
private fun polygonRadius(sides: Int, theta: Float): Float {
    if (sides == 0) return 0.9f
    val sector = TwoPi / sides
    val local = fract(theta / sector) * sector - sector / 2f
    return cos(Pi / sides) / cos(local)
}
