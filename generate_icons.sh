#!/bin/bash

# Watermelon Player Icon Generator
# Usage: ./generate_icons.sh [base_icon.png or base_icon.svg]

BASE_ICON="${1:-icon.png}"  # Default to icon.png if no arg

# Sizes for launcher icons
SIZES=(
  "mdpi:48"
  "hdpi:72"
  "xhdpi:96"
  "xxhdpi:144"
  "xxxhdpi:192"
)

# Adaptive icon foreground (same sizes)
for size in "${SIZES[@]}"; do
  density="${size%%:*}"
  px="${size##*:}"
  
  mkdir -p app/src/main/res/mipmap-$density
  
  if [[ $BASE_ICON == *.svg ]]; then
    rsvg-convert -w $px -h $px "$BASE_ICON" -o "app/src/main/res/mipmap-$density/ic_launcher.png"
    rsvg-convert -w $px -h $px "$BASE_ICON" -o "app/src/main/res/mipmap-$density/ic_launcher_foreground.png"
  else
    convert "$BASE_ICON" -resize ${px}x${px} "app/src/main/res/mipmap-$density/ic_launcher.png"
    convert "$BASE_ICON" -resize ${px}x${px} "app/src/main/res/mipmap-$density/ic_launcher_foreground.png"
  fi
  
  echo "Generated mipmap-$density: ${px}x${px}"
done

# Round icon (optional for some launchers)
for size in "${SIZES[@]}"; do
  density="${size%%:*}"
  px="${size##*:}"
  convert "app/src/main/res/mipmap-$density/ic_launcher.png" \
    \( +clone -alpha extract -draw 'fill black polygon 0,0 0,$px $px,$px $px,0' -blur 0x10 \) \
    -alpha off -compose CopyOpacity -composite \
    "app/src/main/res/mipmap-$density/ic_launcher_round.png"
done

# Adaptive background (simple solid color - change #000000 to your background)
for size in "${SIZES[@]}"; do
  density="${size%%:*}"
  px="${size##*:}"
  convert -size ${px}x${px} xc:#000000 "app/src/main/res/mipmap-$density/ic_launcher_background.png"
done

echo "All icons generated! Clean and rebuild your project."
