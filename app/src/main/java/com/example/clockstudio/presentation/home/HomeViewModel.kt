package com.example.clockstudio.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.repository.WallpaperRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val wallpaperRepository: WallpaperRepository,
    private val preferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(
        HomeUiState(wallpapers = wallpaperRepository.getAll(), isLoading = false),
    )
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(preferencesRepository.settingsFlow, preferencesRepository.favoritesFlow) { settings, favorites ->
                settings to favorites
            }.catch { throwable ->
                _uiState.update { it.copy(isLoading = false, error = throwable.message) }
            }.collect { (settings, favorites) ->
                _uiState.update { current ->
                    val categoryChangedInSettings = current.appSettings.defaultCategory != settings.defaultCategory
                    val selected = if (current.isLoading || categoryChangedInSettings) {
                        settings.defaultCategory
                    } else {
                        current.selectedCategory
                    }
                    current.copy(
                        selectedCategory = selected,
                        language = settings.languageTag,
                        favoriteIds = favorites,
                        appSettings = settings,
                        isLoading = false,
                        error = null,
                    )
                }
            }
        }
    }

    fun selectCategory(category: ClockCategory) {
        _uiState.update { current ->
            if (current.selectedCategory == category) current else current.copy(selectedCategory = category)
        }
    }

    fun openWallpaper(wallpaperId: String) {
        if (wallpaperRepository.getById(wallpaperId) != null) {
            _uiState.update { it.copy(selectedWallpaperId = wallpaperId) }
        }
    }

    fun toggleFavorite(wallpaperId: String) {
        viewModelScope.launch { preferencesRepository.toggleFavorite(wallpaperId) }
    }

    fun setFavoritesOnly(enabled: Boolean) {
        _uiState.update { it.copy(showFavoritesOnly = enabled) }
    }

    companion object {
        fun factory(
            wallpaperRepository: WallpaperRepository,
            preferencesRepository: UserPreferencesRepository,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(HomeViewModel::class.java))
                return HomeViewModel(wallpaperRepository, preferencesRepository) as T
            }
        }
    }
}
