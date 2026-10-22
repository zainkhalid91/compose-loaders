package io.github.zainkhalid91.loaders

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.MotionDurationScale
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Frame-clock behaviour: speed, pausing, reduced motion and the "remove animations" setting. */
@OptIn(ExperimentalTestApi::class)
class MotionTest {
    private fun sample(config: LoaderConfig, frames: Int, scale: Float? = null): List<Pair<Float, Float>> {
        val samples = mutableListOf<Pair<Float, Float>>()
        val context = if (scale == null) {
            kotlin.coroutines.EmptyCoroutineContext
        } else {
            object : MotionDurationScale {
                override val scaleFactor: Float = scale
            }
        }
        runComposeUiTest(effectContext = context) {
            mainClock.autoAdvance = false
            lateinit var motion: LoaderMotion
            setContent { motion = rememberLoaderMotion(1000, config, restPose = 0.25f) }
            repeat(frames) {
                mainClock.advanceTimeByFrame()
                samples += motion.progress to motion.alpha
            }
        }
        return samples
    }

    private fun travelled(samples: List<Pair<Float, Float>>): Float =
        samples.zipWithNext { a, b -> fract(b.first - a.first) }.sum()

    @Test
    fun runsAtNormalSpeed() {
        // 60 frames of 16 ms is about one 1000 ms loop.
        val distance = travelled(sample(LoaderConfig(), 61))
        assertTrue(distance in 0.85f..1.1f, "expected about one loop, got $distance")
    }

    @Test
    fun speedScalesTheLoop() {
        val normal = travelled(sample(LoaderConfig(), 31))
        val fast = travelled(sample(LoaderConfig(speed = 2f), 31))
        assertEquals(normal * 2f, fast, normal * 0.1f)
    }

    @Test
    fun pausedLoaderHoldsItsFrame() {
        val samples = sample(LoaderConfig(isRunning = false), 30)
        assertEquals(1, samples.map { it.first }.toSet().size, "a paused loader must not move")
    }

    @Test
    fun reducedMotionHoldsStillAndPulses() {
        val samples = sample(LoaderConfig(reducedMotion = ReducedMotion.On), 120)
        assertTrue(samples.all { it.first == 0.25f }, "reduced motion must hold the rest pose")
        val alphas = samples.map { it.second }
        assertTrue(alphas.max() - alphas.min() > 0.2f, "reduced motion should still pulse so it reads as busy")
        assertTrue(alphas.min() >= 0.2f, "the pulse must never fade the loader out")
    }

    @Test
    fun removeAnimationsNeverFreezesTheLoader() {
        // System "Remove animations" is a duration scale of 0.
        val auto = sample(LoaderConfig(), 120, scale = 0f)
        assertTrue(auto.all { it.first == 0.25f }, "Auto should switch to the reduced pose")
        assertNotEquals(auto.first().second, auto.last().second, "Auto must still pulse, not freeze")

        val forced = sample(LoaderConfig(reducedMotion = ReducedMotion.Off), 61, scale = 0f)
        assertTrue(travelled(forced) > 0.8f, "Off must keep full motion even at scale 0")
    }

    @Test
    fun slowerSystemScaleSlowsTheLoader() {
        val normal = travelled(sample(LoaderConfig(), 31))
        val slow = travelled(sample(LoaderConfig(), 31, scale = 2f))
        assertEquals(normal / 2f, slow, normal * 0.1f)
    }

    @Test
    fun speedChangeDoesNotJump() {
        val speed = mutableStateOf(1f)
        val samples = mutableListOf<Float>()
        runComposeUiTest {
            mainClock.autoAdvance = false
            lateinit var motion: LoaderMotion
            setContent { motion = rememberLoaderMotion(1000, LoaderConfig(speed = speed.value)) }
            repeat(20) { mainClock.advanceTimeByFrame(); samples += motion.progress }
            speed.value = 3f
            repeat(20) { mainClock.advanceTimeByFrame(); samples += motion.progress }
        }
        val biggestStep = samples.zipWithNext { a, b -> fract(b - a) }.max()
        // At 3x a 16 ms frame moves about 0.05 of a loop; a reset to 0 would show up as a big step.
        assertTrue(biggestStep < 0.1f, "progress jumped by $biggestStep when speed changed")
    }
}
