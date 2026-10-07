package com.example.clockstudio.data.repository

import android.content.Context
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperItem
import com.example.clockstudio.domain.repository.WallpaperRepository
import java.util.Locale

/** Built-in catalog. Artwork is generated at runtime unless a matching licensed asset is supplied. */
class LocalWallpaperRepository(context: Context? = null) : WallpaperRepository {
    private val applicationContext = context?.applicationContext

    private val wallpapers: List<WallpaperItem> by lazy {
        buildList {
            customTitles.forEachIndexed { index, title ->
                add(item(ClockCategory.CUSTOM, index + 1, title, index, String.format(Locale.ROOT, "digital_%02d", 1)))
            }
            digitalTitles.forEachIndexed { index, title ->
                add(item(ClockCategory.DIGITAL, index + 1, title, 18 + index, String.format(Locale.ROOT, "digital_%02d", index + 1)))
            }
            analogTitles.forEachIndexed { index, title ->
                add(item(ClockCategory.ANALOG, index + 1, title, 30 + index, String.format(Locale.ROOT, "analog_%02d", index + 1)))
            }
            smartTitles.forEachIndexed { index, title ->
                add(item(ClockCategory.SMART, index + 1, title, 42 + index, String.format(Locale.ROOT, "smart_%02d", index + 1)))
            }
        }
    }

    override fun getAll(): List<WallpaperItem> = wallpapers

    override fun getByCategory(category: ClockCategory): List<WallpaperItem> =
        wallpapers.filter { it.category == category }

    override fun getById(id: String): WallpaperItem? = wallpapers.firstOrNull { it.id == id }

    @Suppress("DiscouragedApi")
    private fun item(
        category: ClockCategory,
        number: Int,
        title: String,
        artworkIndex: Int,
        clockStyleId: String,
    ): WallpaperItem {
        val id = String.format(Locale.ROOT, "%s_%02d", category.key, number)
        val resourceName = "wallpaper_$id"
        val resource = applicationContext?.let { appContext ->
            appContext.resources.getIdentifier(resourceName, "drawable", appContext.packageName)
                .takeIf { it != 0 }
        }
        return WallpaperItem(
            id = id,
            category = category,
            previewResource = resource,
            backgroundResource = resource,
            clockStyleId = clockStyleId,
            title = title,
            artworkIndex = artworkIndex,
            assetFileName = "$id.webp",
        )
    }

    private companion object {
        val customTitles = listOf(
            "Obsidian Bloom", "Aurora Veil", "Prism Drift", "Foxfire Signal", "Nocturne City", "Chromatic Spiral",
            "Lumen Garden", "Folded Geometry", "Orbit Rings", "Deep Field", "Velvet Current", "Dusk Valley",
            "Mosaic Tide", "Midnight Petals", "Solar Flare", "Blue Hour", "Glass Tides", "Quiet Horizon",
        )
        val digitalTitles = listOf(
            "Teal Horizon", "Signal Blue", "Monochrome", "Amber Relay", "Split Current", "Sidecar Date",
            "Quiet Minute", "Prism Digits", "Second Pulse", "Retro Amber", "Orbit Time", "Type Study",
        )
        val analogTitles = listOf(
            "Polar Classic", "Sundial Gold", "Ion Cyan", "Violet Pulse", "Walnut Meridian", "Glass Current",
            "Quiet Line", "Roman Night", "Circuit Arc", "Steel Orbit", "Lunar Halo", "Noir Gold",
        )
        val smartTitles = listOf(
            "Halo Panel", "Neon Ring", "Glass Module", "Quiet Stack", "Gradient Arc", "Thin Line",
            "Cyber Panel", "Color Orbit", "Soft Glass", "Mono Module", "Aurora Ring", "Prism Data",
        )
    }
}
