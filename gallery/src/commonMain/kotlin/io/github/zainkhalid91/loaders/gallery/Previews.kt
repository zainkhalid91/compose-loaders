package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.zainkhalid91.loaders.FreezeLoaders
import io.github.zainkhalid91.loaders.LoaderState
import io.github.zainkhalid91.loaders.LoadingButton
import io.github.zainkhalid91.loaders.PathLoader
import io.github.zainkhalid91.loaders.PathLoaderStyle
import io.github.zainkhalid91.loaders.ResultLoader
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(widthDp = 720, heightDp = 1500)
@Composable
internal fun GalleryPreview() {
    LoaderGallery()
}

@Preview(widthDp = 400, heightDp = 900)
@Composable
internal fun PlaygroundPreview() {
    Box(Modifier.background(Background)) { Playground() }
}

@Preview
@Composable
internal fun CardPreview() {
    LoaderCard(LoaderCatalog.entries.first(), GalleryPalette[1], Modifier.width(170.dp))
}

@Preview(widthDp = 400, heightDp = 900)
@Composable
internal fun PatternsPreview() {
    Box(Modifier.background(Background)) { Patterns() }
}

/** Every button and result state side by side, frozen so the render is the same each time. */
@Preview(widthDp = 560, heightDp = 270)
@Composable
internal fun StatesPreview() {
    FreezeLoaders {
        Column(
            Modifier.background(Background).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                for (state in LoaderState.entries) {
                    LoadingButton(text = "Submit", state = state, onClick = {}, containerColor = GalleryPalette[0])
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                for (state in LoaderState.entries.drop(1)) ResultLoader(state = state, color = GalleryPalette[0])
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                PathLoader(DemoShapes.Badge, 24f, 24f, style = PathLoaderStyle.Trace, color = GalleryPalette[0], progress = 0.7f)
                PathLoader(DemoShapes.Cloud, 24f, 24f, style = PathLoaderStyle.Fill, color = GalleryPalette[1], progress = 0.6f)
                PathLoader(DemoShapes.Bolt, 24f, 24f, style = PathLoaderStyle.Shimmer, color = GalleryPalette[3])
            }
        }
    }
}
