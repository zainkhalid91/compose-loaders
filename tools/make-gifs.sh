#!/usr/bin/env bash
# Turns gallery/build/frames (./gradlew :gallery:recordFrames) into docs/media GIFs, and
# gallery/build/demoreel (./gradlew :gallery:recordDemoReel) into docs/media/demo-reel.mp4. Needs ffmpeg.
set -euo pipefail
cd "$(dirname "$0")/.."
frames=gallery/build/frames
out=docs/media
mkdir -p "$out"
ffmpeg -loglevel error -y -framerate 30 -i "$frames/all/%03d.png" \
  -vf "scale=800:-1:flags=lanczos,split[a][b];[a]palettegen=max_colors=128[p];[b][p]paletteuse=dither=bayer:bayer_scale=4" \
  -loop 0 "$out/all.gif"
for dir in "$frames"/*/; do
  name=$(basename "$dir")
  [ "$name" = all ] && continue
  ffmpeg -loglevel error -y -framerate 30 -i "$dir/%03d.png" \
    -vf "scale=120:120:flags=lanczos,split[a][b];[a]palettegen=max_colors=64[p];[b][p]paletteuse" \
    -loop 0 "$out/$name.gif"
done
if [ -d gallery/build/demoreel ]; then
  ffmpeg -loglevel error -y -framerate 30 -i gallery/build/demoreel/%04d.png     -c:v libx264 -pix_fmt yuv420p -crf 18 -movflags +faststart "$out/demo-reel.mp4"
fi
echo "GIFs written to $out"
