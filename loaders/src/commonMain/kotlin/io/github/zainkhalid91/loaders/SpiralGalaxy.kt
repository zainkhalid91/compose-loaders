package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Stars drift inward along the spiral arms of a slowly turning galaxy, fading as they reach the
 * glowing core and reappearing at the rim.
 */
@Composable
public fun SpiralGalaxy(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    coreColor: Color = color,
    size: Dp = LoaderDefaults.size,
    arms: Int = 3,
    stars: Int = 36,
    durationMillis: Int = 3000,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(arms >= 1) { "arms must be at least 1, was $arms" }
    require(stars >= arms) { "stars must be at least arms" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0.25f) { motion ->
        val r = this.size.minDimension / 2f
        val dot = r * 0.055f
        onDrawBehind {
            drawCircle(coreColor.copy(alpha = coreColor.alpha * 0.25f), r * 0.24f)
            drawCircle(coreColor, r * 0.12f)
            rotate(motion.progress * 360f) {
                for (k in 0 until stars) {
                    val arm = k % arms
                    // Spread each arm's stars along their journey so the arm is always full.
                    val life = fract(motion.progress + (k / arms) * arms.toFloat() / stars + arm * 0.13f)
                    val distance = r * 0.92f * (1f - life) + r * 0.12f * life
                    val angle = TwoPi * arm / arms + 2.4f * life
                    val fade = smoothstep(0f, 0.15f, life) * (1f - smoothstep(0.75f, 1f, life))
                    drawCircle(
                        color.copy(alpha = color.alpha * fade),
                        dot * (0.6f + 0.6f * life),
                        Offset(center.x + distance * cos(angle), center.y + distance * sin(angle)),
                    )
                }
            }
        }
    }
}
