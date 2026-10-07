package com.example.clockstudio

import android.app.Application
import com.example.clockstudio.data.preferences.UserPreferencesRepository
import com.example.clockstudio.data.repository.LocalWallpaperRepository
import com.example.clockstudio.domain.repository.WallpaperRepository

class ClockStudioApplication : Application() {
    lateinit var preferencesRepository: UserPreferencesRepository
        private set

    lateinit var wallpaperRepository: WallpaperRepository
        private set

    override fun onCreate() {
        super.onCreate()
        preferencesRepository = UserPreferencesRepository(applicationContext)
        wallpaperRepository = LocalWallpaperRepository(applicationContext)
    }
}
