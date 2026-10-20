package io.github.zainkhalid91.loaders.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Background = Color(0xFF0F1115)
private val Card = Color(0xFF181B22)
private val CardBorder = Color(0xFF262A33)
private val Muted = Color(0xFF8A90A0)

/** Tints the gallery can switch between, so every loader is seen on more than one colour. */
public val GalleryPalette: List<Color> = listOf(
    Color(0xFF7C5CFF),
    Color(0xFF22D3EE),
    Color(0xFFF472B6),
    Color(0xFFFBBF24),
    Color(0xFF34D399),
)

/** Every loader in [LoaderCatalog] on one scrolling grid, with a colour picker. */
@Composable
public fun LoaderGallery(modifier: Modifier = Modifier) {
    var tint by remember { mutableStateOf(GalleryPalette.first()) }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize().background(Background).safeDrawingPadding(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Header(tint = tint, onTint = { tint = it })
        }
        items(LoaderCatalog.entries, key = { it.id }) { entry ->
            LoaderCard(entry, tint)
        }
    }
}

@Composable
private fun Header(tint: Color, onTint: (Color) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
        BasicText(
            "Compose Loaders",
            style = TextStyle(color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(Modifier.height(4.dp))
        BasicText(
            "${LoaderCatalog.entries.size} loading indicators, pure Compose, no assets",
            style = TextStyle(color = Muted, fontSize = 14.sp),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            for (color in GalleryPalette) {
                val selected = color == tint
                Box(
                    Modifier
                        .size(28.dp)
                        .border(2.dp, if (selected) Color.White else Color.Transparent, CircleShape)
                        .padding(4.dp)
                        .background(color, CircleShape)
                        .clickable { onTint(color) },
                )
            }
        }
    }
}

@Composable
internal fun LoaderCard(entry: LoaderEntry, tint: Color, modifier: Modifier = Modifier) {
    Column(
        modifier
            .background(Card, RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(110.dp), contentAlignment = Alignment.Center) {
            entry.content(tint)
        }
        BasicText(
            entry.name,
            style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
        )
        BasicText(
            "#${entry.id.toString().padStart(2, '0')} · ${entry.category.name}",
            style = TextStyle(color = Muted, fontSize = 12.sp),
        )
    }
}
