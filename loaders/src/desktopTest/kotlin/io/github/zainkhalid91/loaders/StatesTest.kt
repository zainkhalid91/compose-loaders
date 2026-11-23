package io.github.zainkhalid91.loaders

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Anti-flicker timing, result states, the loading button, freezing and path splitting. */
@OptIn(ExperimentalTestApi::class)
class StatesTest {
    private val Frame = 16L

    /**
     * Runs [loading] through a timeline of (millis, isLoading) steps and samples visibility once a
     * frame. Returns one sample per [Frame] ms, so index * Frame is the time.
     */
    private fun visibility(vararg steps: Pair<Long, Boolean>): List<Boolean> {
        val loading = mutableStateOf(false)
        val samples = mutableListOf<Boolean>()
        runComposeUiTest {
            mainClock.autoAdvance = false
            var visible = false
            setContent { visible = rememberLoadingVisibility(loading.value).value }
            for ((millis, isLoading) in steps) {
                loading.value = isLoading
                repeat((millis / Frame).toInt()) {
                    mainClock.advanceTimeByFrame()
                    samples += visible
                }
            }
        }
        return samples
    }

    @Test
    fun fastRequestNeverShowsALoader() {
        val samples = visibility(120L to true, 1000L to false)
        assertTrue(samples.none { it }, "a 120 ms request should show no loader")
    }

    @Test
    fun slowRequestShowsAfterTheDelay() {
        val samples = visibility(1000L to true, 1000L to false)
        val firstShown = samples.indexOfFirst { it } * Frame
        assertTrue(firstShown in 280..340, "loader should appear about 300 ms in, appeared at $firstShown")
        val lastShown = samples.indexOfLast { it } * Frame
        assertTrue(lastShown in 980..1050, "loader should go when loading ends, went at $lastShown")
    }

    @Test
    fun shownLoaderStaysAtLeastTheMinimum() {
        // Loading ends 50 ms after the loader appears; it must still stay about 500 ms.
        val samples = visibility(350L to true, 1500L to false)
        val shownFor = samples.count { it } * Frame
        assertTrue(shownFor in 480..560, "loader should stay about 500 ms, stayed $shownFor")
        assertEquals(1, samples.zipWithNext().count { (a, b) -> a != b && b }, "loader must not blink")
    }

    @Test
    fun frozenLoadersHoldOneFrameAndVisibilityIsImmediate() = runComposeUiTest {
        mainClock.autoAdvance = false
        lateinit var motion: LoaderMotion
        var visible = false
        setContent {
            FreezeLoaders(frame = 0.4f) {
                motion = rememberLoaderMotion(1000, LoaderConfig())
                visible = rememberLoadingVisibility(true).value
            }
        }
        repeat(30) { mainClock.advanceTimeByFrame() }
        assertEquals(0.4f, motion.progress)
        assertTrue(visible, "inside FreezeLoaders visibility should follow isLoading at once")
    }

    @Test
    fun resultLoaderAnnouncesEachState() = runComposeUiTest {
        mainClock.autoAdvance = false
        val state = mutableStateOf(LoaderState.Loading)
        setContent { ResultLoader(state.value, config = LoaderConfig(label = "Uploading"), haptics = false) }
        mainClock.advanceTimeBy(50)
        val loading = onNode(hasContentDescription("Uploading")).fetchSemanticsNode().config
        assertEquals(ProgressBarRangeInfo.Indeterminate, loading[SemanticsProperties.ProgressBarRangeInfo])
        state.value = LoaderState.Success
        mainClock.advanceTimeBy(1000)
        onNode(hasContentDescription("Done")).assertExists()
        state.value = LoaderState.Error
        mainClock.advanceTimeBy(1000)
        onNode(hasContentDescription("Failed")).assertExists()
    }

    @Test
    fun buttonIgnoresTapsUntilIdleAndReportsTheResult() = runComposeUiTest {
        // A running loader asks for frames forever, so drive the clock by hand.
        mainClock.autoAdvance = false
        val state = mutableStateOf(LoaderState.Idle)
        var clicks = 0
        var resultShown = 0
        setContent {
            LoadingButton(
                text = "Pay",
                state = state.value,
                onClick = { clicks++; state.value = LoaderState.Loading },
                onResultShown = { resultShown++; state.value = LoaderState.Idle },
                haptics = false,
                modifier = Modifier.testTag("pay"),
            )
        }
        val button = onNodeWithTag("pay")
        val config = button.fetchSemanticsNode().config
        assertEquals(Role.Button, config[SemanticsProperties.Role])
        repeat(3) {
            button.performClick()
            mainClock.advanceTimeByFrame()
        }
        assertEquals(1, clicks, "taps while loading must not submit again")
        assertEquals("Loading", button.fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription))

        state.value = LoaderState.Success
        mainClock.advanceTimeBy(100)
        assertEquals("Done", button.fetchSemanticsNode().config.getOrNull(SemanticsProperties.StateDescription))
        assertEquals(0, resultShown)
        mainClock.advanceTimeBy(1500)
        assertEquals(1, resultShown, "onResultShown should run after resultMillis")
        assertEquals(LoaderState.Idle, state.value)
        mainClock.advanceTimeBy(1000)
        button.performClick()
        mainClock.advanceTimeByFrame()
        assertEquals(2, clicks, "an idle button takes taps again")
    }

    @Test
    fun buttonKeepsItsNameWhileLoading() = runComposeUiTest {
        // FreezeLoaders is also what lets a UI test with a running loader go idle.
        setContent {
            FreezeLoaders {
                LoadingButton(text = "Save", state = LoaderState.Loading, onClick = {}, haptics = false, modifier = Modifier.testTag("b"))
            }
        }
        val text = onNodeWithTag("b").fetchSemanticsNode().config.getOrNull(SemanticsProperties.Text)
        assertEquals("Save", text?.joinToString { it.text })
    }

    @Test
    fun splitsEveryOutlineOfAShape() {
        val ring = Path().apply {
            moveTo(0f, 0f); lineTo(10f, 0f); lineTo(10f, 10f); close()
            moveTo(20f, 20f); cubicTo(25f, 20f, 30f, 25f, 30f, 30f)
            moveTo(40f, 40f) // empty: dropped
        }
        assertEquals(2, splitContours(ring).size)
        val shape = LoaderShape.from("M2 2 L22 2 L22 22 Z M8 8 L16 8 L16 16 Z", 24f, 24f)
        assertEquals(2, shape.contours.size)
        assertFalse(shape.bounds.isEmpty)
    }
}
