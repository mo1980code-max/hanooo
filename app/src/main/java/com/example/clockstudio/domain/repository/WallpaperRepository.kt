package com.example.clockstudio.domain.repository

import com.example.clockstudio.domain.model.ClockCategory
import com.example.clockstudio.domain.model.WallpaperItem

interface WallpaperRepository {
    fun getAll(): List<WallpaperItem>
    fun getByCategory(category: ClockCategory): List<WallpaperItem>
    fun getById(id: String): WallpaperItem?
}
