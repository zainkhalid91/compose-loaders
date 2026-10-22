package io.github.zainkhalid91.loaders

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A linear indeterminate bar: a segment stretches out from the left, races across the track and
 * shrinks away on the right.
 */
@Composable
public fun SlidingBar(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    trackColor: Color = color.copy(alpha = color.alpha * 0.22f),
    width: Dp = 160.dp,
    height: Dp = 6.dp,
    durationMillis: Int = 1500,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    LoaderCanvas(modifier, width, height, durationMillis, progress, config, restPose = 0.45f) { motion ->
        val w = this.size.width
        val h = this.size.height
        val pill = Path().apply { addRoundRect(RoundRect(0f, 0f, w, h, CornerRadius(h / 2f))) }
        val corner = CornerRadius(h / 2f)
        onDrawBehind {
            val p = motion.progress
            // The head leads and the tail follows a quarter loop behind; both start and end off-track.
            val head = FastOutSlowInEasing.transform((p / 0.75f).coerceAtMost(1f))
            val tail = FastOutSlowInEasing.transform(((p - 0.25f) / 0.75f).coerceIn(0f, 1f))
            clipPath(pill) {
                drawRect(trackColor)
                val left = tail * w
                val right = head * w
                if (right - left > 0.5f) drawRoundRect(color, Offset(left, 0f), Size(right - left, h), corner)
            }
        }
    }
}
