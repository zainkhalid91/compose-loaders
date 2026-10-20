# Adding a loader

1. **Check it's new.** Read the *Motion signature* column in [LOADERS.md](LOADERS.md). A loader must
   bring a new motion idea. More dots, another colour or a different speed is a parameter on an
   existing loader, not a new loader.
2. **Write it** in `loaders/src/commonMain/kotlin/io/github/zainkhalid91/loaders/<Name>.kt`:
   - signature `public fun Name(modifier: Modifier = Modifier, color: Color = LoaderDefaults.color, size: Dp = LoaderDefaults.size, ..., durationMillis: Int = ...)`
   - start the modifier chain with `modifier.loader(size)` (adds progress semantics and the default size)
   - drive it with `rememberLoopProgress(durationMillis)` and read the value **only** inside draw or
     `graphicsLayer` lambdas, so frames don't recompose
   - make the loop seamless: at progress 1 everything must look the same as at progress 0
     (use whole turns, whole wave periods, and so on)
   - use only Compose runtime, foundation, ui and animation
3. **Register it**: add the next row to LOADERS.md (numbers are permanent) and the matching
   `LoaderEntry` to `gallery/.../LoaderCatalog.kt`.
4. **Check it**:
   ```bash
   ./gradlew :gallery:desktopTest :gallery:recordFrames
   ```
   The test fails if LOADERS.md, the catalog and the library disagree. Look at the frames in
   `gallery/build/frames/<Name>/` before opening a PR.
5. **Media**: run `tools/make-gifs.sh` (needs ffmpeg) and add the loader's GIF to the README table.
