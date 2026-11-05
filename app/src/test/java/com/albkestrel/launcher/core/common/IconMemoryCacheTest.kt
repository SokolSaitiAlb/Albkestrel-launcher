package com.albkestrel.launcher.core.common

import androidx.compose.ui.graphics.ImageBitmap
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IconMemoryCacheTest {

    private fun icon(): ImageBitmap = ImageBitmap(2, 2)

    @Test
    fun `reports the configured byte budget`() {
        val cache = IconMemoryCache(maxBytes = 128)
        assertEquals(128, cache.maxBytes())
        assertEquals(0, cache.sizeBytes())
    }

    @Test
    fun `evicts the least recently used entry when the budget is exceeded`() {
        // Four bytes per icon, so a 12-byte budget holds exactly three entries.
        val cache = IconMemoryCache(maxBytes = 12) { 4 }
        cache.put("first", icon())
        cache.put("second", icon())
        cache.put("third", icon())

        // Make "first" the most recently used entry; "second" becomes the eviction candidate.
        assertNotNull(cache.get("first"))
        cache.put("fourth", icon())

        assertNotNull(cache.get("first"))
        assertNull(cache.get("second"))
        assertNotNull(cache.get("third"))
        assertNotNull(cache.get("fourth"))
        assertEquals(12, cache.sizeBytes())
    }

    @Test
    fun `clear releases every entry`() {
        val cache = IconMemoryCache(maxBytes = 64) { 8 }
        cache.put("first", icon())
        cache.put("second", icon())
        assertTrue(cache.sizeBytes() > 0)

        cache.clear()

        assertEquals(0, cache.sizeBytes())
        assertTrue(cache.snapshotKeys().isEmpty())
    }

    @Test
    fun `keeps entries within budget under concurrent access`() {
        val cache = IconMemoryCache(maxBytes = 256) { 8 }
        val executor = Executors.newFixedThreadPool(8)
        val start = CountDownLatch(1)
        val done = CountDownLatch(8)

        repeat(8) { worker ->
            executor.execute {
                start.await()
                repeat(64) { index ->
                    val key = "worker-$worker-$index"
                    cache.put(key, icon())
                    cache.get(key)
                }
                done.countDown()
            }
        }

        start.countDown()
        assertTrue("Concurrent cache operations timed out", done.await(10, TimeUnit.SECONDS))
        executor.shutdownNow()

        assertTrue("Cache exceeded its byte budget", cache.sizeBytes() <= cache.maxBytes())
        assertFalse(cache.snapshotKeys().isEmpty())
    }
}
