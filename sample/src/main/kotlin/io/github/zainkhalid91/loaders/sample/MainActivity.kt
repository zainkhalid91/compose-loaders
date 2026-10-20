package io.github.zainkhalid91.loaders.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.zainkhalid91.loaders.gallery.LoaderGallery

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoaderGallery() }
    }
}
