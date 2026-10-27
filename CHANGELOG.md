# Changelog

## 0.2.0 (2026-10-28)

Shaped by feedback on the launch post: config, accessibility, reduced motion and customization.
25 new loaders, taking the library from 20 to 45.

### New

- **`LoaderConfig`**: one immutable object for speed, pause (`isRunning`), direction (`reverse`),
  reduced motion, the screen reader label and announcements. Every loader takes `config`.
- **App-wide defaults**: `ProvideLoaderDefaults(color, config)`, or the `LocalLoaderColor` and
  `LocalLoaderConfig` CompositionLocals. Every loader's `color` now defaults to `LocalLoaderColor`.
- **Reduced motion**: `ReducedMotion.Auto` (follows the system animator scale on Android), `On`
  and `Off`. Reduced motion holds a pose and slowly pulses opacity instead of freezing.
- **Screen readers**: `LoaderConfig.label`, determinate `progress` on every loader, a polite live
  region that announces once on appear (`announce`), and `LoadingAnnouncer` for "started" and
  "finished" announcements.
- **Customization**: `strokeWidth` on ring and line loaders, `brush` on `ExpressiveArc` and
  `WaveBars`, `sparks` on `FireworkBurst`.
- **25 new loaders**: `RadarSweep`, `Hourglass`, `PacDots`, `SlidingBar`, `FireworkBurst`,
  `TypingBubble`, `WaveText`, `OrbitingMoons`, `FoldingCube`, `SegmentedRing`, `SkeletonShimmer`, `SpiralGalaxy`, `Pendulum`, `SlidingTiles`, `BreathingCircle`, `DominoFall`, `NeonTrace`, `BatteryCharge`, `Rocket`, `SignalBars`, `GearPair`, `YinYang`, `SpringCoil`, `GooeyOrbit`, `HeartPulse`.
  They come with their own options: counts (segments, moons, arms, stars, layers, cells, coils,
  teeth, blobs, lines, particles), shape options (gaps, corners, swing, stretch) and parts you can
  switch off (tail, orbits, avatar, bolt, echo).
- **`WaveText`**: animated text with your own string and style; screen readers read the text.
- **`SkeletonShimmer`**: a shimmering placeholder for the content that is loading.
- **Gallery Playground** tab with colour, hue, speed, size, reduced-motion, reverse and pause controls.
- **Benchmark app** (`:benchmark`, `tools/benchmark.ps1`) comparing frame times with an
  equivalent Lottie spinner.

### Changed

- Loaders run on the frame clock instead of an infinite transition. Changing speed no longer
  jumps, pausing keeps the frame, and an animator scale of 0 no longer freezes a loader.
- Draw objects (strokes, paths, brushes) are built once per size with `drawWithCache`; loader
  draw code allocates nothing per frame. `FlippingSquare` now projects its 3D turn itself
  instead of updating a graphicsLayer every frame.
- Every loader announces itself to screen readers by default. Pass
  `LoaderConfig(announce = false)` (or provide it app-wide) where many loaders are on screen.

### Tests

- Motion: speed, pause, reduced motion, "Remove animations", system scale, no jump on speed change.
- Accessibility: labels, determinate progress, live regions, app-wide defaults, `LoadingAnnouncer`,
  default colour contrast.
- Every loader: no more than three flashes per second (WCAG 2.3.1) in full and reduced motion, and
  bytes allocated per frame.

### Compatibility

Source compatible: new parameters were added at the end with defaults, so existing calls compile
unchanged. Not binary compatible with 0.1.0 (Kotlin default-argument signatures changed), so
libraries built against 0.1.0 must be rebuilt.

## 0.1.0 (2026-10-20)

First release: 20 loaders.
