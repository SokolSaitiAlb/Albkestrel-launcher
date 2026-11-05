package com.albkestrel.launcher.core.common

import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Process-scoped, bounded LRU cache for decoded launcher icons.
 *
 * The cache is measured in bytes instead of entries so that large raster icons cannot exhaust the
 * heap even when the installed-app count is small. Android's [LruCache] is internally synchronized,
 * so the repository can access it from package-query workers and cache invalidation broadcasts
 * without an additional lock.
 *
 * Failed decodes are deliberately not cached: caching a negative result would hide a transient
 * PackageManager or drawable failure until the next package change.
 */
class IconMemoryCache(
    maxBytes: Int,
    private val sizeOf: (ImageBitmap) -> Int = { it.width * it.height * BYTES_PER_PIXEL }
) {

    init {
        require(maxBytes > 0) { "Icon cache size must be positive" }
    }

    private val cache = object : LruCache<String, ImageBitmap>(maxBytes) {
        override fun sizeOf(key: String, value: ImageBitmap): Int {
            return sizeOf(value).coerceAtLeast(1)
        }
    }

    fun get(key: String): ImageBitmap? = cache.get(key)

    fun put(key: String, value: ImageBitmap) {
        cache.put(key, value)
    }

    fun clear() {
        cache.evictAll()
    }

    /** Current retained size in bytes. Exposed for cache-policy tests and diagnostics. */
    fun sizeBytes(): Int = cache.size()

    /** Maximum retained size in bytes. */
    fun maxBytes(): Int = cache.maxSize()

    fun snapshotKeys(): Set<String> = cache.snapshot().keys

    /** Default budget for a launcher process: 1/10 of the app heap, clamped to 4-32 MiB. */
    companion object {
        private const val BYTES_PER_PIXEL = 4
        private const val MIN_BYTES = 4 * 1024 * 1024
        private const val MAX_BYTES = 32 * 1024 * 1024
        private const val HEAP_DIVISOR = 10L

        fun forCurrentHeap(): IconMemoryCache {
            val heapBudget = Runtime.getRuntime().maxMemory() / HEAP_DIVISOR
            val maxBytes = heapBudget
                .coerceIn(MIN_BYTES.toLong(), MAX_BYTES.toLong())
                .toInt()
            return IconMemoryCache(maxBytes)
        }
    }
}
