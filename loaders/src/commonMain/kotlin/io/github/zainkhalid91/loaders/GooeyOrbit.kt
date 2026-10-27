package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Blobs that orbit a centre drop and stretch out from it like liquid, joining with gooey bridges
 * when close and snapping free when they drift apart. No blur or shaders: the bridges are curves,
 * so it works on every API level.
 */
@Composable
public fun GooeyOrbit(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    size: Dp = LoaderDefaults.size,
    blobs: Int = 3,
    durationMillis: Int = 2400,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(blobs in 1..6) { "blobs must be 1 to 6, was $blobs" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.2f) { motion ->
        val r = this.size.minDimension / 2f
        val core = r * 0.3f
        val blob = r * 0.17f
        val bridges = Array(blobs) { Path() }
        onDrawBehind {
            val p = motion.progress
            // Blobs drift out and back twice per loop while the whole set turns once.
            val reach = r * (0.36f + 0.42f * (0.5f - 0.5f * cos(TwoPi * p * 2f)))
            drawCircle(color, core)
            for (i in 0 until blobs) {
                val a = TwoPi * (p + i / blobs.toFloat())
                val at = Offset(center.x + reach * cos(a), center.y + reach * sin(a))
                drawCircle(color, blob, at)
                val bridge = bridges[i]
                if (metaball(bridge, center, core, at, blob)) drawPath(bridge, color)
            }
        }
    }
}

/**
 * Builds the gooey bridge between two circles into [into], after the classic metaball construction
 * (two tangent points on each circle joined by curves). Returns false when they are too far apart.
 */
private fun metaball(into: Path, c1: Offset, r1: Float, c2: Offset, r2: Float): Boolean {
    val spread = 0.5f
    val handle = 2.4f
    val d = hypot(c2.x - c1.x, c2.y - c1.y)
    val maxDistance = r1 + r2 * 2.5f
    if (d > maxDistance || d <= abs(r1 - r2)) return false
    val u1 = if (d < r1 + r2) acos(((r1 * r1 + d * d - r2 * r2) / (2 * r1 * d)).coerceIn(-1f, 1f)) else 0f
    val u2 = if (d < r1 + r2) acos(((r2 * r2 + d * d - r1 * r1) / (2 * r2 * d)).coerceIn(-1f, 1f)) else 0f
    val between = atan2(c2.y - c1.y, c2.x - c1.x)
    val maxSpread = acos(((r1 - r2) / d).coerceIn(-1f, 1f))
    val a1 = between + u1 + (maxSpread - u1) * spread
    val a2 = between - u1 - (maxSpread - u1) * spread
    val a3 = between + Pi - u2 - (Pi - u2 - maxSpread) * spread
    val a4 = between - Pi + u2 + (Pi - u2 - maxSpread) * spread
    val p1x = c1.x + r1 * cos(a1); val p1y = c1.y + r1 * sin(a1)
    val p2x = c1.x + r1 * cos(a2); val p2y = c1.y + r1 * sin(a2)
    val p3x = c2.x + r2 * cos(a3); val p3y = c2.y + r2 * sin(a3)
    val p4x = c2.x + r2 * cos(a4); val p4y = c2.y + r2 * sin(a4)
    val total = r1 + r2
    val d2 = minOf(spread * handle, hypot(p3x - p1x, p3y - p1y) / total) * minOf(1f, d * 2f / total)
    val h1 = r1 * d2
    val h2 = r2 * d2
    into.rewind()
    into.moveTo(p1x, p1y)
    into.cubicTo(
        p1x + h1 * cos(a1 - Pi / 2f), p1y + h1 * sin(a1 - Pi / 2f),
        p3x + h2 * cos(a3 + Pi / 2f), p3y + h2 * sin(a3 + Pi / 2f),
        p3x, p3y,
    )
    into.lineTo(p4x, p4y)
    into.cubicTo(
        p4x + h2 * cos(a4 - Pi / 2f), p4y + h2 * sin(a4 - Pi / 2f),
        p2x + h1 * cos(a2 + Pi / 2f), p2y + h1 * sin(a2 + Pi / 2f),
        p2x, p2y,
    )
    into.close()
    return true
}
