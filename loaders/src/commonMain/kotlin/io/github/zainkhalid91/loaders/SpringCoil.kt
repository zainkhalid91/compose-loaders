package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import kotlin.math.cos

/**
 * A weight bouncing on a spring hung from a bar; the coils bunch up and spread out as it moves.
 */
@Composable
public fun SpringCoil(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    weightColor: Color = color,
    size: Dp = LoaderDefaults.size,
    coils: Int = 6,
    stretch: Float = 0.35f,
    durationMillis: Int = 1100,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(coils in 2..16) { "coils must be 2 to 16, was $coils" }
    require(stretch in 0.05f..0.6f) { "stretch must be between 0.05 and 0.6, was $stretch" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.25f) { motion ->
        val d = this.size.minDimension
        val wire = strokePx(strokeWidth, d * 0.035f)
        val barY = d * 0.06f
        val halfWidth = d * 0.16f
        val weight = Size(d * 0.32f, d * 0.2f)
        val weightCorner = CornerRadius(d * 0.04f)
        val rest = d * 0.48f
        val barStart = Offset(center.x - d * 0.3f, barY)
        val barEnd = Offset(center.x + d * 0.3f, barY)
        onDrawBehind {
            // Starts stretched at the bottom of its bounce; cosine keeps the loop seamless.
            val length = rest * (1f + stretch * cos(TwoPi * motion.progress)) / (1f + stretch) * 1.05f
            drawLine(color, barStart, barEnd, wire * 1.4f, StrokeCap.Round)
            // Zig-zag from the bar down to the weight, one line per half coil.
            val top = barY + wire
            val bottom = top + length
            val segments = coils * 2
            var x0 = center.x
            var y0 = top
            for (i in 1..segments) {
                val x1 = if (i == segments) center.x else center.x + if (i % 2 == 1) halfWidth else -halfWidth
                val y1 = top + length * i / segments
                drawLine(color, Offset(x0, y0), Offset(x1, y1), wire, StrokeCap.Round)
                x0 = x1
                y0 = y1
            }
            drawRoundRect(weightColor, Offset(center.x - weight.width / 2f, bottom), weight, weightCorner)
        }
    }
}
