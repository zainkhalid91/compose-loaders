package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlin.math.abs
import kotlin.math.cos

/**
 * A chomping mouth eating a row of dots that keeps sliding in.
 * The loader is twice as wide as it is tall; [size] sets the width.
 */
@Composable
public fun PacDots(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    dotColor: Color = color.copy(alpha = color.alpha * 0.6f),
    size: Dp = LoaderDefaults.size * 2,
    dotCount: Int = 4,
    durationMillis: Int = 500,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(dotCount >= 1) { "dotCount must be at least 1, was $dotCount" }
    LoaderCanvas(modifier, size, size / 2, durationMillis, progress, config, restPose = 0f) { motion ->
        val radius = this.size.height * 0.42f
        val mouthCentre = Offset(radius + this.size.height * 0.08f, center.y)
        val spacing = (this.size.width - mouthCentre.x) / (dotCount + 0.5f)
        val dot = radius * 0.22f
        val topLeft = Offset(mouthCentre.x - radius, mouthCentre.y - radius)
        val mouthSize = Size(radius * 2f, radius * 2f)
        onDrawBehind {
            val p = motion.progress
            // Every loop each dot moves one slot left, so the row looks endless.
            for (i in 0 until dotCount) {
                val x = mouthCentre.x + spacing * (i + 1 - p)
                if (x > mouthCentre.x + radius * 0.35f) drawCircle(dotColor, dot, Offset(x, center.y))
            }
            // One chomp per dot: open, closed halfway, open again as the next dot arrives.
            val mouth = 50f * abs(cos(Pi * p))
            drawArc(color, mouth / 2f, 360f - mouth, true, topLeft, mouthSize)
        }
    }
}
