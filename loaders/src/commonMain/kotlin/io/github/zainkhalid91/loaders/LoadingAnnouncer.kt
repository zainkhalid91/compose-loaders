package io.github.zainkhalid91.loaders

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Tells screen reader users when loading starts and when it finishes, which a loader alone can't
 * do: once loading ends the loader is gone, and so is anything it could say.
 *
 * Keep it in the layout for the whole lifetime of the screen, next to where the loader appears:
 *
 * ```
 * LoadingAnnouncer(isLoading = state.loading, loadingLabel = "Loading photos", doneLabel = "Photos loaded")
 * if (state.loading) CometSpinner(config = LoaderConfig(announce = false))
 * ```
 *
 * It is a polite live region, so it waits for the screen reader to finish what it is saying.
 * Nothing is announced if loading had already finished before it appeared.
 */
@Composable
public fun LoadingAnnouncer(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    loadingLabel: String = LoaderDefaults.label,
    doneLabel: String? = null,
) {
    var text by remember { mutableStateOf("") }
    var wasLoading by remember { mutableStateOf(false) }
    // Set after the first frame: a live region only speaks when its content changes.
    LaunchedEffect(isLoading, loadingLabel, doneLabel) {
        text = when {
            isLoading -> loadingLabel
            wasLoading && doneLabel != null -> doneLabel
            else -> ""
        }
        wasLoading = isLoading
    }
    Spacer(
        modifier
            .size(1.dp)
            .semantics {
                liveRegion = LiveRegionMode.Polite
                contentDescription = text
            },
    )
}
