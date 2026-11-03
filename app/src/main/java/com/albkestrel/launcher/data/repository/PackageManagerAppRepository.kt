package com.albkestrel.launcher.data.repository

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.model.LauncherApp
import com.albkestrel.launcher.domain.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Repository backed by Android PackageManager.
 *
 * PackageManager stays in the data layer. Icons are converted once into [ImageBitmap]s and cached
 * alongside the mapped application list so Compose never needs to touch PackageManager.
 */
class PackageManagerAppRepository(
    context: Context
) : AppRepository {

    private val appContext = context.applicationContext
    private val cacheLock = Any()
    private val iconCache = mutableMapOf<String, ImageBitmap?>()
    private val packageVersion = MutableStateFlow(0L)

    @Volatile
    private var cachedApps: List<LauncherApp>? = null

    /**
     * Emits after the process receives a package add/remove/update broadcast. The initial value is
     * intentionally skipped so consumers only react to actual changes.
     */
    override val packageChanges: Flow<Unit> = packageVersion.drop(1).map { }

    private val packageChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_PACKAGE_ADDED,
                Intent.ACTION_PACKAGE_REMOVED,
                Intent.ACTION_PACKAGE_CHANGED,
                Intent.ACTION_PACKAGE_REPLACED -> invalidateCache()
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        // Application context + process-scoped repository: the receiver lives as long as the app
        // process and never references an Activity.
        ContextCompat.registerReceiver(
            appContext,
            packageChangeReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun loadAllApps(): List<LauncherApp> {
        cachedApps?.let { return it }
        synchronized(cacheLock) {
            cachedApps?.let { return it }
            val apps = try {
                queryInstalledLauncherApps()
            } catch (_: Exception) {
                // Do not cache a transient failure; the next refresh will retry.
                return emptyList()
            }
            cachedApps = apps
            return apps
        }
    }

    override fun findAppByPackage(packageName: String): LauncherApp? {
        return loadAllApps().find { it.packageName == packageName }
    }

    override fun launchApp(app: LauncherApp): Boolean {
        val intent = buildLaunchIntent(app) ?: return false
        return try {
            appContext.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Builds an explicit intent for the exact launcher activity discovered for [app]. Falling back
     * to PackageManager's front-door intent keeps compatibility with legacy workspace entries.
     */
    internal fun buildLaunchIntent(app: LauncherApp): Intent? {
        if (!app.isInstalledApp) return null

        val activityName = app.activityName
        if (!activityName.isNullOrBlank()) {
            return Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
        }

        return appContext.packageManager
            .getLaunchIntentForPackage(app.packageName)
            ?.apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    }

    private fun queryInstalledLauncherApps(): List<LauncherApp> {
        val packageManager = appContext.packageManager
        val intent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(intent, 0)
        }

        val records = resolveInfos.mapNotNull { resolveInfo ->
            resolveInfo.toLauncherActivityRecord(packageManager)
        }
        return mapLauncherActivities(records, appContext.packageName)
    }

    private fun ResolveInfo.toLauncherActivityRecord(
        packageManager: PackageManager
    ): LauncherActivityRecord? {
        val info = activityInfo ?: return null
        val packageName = info.packageName ?: return null
        val activityName = info.name ?: return null
        val applicationInfo = info.applicationInfo
        val label = runCatching { loadLabel(packageManager).toString() }
            .getOrNull()
            .orEmpty()
        val icon = loadIconCached("$packageName/$activityName") { loadIcon(packageManager) }

        return LauncherActivityRecord(
            packageName = packageName,
            activityName = activityName,
            label = label,
            icon = icon,
            category = applicationInfo?.let(::mapApplicationCategory) ?: AppCategory.TOOLS,
            isSystemApp = applicationInfo != null &&
                (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
        )
    }

    private fun loadIconCached(key: String, loader: () -> Drawable?): ImageBitmap? {
        synchronized(iconCache) {
            if (iconCache.containsKey(key)) return iconCache[key]
        }

        val icon = try {
            val drawable = loader() ?: return null
            val density = appContext.resources.displayMetrics.density
            val sizePx = (64f * density).toInt().coerceIn(96, 192)
            drawable.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (_: Exception) {
            null
        }

        synchronized(iconCache) { iconCache[key] = icon }
        return icon
    }

    private fun mapApplicationCategory(applicationInfo: ApplicationInfo): AppCategory {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return AppCategory.TOOLS

        return when (applicationInfo.category) {
            ApplicationInfo.CATEGORY_GAME -> AppCategory.GAMES
            ApplicationInfo.CATEGORY_AUDIO,
            ApplicationInfo.CATEGORY_VIDEO,
            ApplicationInfo.CATEGORY_IMAGE -> AppCategory.MEDIA
            ApplicationInfo.CATEGORY_SOCIAL -> AppCategory.SOCIAL
            ApplicationInfo.CATEGORY_PRODUCTIVITY -> AppCategory.PRODUCTIVITY
            else -> AppCategory.TOOLS
        }
    }

    private fun invalidateCache() {
        synchronized(cacheLock) {
            cachedApps = null
            synchronized(iconCache) { iconCache.clear() }
        }
        packageVersion.update { it + 1 }
    }
}
