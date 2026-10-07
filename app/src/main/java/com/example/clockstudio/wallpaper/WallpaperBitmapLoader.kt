package com.example.clockstudio.wallpaper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import com.example.clockstudio.domain.model.WallpaperItem
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

/** Sampled, process-wide image cache for wallpaper previews and the live wallpaper engine. */
object WallpaperBitmapLoader {
    private val maxCacheKb = (Runtime.getRuntime().maxMemory() / 1024L / 8L)
        .coerceIn(8_192L, 64_000L)
        .toInt()
    private val cache = object : LruCache<String, Bitmap>(maxCacheKb) {
        override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
    }
    private val missingAssets = ConcurrentHashMap.newKeySet<String>()

    fun load(context: Context, item: WallpaperItem, targetWidth: Int, targetHeight: Int): Bitmap? {
        val width = targetWidth.coerceAtLeast(1)
        val height = targetHeight.coerceAtLeast(1)
        val resourceKey = item.backgroundResource?.toString() ?: "procedural"
        val key = "${item.id}:$resourceKey:${item.assetFileName}:$width:$height"
        synchronized(cache) { cache.get(key)?.let { return it } }

        return try {
            val decoded = item.assetFileName
                ?.takeUnless { it in missingAssets }
                ?.let { file -> decodeAsset(context, file, width, height) ?: run { missingAssets.add(file); null } }
                ?: item.backgroundResource?.let { decodeResource(context, it, width, height) }

            decoded?.also { bitmap -> synchronized(cache) { cache.put(key, bitmap) } }
        } catch (_: OutOfMemoryError) {
            synchronized(cache) { cache.evictAll() }
            null
        } catch (_: RuntimeException) {
            null
        }
    }

    fun clear() {
        synchronized(cache) { cache.evictAll() }
        missingAssets.clear()
    }

    private fun decodeAsset(context: Context, filename: String, targetWidth: Int, targetHeight: Int): Bitmap? {
        val path = "wallpapers/$filename"
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
            val options = decodeOptions(bounds.outWidth, bounds.outHeight, targetWidth, targetHeight)
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
        } catch (_: IOException) {
            null
        }
    }

    private fun decodeResource(context: Context, resourceId: Int, targetWidth: Int, targetHeight: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resourceId, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        return BitmapFactory.decodeResource(
            context.resources,
            resourceId,
            decodeOptions(bounds.outWidth, bounds.outHeight, targetWidth, targetHeight),
        )
    }

    private fun decodeOptions(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int) =
        BitmapFactory.Options().apply {
            inSampleSize = sampleSize(sourceWidth, sourceHeight, targetWidth, targetHeight)
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inDither = true
            inMutable = false
        }

    private fun sampleSize(sourceWidth: Int, sourceHeight: Int, targetWidth: Int, targetHeight: Int): Int {
        var sample = 1
        while (sourceWidth / (sample * 2) >= targetWidth && sourceHeight / (sample * 2) >= targetHeight) {
            sample *= 2
        }
        return sample.coerceAtLeast(1)
    }
}
