package io.github.zainkhalid91.loaders

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AccessibilityTest {
    private fun SemanticsNodeInteraction.liveRegion() =
        fetchSemanticsNode().config.getOrNull(SemanticsProperties.LiveRegion)

    @Test
    fun saysWhatIsLoading() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { CometSpinner(config = LoaderConfig(label = "Loading photos")) }
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        val node = onNode(hasContentDescription("Loading photos"))
        node.assertExists()
        assertEquals(ProgressBarRangeInfo.Indeterminate, node.fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo])
        assertEquals(LiveRegionMode.Polite, node.liveRegion())
    }

    @Test
    fun announcesOnAppearByArrivingOneFrameLate() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { SpokeSpinner(config = LoaderConfig(label = "Saving")) }
        // A live region only speaks when its content changes, so the label must not be there at first.
        onNode(hasContentDescription("Saving")).assertDoesNotExist()
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        onNode(hasContentDescription("Saving")).assertExists()
    }

    @Test
    fun quietWhenAnnounceIsOff() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent { WaveBars(config = LoaderConfig(label = "Syncing", announce = false)) }
        val node = onNode(hasContentDescription("Syncing"))
        node.assertExists()
        assertNull(node.liveRegion())
    }

    @Test
    fun reportsDeterminateProgress() = runComposeUiTest {
        mainClock.autoAdvance = false
        val progress = mutableStateOf(0.4f)
        setContent { LiquidFill(progress = progress.value, config = LoaderConfig(announce = false)) }
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.4f, 0f..1f))).assertExists()
        progress.value = 0.75f
        mainClock.advanceTimeByFrame()
        onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.75f, 0f..1f))).assertExists()
    }

    @Test
    fun appWideDefaultsApply() = runComposeUiTest {
        mainClock.autoAdvance = false
        setContent {
            ProvideLoaderDefaults(config = LoaderConfig(label = "Cargando", announce = false)) { DualRing() }
        }
        onNode(hasContentDescription("Cargando")).assertExists()
    }

    @Test
    fun announcerSpeaksOnStartAndFinish() = runComposeUiTest {
        mainClock.autoAdvance = false
        val loading = mutableStateOf(true)
        setContent { LoadingAnnouncer(isLoading = loading.value, loadingLabel = "Loading photos", doneLabel = "Photos loaded") }
        mainClock.advanceTimeByFrame()
        onNode(hasContentDescription("Loading photos")).assertExists()
        loading.value = false
        mainClock.advanceTimeByFrame()
        mainClock.advanceTimeByFrame()
        onNode(hasContentDescription("Photos loaded")).assertExists()
    }

    @Test
    fun defaultColourHasEnoughContrastOnLightAndDark() {
        // WCAG 1.4.11 asks 3:1 for graphics that convey state.
        fun contrast(a: Color, b: Color): Float {
            val (hi, lo) = listOf(a.luminance(), b.luminance()).sortedDescending()
            return (hi + 0.05f) / (lo + 0.05f)
        }
        val onWhite = contrast(LoaderDefaults.color, Color.White)
        val onBlack = contrast(LoaderDefaults.color, Color.Black)
        assertTrue(onWhite >= 3f, "contrast on white is $onWhite")
        assertTrue(onBlack >= 3f, "contrast on black is $onBlack")
    }
}
