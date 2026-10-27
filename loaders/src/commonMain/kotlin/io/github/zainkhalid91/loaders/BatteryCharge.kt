package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp

/**
 * A battery charging: cells light up one by one from empty to full, hold, then the battery drains
 * and starts again. The loader is wider than it is tall; [size] sets the width.
 */
@Composable
public fun BatteryCharge(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    cellColor: Color = color,
    size: Dp = LoaderDefaults.size * 1.4f,
    cells: Int = 4,
    showBolt: Boolean = true,
    durationMillis: Int = 2400,
    strokeWidth: Dp = Dp.Unspecified,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(cells in 1..10) { "cells must be 1 to 10, was $cells" }
    LoaderCanvas(modifier, size, size * 0.55f, durationMillis, progress, config, restPose = 0.55f) { motion ->
        val w = this.size.width
        val h = this.size.height
        val stroke = strokePx(strokeWidth, h * 0.09f)
        val nub = w * 0.07f
        val bodySize = Size(w - nub - stroke, h - stroke)
        val bodyTopLeft = Offset(stroke / 2f, stroke / 2f)
        val bodyCorner = CornerRadius(h * 0.18f)
        val shell = Stroke(stroke)
        val nubTopLeft = Offset(bodyTopLeft.x + bodySize.width, h * 0.32f)
        val nubSize = Size(nub, h * 0.36f)
        val pad = stroke * 1.3f
        val innerWidth = bodySize.width - pad * 2f
        val cellGap = innerWidth * 0.06f
        val cellWidth = (innerWidth - cellGap * (cells - 1)) / cells
        val cellSize = Size(cellWidth, bodySize.height - pad * 2f)
        val cellCorner = CornerRadius(h * 0.06f)
        val bolt = Path().apply {
            val cx = bodyTopLeft.x + bodySize.width / 2f
            val cy = h / 2f
            val s = h * 0.3f
            moveTo(cx + s * 0.15f, cy - s)
            lineTo(cx - s * 0.45f, cy + s * 0.1f)
            lineTo(cx - s * 0.02f, cy + s * 0.1f)
            lineTo(cx - s * 0.15f, cy + s)
            lineTo(cx + s * 0.45f, cy - s * 0.1f)
            lineTo(cx + s * 0.02f, cy - s * 0.1f)
            close()
        }
        onDrawBehind {
            val p = motion.progress
            drawRoundRect(color, bodyTopLeft, bodySize, bodyCorner, style = shell)
            drawRoundRect(color, nubTopLeft, nubSize, cellCorner)
            // Fill over 75%, hold full, then drain quickly over the last 10%.
            val drain = 1f - smoothstep(0.9f, 1f, p)
            for (i in 0 until cells) {
                val lit = ((p / 0.75f) * cells - i).coerceIn(0f, 1f) * drain
                if (lit <= 0f) continue
                drawRoundRect(
                    cellColor.copy(alpha = cellColor.alpha * lit),
                    Offset(bodyTopLeft.x + pad + i * (cellWidth + cellGap), bodyTopLeft.y + pad),
                    cellSize,
                    cellCorner,
                )
            }
            if (showBolt) drawPath(bolt, color.copy(alpha = color.alpha * (0.35f + 0.65f * (1f - drain * smoothstep(0f, 0.75f, p)))))
        }
    }
}
