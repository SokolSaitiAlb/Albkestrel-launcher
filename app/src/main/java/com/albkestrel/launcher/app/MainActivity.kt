package com.albkestrel.launcher.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.albkestrel.launcher.core.designsystem.AlbkestrelTheme
import com.albkestrel.launcher.domain.model.AnimationSpeed
import com.albkestrel.launcher.feature.appdrawer.AppDrawerScreen
import com.albkestrel.launcher.feature.common.LauncherOverlay
import com.albkestrel.launcher.feature.common.LauncherUiState
import com.albkestrel.launcher.feature.common.LauncherViewModel
import com.albkestrel.launcher.feature.common.PrototypeInfoDialog
import com.albkestrel.launcher.feature.folders.FolderModalScreen
import com.albkestrel.launcher.feature.home.HomeScreen
import com.albkestrel.launcher.feature.search.SearchScreen
import com.albkestrel.launcher.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            AlbkestrelTheme(
                themeMode = uiState.settings.themeMode,
                dynamicColor = uiState.settings.dynamicColor
            ) {
                LauncherRoot(viewModel = viewModel, uiState = uiState)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // When user taps Home or Home gesture navigates back to launcher, collapse active overlay or return to page 0
        viewModel.onHomeIntentReceived()
    }
}

@Composable
fun LauncherRoot(
    viewModel: LauncherViewModel,
    uiState: LauncherUiState,
    modifier: Modifier = Modifier
) {
    val animationSpeed = uiState.settings.animationSpeed

    val enterSlideSpec = when (animationSpeed) {
        AnimationSpeed.FLUID_SPRING -> spring<androidx.compose.ui.unit.IntOffset>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        AnimationSpeed.SNAPPY -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
        AnimationSpeed.REDUCED_MOTION -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }

    val enterScaleSpec = when (animationSpeed) {
        AnimationSpeed.FLUID_SPRING -> spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        AnimationSpeed.SNAPPY -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
        AnimationSpeed.REDUCED_MOTION -> spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Base Home Screen
        HomeScreen(
            uiState = uiState,
            viewModel = viewModel
        )

        // Overlay: App Drawer
        AnimatedVisibility(
            visible = uiState.activeOverlay is LauncherOverlay.AppDrawer,
            enter = slideInVertically(
                animationSpec = enterSlideSpec,
                initialOffsetY = { it }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = enterSlideSpec,
                targetOffsetY = { it }
            ) + fadeOut()
        ) {
            AppDrawerScreen(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        // Overlay: Search
        AnimatedVisibility(
            visible = uiState.activeOverlay is LauncherOverlay.Search,
            enter = slideInVertically(
                animationSpec = enterSlideSpec,
                initialOffsetY = { -it / 2 }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = enterSlideSpec,
                targetOffsetY = { -it / 2 }
            ) + fadeOut()
        ) {
            SearchScreen(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        // Overlay: Settings
        AnimatedVisibility(
            visible = uiState.activeOverlay is LauncherOverlay.Settings,
            enter = slideInVertically(
                animationSpec = enterSlideSpec,
                initialOffsetY = { it }
            ) + fadeIn(),
            exit = slideOutVertically(
                animationSpec = enterSlideSpec,
                targetOffsetY = { it }
            ) + fadeOut()
        ) {
            SettingsScreen(
                uiState = uiState,
                viewModel = viewModel
            )
        }

        // Modal: Folder Expansion
        val activeOverlay = uiState.activeOverlay
        AnimatedVisibility(
            visible = activeOverlay is LauncherOverlay.FolderExpanded,
            enter = scaleIn(
                animationSpec = enterScaleSpec,
                initialScale = 0.85f
            ) + fadeIn(),
            exit = scaleOut(
                animationSpec = enterScaleSpec,
                targetScale = 0.85f
            ) + fadeOut()
        ) {
            if (activeOverlay is LauncherOverlay.FolderExpanded) {
                FolderModalScreen(
                    folder = activeOverlay.folder,
                    uiState = uiState,
                    viewModel = viewModel
                )
            }
        }

        // Modal: Prototype Info Dialog
        AnimatedVisibility(
            visible = activeOverlay is LauncherOverlay.PrototypeInfo,
            enter = scaleIn(animationSpec = enterScaleSpec, initialScale = 0.9f) + fadeIn(),
            exit = scaleOut(animationSpec = enterScaleSpec, targetScale = 0.9f) + fadeOut()
        ) {
            PrototypeInfoDialog(
                onDismiss = { viewModel.closeOverlay() }
            )
        }
    }
}
