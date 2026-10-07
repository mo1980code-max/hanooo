package com.example.clockstudio.presentation.home

import com.example.clockstudio.domain.model.AppSettings
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperItem

data class HomeUiState(
    val selectedCategory: ClockCategory = ClockCategory.CUSTOM,
    val wallpapers: List<WallpaperItem> = emptyList(),
    val language: String = "en",
    val favoriteIds: Set<String> = emptySet(),
    val appSettings: AppSettings = AppSettings(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedWallpaperId: String? = null,
    val showFavoritesOnly: Boolean = false,
) {
    fun wallpapersFor(category: ClockCategory): List<WallpaperItem> =
        wallpapers.filter { it.category == category }
}
