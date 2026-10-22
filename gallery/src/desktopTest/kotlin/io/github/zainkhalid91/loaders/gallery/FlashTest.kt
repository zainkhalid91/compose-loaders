package io.github.zainkhalid91.loaders.gallery

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.luminance
import io.github.zainkhalid91.loaders.LoaderConfig
import io.github.zainkhalid91.loaders.ReducedMotion
import org.jetbrains.skia.Bitmap
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.fail

/**
 * WCAG 2.3.1 (three flashes or below threshold): no loader may flash more than three times in any
 * one-second window. A flash is a pair of opposing changes in relative luminance of at least 0.1
 * where the darker state is below 0.8. Luminance is averaged over the area the loader actually
 * draws on, which is far stricter than WCAG's area rule for a loader this small.
 */
class FlashTest {
    private val fps = 60
    private val seconds = 4

    @Test
    fun noLoaderFlashesMoreThanThreeTimesASecond() {
        val failures = mutableListOf<String>()
        for (mode in listOf(ReducedMotion.Off, ReducedMotion.On)) {
            for (entry in LoaderCatalog.entries) {
                val worst = worstFlashesPerSecond(LoaderConfig(announce = false, reducedMotion = mode)) {
                    entry.content(GalleryPalette.first())
                }
                if (worst > 3) failures += "${entry.name} ($mode): $worst flashes in one second"
            }
        }
        if (failures.isNotEmpty()) fail(failures.joinToString("\n"))
    }

    private fun worstFlashesPerSecond(config: LoaderConfig, content: @Composable () -> Unit): Int {
        val frames = fps * seconds
        val pixels = 240 * 240
        val luma = Array(frames) { FloatArray(pixels) }
        renderFrames(frames, fps, config, content) { i, scene, _ ->
            val bitmap = Bitmap.makeFromImage(scene.render(i * 1_000_000_000L / fps))
            luminance(bitmap, luma[i])
            bitmap.close()
        }
        // Only pixels that differ from the dark background in some frame count towards the area.
        val background = Dark.luminance()
        val touched = BooleanArray(pixels)
        for (frame in luma) for (p in 0 until pixels) if (abs(frame[p] - background) > 0.002f) touched[p] = true
        val area = touched.count { it }.coerceAtLeast(1)
        val mean = FloatArray(frames) { f -> var sum = 0f; for (p in 0 until pixels) if (touched[p]) sum += luma[f][p]; sum / area }
        return worstWindow(transitions(mean))
    }

    /** Frame indices of luminance swings of at least 0.1, alternating in direction. */
    private fun transitions(mean: FloatArray): List<Int> {
        val result = mutableListOf<Int>()
        var anchor = mean[0]
        var direction = 0
        for (i in 1 until mean.size) {
            val delta = mean[i] - anchor
            val newDirection = if (delta >= 0.1f) 1 else if (delta <= -0.1f) -1 else 0
            if (newDirection != 0 && newDirection != direction && minOf(mean[i], anchor) < 0.8f) {
                result += i
                direction = newDirection
                anchor = mean[i]
            } else if ((direction >= 0 && mean[i] > anchor) || (direction < 0 && mean[i] < anchor)) {
                anchor = mean[i]
            }
        }
        return result
    }

    /** Most flashes (pairs of opposing transitions) in any one-second window. */
    private fun worstWindow(transitions: List<Int>): Int {
        var worst = 0
        for (start in transitions) {
            val inWindow = transitions.count { it >= start && it < start + fps }
            worst = maxOf(worst, inWindow / 2)
        }
        return worst
    }
}
