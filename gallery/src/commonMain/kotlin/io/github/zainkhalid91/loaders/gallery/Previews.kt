package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
