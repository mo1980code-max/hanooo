package com.example.clockstudio.data.repository

import com.example.clockstudio.domain.model.ClockCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class LocalWallpaperRepositoryTest {
    private val repository = LocalWallpaperRepository()

    @Test fun catalogHasAtLeastRequiredOriginalDesignCount() {
        assertEquals(18, repository.getByCategory(ClockCategory.CUSTOM).size)
        assertEquals(12, repository.getByCategory(ClockCategory.DIGITAL).size)
        assertEquals(12, repository.getByCategory(ClockCategory.ANALOG).size)
        assertEquals(12, repository.getByCategory(ClockCategory.SMART).size)
        assertEquals(54, repository.getAll().size)
    }

    @Test fun categoryFilteringDoesNotLeakOtherFamilies() {
        val digital = repository.getByCategory(ClockCategory.DIGITAL)
        assertEquals(12, digital.size)
        assertEquals(setOf(ClockCategory.DIGITAL), digital.map { it.category }.toSet())
        assertNotNull(repository.getById("analog_12"))
        assertNull(repository.getById("not_a_wallpaper"))
    }
}
