package com.albkestrel.launcher.data.repository

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.albkestrel.launcher.core.common.IconMemoryCache
import com.albkestrel.launcher.domain.contextmenu.AppShortcut
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
    private val iconCache = IconMemoryCache.forCurrentHeap()
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

    override fun openAppDetails(packageName: String): Boolean {
        val isInstalled = loadAllApps().any { it.packageName == packageName && it.isInstalledApp }
        if (!isInstalled) return false

        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            appContext.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun uninstallApp(packageName: String): Boolean {
        val app = loadAllApps()
            .find { it.packageName == packageName && it.isInstalledApp }
            ?: return false

        // Defense in depth: the domain policy already omits the action for system packages, and
        // the repository must never initiate removal for them even if a caller bypasses it.
        if (app.isSystemApp) return false

        // The system owns the confirmation UI for package removal; the launcher only initiates.
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = Uri.parse("package:$packageName")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            appContext.startActivity(intent)
            true
        } catch (_: Exception) {
            false
        }
    }

    override fun getShortcutsForPackage(packageName: String): List<AppShortcut> {
        // LauncherApps shortcut discovery was introduced in Android 7.1 (API 25). minSdk is 24,
        // so older devices must gracefully fall back to the regular app launch actions.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return emptyList()

        val launcherApps = appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return emptyList()

        if (!launcherApps.hasShortcutHostPermission()) {
            return emptyList()
        }

        val query = LauncherApps.ShortcutQuery().apply {
            setPackage(packageName)
            setQueryFlags(
                LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED
            )
        }

        val shortcuts: List<ShortcutInfo> = try {
            launcherApps.getShortcuts(query, Process.myUserHandle()) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        val result = mutableListOf<AppShortcut>()
        for (info in shortcuts) {
            val id = info.id ?: continue
            result += AppShortcut(
                id = id,
                packageName = info.`package` ?: packageName,
                activityName = info.activity?.className,
                shortLabel = info.shortLabel?.toString().orEmpty(),
                longLabel = info.longLabel?.toString().orEmpty(),
                isDynamic = info.isDynamic,
                isDeclaredInManifest = info.isDeclaredInManifest
            )
        }
        return result.filter { it.packageName == packageName }
    }

    override fun launchShortcut(app: LauncherApp, shortcutId: String): Boolean {
        // Shortcut launching is only available from API 25 onwards, and only for packages that
        // are still installed and launchable.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return false
        if (!app.isInstalledApp) return false
        if (shortcutId.isBlank()) return false

        val launcherApps = appContext.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps
            ?: return false

        return try {
            launcherApps.startShortcut(
                app.packageName,
                shortcutId,
                null,
                null,
                Process.myUserHandle()
            )
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
        iconCache.get(key)?.let { return it }

        val icon = try {
            val drawable = loader() ?: return null
            val density = appContext.resources.displayMetrics.density
            val sizePx = (64f * density).toInt().coerceIn(96, 192)
            drawable.toBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888).asImageBitmap()
        } catch (_: Exception) {
            null
        } ?: return null

        // Do not cache failed decodes; a transient failure should be retried on the next refresh.
        iconCache.put(key, icon)
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
            iconCache.clear()
        }
        packageVersion.update { it + 1 }
    }
}
