package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Two meshing gears turning against each other; the small one turns faster by the ratio of
 * their teeth. Each loop moves the big gear on by exactly one tooth.
 */
@Composable
public fun GearPair(
    modifier: Modifier = Modifier,
    color: Color = LocalLoaderColor.current,
    secondaryColor: Color = color.copy(alpha = color.alpha * 0.6f),
    size: Dp = LoaderDefaults.size,
    teeth: Int = 12,
    durationMillis: Int = 600,
    progress: Float? = null,
    config: LoaderConfig = LocalLoaderConfig.current,
) {
    require(teeth in 8..24) { "teeth must be 8 to 24, was $teeth" }
    LoaderCanvas(modifier, size, size, durationMillis, progress, config, restPose = 0f) { motion ->
        val d = this.size.minDimension
        val smallTeeth = (teeth * 2f / 3f).roundToInt()
        // Pitch radii are proportional to the teeth; sized so both fit along the diagonal.
        val toothDepth = d * 0.06f
        val diagonal = d * sqrt(2f)
        val bigPitch = (diagonal - toothDepth * 4f) / (2f * (1f + smallTeeth.toFloat() / teeth) + 0.6f) * 0.92f
        val smallPitch = bigPitch * smallTeeth / teeth
        val reach = (bigPitch + smallPitch) / sqrt(2f)
        val bigCentre = Offset(center.x - reach / 2f, center.y + reach / 2f)
        val smallCentre = Offset(center.x + reach / 2f, center.y - reach / 2f)
        val big = gear(teeth, bigPitch, toothDepth)
        val small = gear(smallTeeth, smallPitch, toothDepth)
        // On the line between the centres, a big-gear tooth must face a small-gear gap. A tooth is
        // centred 0.325 of a step into its slot and a gap 0.825; the line runs at -45 degrees from
        // the big gear and 135 degrees from the small one.
        val bigStep = 360f / teeth
        val smallStep = 360f / smallTeeth
        val bigBase = -45f - 0.325f * bigStep
        val smallBase = 135f - 0.825f * smallStep
        onDrawBehind {
            // One tooth per loop on each gear; equal pitch means the same arc at the contact.
            val bigAngle = bigBase + motion.progress * bigStep
            val smallAngle = smallBase - motion.progress * smallStep
            translate(bigCentre.x, bigCentre.y) { rotate(bigAngle, Offset.Zero) { drawPath(big, color) } }
            translate(smallCentre.x, smallCentre.y) { rotate(smallAngle, Offset.Zero) { drawPath(small, secondaryColor) } }
        }
    }
}

/** A gear outline centred on the origin, with a hole in the middle. */
private fun gear(teeth: Int, pitch: Float, depth: Float): Path = Path().apply {
    fillType = PathFillType.EvenOdd
    val outer = pitch + depth
    val root = pitch - depth
    val step = TwoPi / teeth
    for (i in 0 until teeth) {
        val a = i * step
        // Each tooth: root, rise, flat top, fall, using quarter steps.
        val points = floatArrayOf(root, outer, outer, root)
        val angles = floatArrayOf(a, a + step * 0.2f, a + step * 0.45f, a + step * 0.65f)
        for (j in 0 until 4) {
            val x = points[j] * cos(angles[j])
            val y = points[j] * sin(angles[j])
            if (i == 0 && j == 0) moveTo(x, y) else lineTo(x, y)
        }
    }
    close()
    addOval(Rect(Offset.Zero, pitch * 0.32f))
}
