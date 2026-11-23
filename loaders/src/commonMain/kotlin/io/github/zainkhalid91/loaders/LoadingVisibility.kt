package io.github.zainkhalid91.loaders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

/**
 * Decides when a loader should actually be on screen, so fast requests don't flash a spinner
 * for a frame or two:
 *
 * ```
 * val showLoader by rememberLoadingVisibility(isLoading = state.loading)
 * if (showLoader) CometSpinner()
 * ```
 *
 * - The loader appears only once loading has lasted [showAfterMillis]. A request that finishes
 *   sooner shows no loader at all.
 * - Once it appears it stays for at least [minVisibleMillis], even if loading ends sooner, so it
 *   never blinks on and off.
 * - If loading starts again while the loader is still showing, it just stays.
 *
 * Inside [FreezeLoaders] it returns `isLoading` straight away, so screenshot tests don't wait.
 */
@Composable
public fun rememberLoadingVisibility(
    isLoading: Boolean,
    showAfterMillis: Long = 300,
    minVisibleMillis: Long = 500,
): State<Boolean> {
    require(showAfterMillis >= 0 && minVisibleMillis >= 0) { "delays must not be negative" }
    if (LocalFrozenFrame.current != null) return rememberUpdatedState(isLoading)

    val visible = remember { mutableStateOf(false) }
    // True from the moment the loader appears until it has been up for minVisibleMillis.
    val holding = remember { mutableStateOf(false) }
    LaunchedEffect(isLoading, showAfterMillis) {
        if (isLoading) {
            if (!visible.value) {
                delay(showAfterMillis)
                holding.value = true
                visible.value = true
            }
        } else if (visible.value) {
            snapshotFlow { holding.value }.first { !it }
            visible.value = false
        }
    }
    LaunchedEffect(visible.value, minVisibleMillis) {
        if (visible.value) {
            delay(minVisibleMillis)
            holding.value = false
        }
    }
    return visible
}
