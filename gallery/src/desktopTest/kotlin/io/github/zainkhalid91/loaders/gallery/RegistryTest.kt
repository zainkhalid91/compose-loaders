package io.github.zainkhalid91.loaders.gallery

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Keeps LOADERS.md, the catalog and the library sources in step, so a loader
 * can't be added twice or shipped without being registered.
 */
class RegistryTest {
    // Public composables in :loaders that are not loaders themselves.
    private val Helpers = setOf("ProvideLoaderDefaults", "LoadingAnnouncer")

    private val root = generateSequence(File("").absoluteFile) { it.parentFile }
        .first { File(it, "LOADERS.md").exists() }

    // Rows look like: | 7 | WaveBars | Bars | ... |
    private val registry: List<Pair<Int, String>> = File(root, "LOADERS.md").readLines()
        .mapNotNull { Regex("""^\|\s*(\d+)\s*\|\s*`?(\w+)`?\s*\|""").find(it) }
        .map { it.groupValues[1].toInt() to it.groupValues[2] }

    @Test
    fun idsAndNamesAreUnique() {
        val entries = LoaderCatalog.entries
        assertEquals(entries.size, entries.map { it.id }.toSet().size, "duplicate id in LoaderCatalog")
        assertEquals(entries.size, entries.map { it.name.lowercase() }.toSet().size, "duplicate name in LoaderCatalog")
    }

    @Test
    fun idsAreSequential() {
        assertEquals((1..LoaderCatalog.entries.size).toList(), LoaderCatalog.entries.map { it.id })
    }

    @Test
    fun registryMatchesCatalog() {
        val catalog = LoaderCatalog.entries.map { it.id to it.name }
        assertEquals(catalog, registry, "LOADERS.md rows must match LoaderCatalog, in order")
    }

    @Test
    fun everyLibraryLoaderIsRegistered() {
        val sources = File(root, "loaders/src/commonMain/kotlin").walk().filter { it.extension == "kt" }
        val public = sources.flatMap { file ->
            Regex("""@Composable\s+public fun (\w+)\(""").findAll(file.readText()).map { it.groupValues[1] }
        }
            .filterNot { it in Helpers }
            .toSet()
        assertTrue(public.isNotEmpty(), "found no public loaders; did the source layout move?")
        val missing = public - registry.map { it.second }.toSet()
        if (missing.isNotEmpty()) fail("Public loaders missing from LOADERS.md: $missing")
    }
}
