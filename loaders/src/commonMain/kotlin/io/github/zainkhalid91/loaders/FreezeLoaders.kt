package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
internal class FrozenFrame(val frame: Float?)

/** Non-null while inside [FreezeLoaders]. */
internal val LocalFrozenFrame = staticCompositionLocalOf<FrozenFrame?> { null }

/**
 * Pins every loader in [content] to a single frame. Use it in screenshot tests (Paparazzi,
 * Roborazzi, Compose UI tests) and previews:
 *
 * ```
 * paparazzi.snapshot { FreezeLoaders { CheckoutScreen(state = Loading) } }
 * ```
 *
 * Without it, a running loader lands on a different frame in every run, so screenshots never
 * match, and its endless frame requests can keep a UI test from ever going idle. Frozen loaders
 * ask for no frames at all. Your production code doesn't change: wrap the screen in the test.
 *
 * Inside it, [ResultLoader] and [LoadingButton] jump straight to their end state, and
 * [rememberLoadingVisibility] follows `isLoading` with no delay.
 *
 * @param frame Where in the loop to stop, from 0 to 1. Null stops each loader on its rest pose,
 *   the frame it shows in reduced motion, which is picked to be easy to recognise.
 */
@Composable
public fun FreezeLoaders(frame: Float? = null, content: @Composable () -> Unit) {
    require(frame == null || frame in 0f..1f) { "frame must be between 0 and 1, was $frame" }
    CompositionLocalProvider(LocalFrozenFrame provides FrozenFrame(frame), content = content)
}
