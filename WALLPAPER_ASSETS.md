# Wallpaper asset guide

Clock Studio starts with procedural, original Canvas artwork. You can replace any placeholder with an image for which you have commercial distribution rights. The gallery, clock overlays, customization, and wallpaper service all use the same `WallpaperItem` catalog and bitmap loader.

## Recommended image specifications

- Portrait master: **1440 × 2560 px** (9:16 is also supported)
- Use high-quality **WebP** for compact color images; **JPEG** is also supported. Android drawable resources may additionally use PNG/WebP.
- RGB/sRGB color profile; avoid very large transparent PNGs.
- Keep each compressed file reasonably small (for example, under 2 MB) to reduce install size and first-decode latency.
- Compose previews and the system engine decode sampled bitmaps into a bounded in-memory LRU cache. Do not commit uncompressed masters unless the project needs them.

## Drop-in locations and names

### Assets directory

Place a file at:

```text
app/src/main/assets/wallpapers/<wallpaper-id>.webp
```

The local catalog uses stable IDs such as `custom_01`, `digital_09`, `analog_04`, and `smart_12`. Examples:

```text
app/src/main/assets/wallpapers/custom_01.webp
app/src/main/assets/wallpapers/analog_04.webp
```

The `assetFileName` field in `WallpaperItem` already defaults to `<id>.webp`. No gallery or engine changes are needed when you replace a matching procedural background with that file.

### `drawable-nodpi`

Alternatively, add an image such as:

```text
app/src/main/res/drawable-nodpi/wallpaper_custom_01.webp
```

`LocalWallpaperRepository` looks for a drawable named `wallpaper_<wallpaper-id>` and uses it when present. Avoid density-qualified drawable folders so Android does not rescale a large wallpaper unexpectedly.

If both locations contain an image, the matching asset file is preferred. The fallback artwork is generated on-device if neither is present.

## Connecting a new wallpaper to a clock style

Edit `LocalWallpaperRepository.kt` and add a catalog entry with a unique ID, title, category, artwork index, and clock style ID. Built-in style catalogs live in `domain/model/ClockStyleCatalog.kt`:

- `digital_01` … `digital_12`
- `analog_01` … `analog_12`
- `smart_01` … `smart_12`

For example, a new digital item can use `category = ClockCategory.DIGITAL`, `clockStyleId = "digital_03"`, `id = "digital_13"`, and `assetFileName = "digital_13.webp"`. Add the required style to the matching style catalog if it does not already exist. The native renderer uses the wallpaper's category and style ID; it does not depend on filenames or image-specific UI code.

Keep the `artworkIndex` unique enough to give missing-image fallbacks variety. Add any artist/source/license attribution to your distribution documentation and preserve a copy of the license with the source asset outside the generated APK if required.
