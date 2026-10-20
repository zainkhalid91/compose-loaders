#!/usr/bin/env bash
# Turns gallery/build/frames (from ./gradlew :gallery:recordFrames) into docs/media GIFs. Needs ffmpeg.
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
echo "GIFs written to $out"
