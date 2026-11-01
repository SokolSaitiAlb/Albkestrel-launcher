package com.albkestrel.launcher.feature.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.R
import com.albkestrel.launcher.core.designsystem.FolderIcon
import com.albkestrel.launcher.core.designsystem.LivingAppIcon
import com.albkestrel.launcher.core.designsystem.LuminousSurface
import com.albkestrel.launcher.domain.model.WorkspaceItem
import com.albkestrel.launcher.domain.model.WorkspaceItemType
import com.albkestrel.launcher.feature.common.LauncherOverlay
import com.albkestrel.launcher.feature.common.LauncherUiState
import com.albkestrel.launcher.feature.common.LauncherViewModel

@Composable
fun HomeScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = uiState.homePageIndex,
        pageCount = { 2 }
    )

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.setHomePage(page)
        }
    }

    LaunchedEffect(uiState.homePageIndex) {
        if (pagerState.currentPage != uiState.homePageIndex) {
            pagerState.animateScrollToPage(uiState.homePageIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -35f) {
                        viewModel.setOverlay(LauncherOverlay.AppDrawer)
                    } else if (dragAmount > 45f) {
                        viewModel.setOverlay(LauncherOverlay.Search)
                    }
                }
            }
    ) {
        // Wallpaper Layer
        Image(
            painter = painterResource(id = R.drawable.albkestrel_wallpaper),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Wallpaper Dimming & Living Tint Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = uiState.settings.wallpaperDim * 0.7f),
                            Color(0xFF080D1A).copy(alpha = uiState.settings.wallpaperDim),
                            Color(0xFF040710).copy(alpha = (uiState.settings.wallpaperDim + 0.25f).coerceAtMost(0.9f))
                        )
                    )
                )
        )

        // Main Vertical Stack
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Navigation & Quick Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Living Search Bar Pill
                LuminousSurface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("quick_search_pill")
                        .clickable {
                            viewModel.setOverlay(LauncherOverlay.Search)
                        },
                    shape = RoundedCornerShape(22.dp),
                    backgroundColor = Color.Black.copy(alpha = 0.26f),
                    borderColor = Color.White.copy(alpha = 0.18f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = "Search",
                            tint = Color.White.copy(alpha = 0.75f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Search AlbkestrelOS apps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 13.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Prototype Info Button
                IconButton(
                    onClick = {
                        viewModel.setOverlay(LauncherOverlay.PrototypeInfo)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .testTag("prototype_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = "Prototype Info",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Settings Gear Button
                IconButton(
                    onClick = {
                        viewModel.setOverlay(LauncherOverlay.Settings)
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.14f))
                        .testTag("settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "Launcher Settings",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Horizontal Workspace Pages
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                if (page == 0) {
                    // Page 1: Clock, Weather & Data-Backed Persistent Workspace Grid
                    WorkspaceMainPage(
                        uiState = uiState,
                        workspaceItems = uiState.workspaceItems,
                        viewModel = viewModel
                    )
                } else {
                    // Page 2: Living Flow Glance Widgets
                    WorkspaceGlancePage(
                        uiState = uiState,
                        viewModel = viewModel
                    )
                }
            }

            // Morphing Page Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) { pageIndex ->
                    val isSelected = pagerState.currentPage == pageIndex
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 20.dp else 6.dp,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "dot_width"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .height(6.dp)
                            .width(width)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.35f)
                            )
                    )
                }
            }

            // Minimal Luminous Dock (loaded from persistent dock items)
            LuminousDock(
                dockItems = uiState.dockItems,
                iconSize = uiState.settings.iconSize.dp,
                showLabels = uiState.settings.showDockLabels,
                animationSpeed = uiState.settings.animationSpeed,
                onAppClick = { app -> viewModel.onAppClicked(app) },
                onOpenDrawer = {
                    viewModel.setOverlay(LauncherOverlay.AppDrawer)
                }
            )

            Spacer(modifier = Modifier.height(6.dp))
        }
    }
}

@Composable
private fun WorkspaceMainPage(
    uiState: LauncherUiState,
    workspaceItems: List<WorkspaceItem>,
    viewModel: LauncherViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Clock & Date Widget with real battery telemetry
        ClockDateWidget(
            batteryStatus = uiState.batteryStatus,
            onWidgetClick = {
                uiState.allApps.find { it.id == "app_clock" }?.let { viewModel.onAppClicked(it) }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Weather Mock Widget
        WeatherMockWidget(
            weather = uiState.weather,
            onClick = {
                uiState.allApps.find { it.id == "app_weather" }?.let { viewModel.onAppClicked(it) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Persistent Workspace Grid
        val columns = uiState.settings.gridColumns
        val displayedItems = workspaceItems.take(if (columns == 4) 8 else 10)
        val rowChunks = displayedItems.chunked(columns)

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            rowChunks.take(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.Top
                ) {
                    rowItems.forEach { item ->
                        when (item.itemType) {
                            WorkspaceItemType.APPLICATION -> {
                                item.app?.let { app ->
                                    LivingAppIcon(
                                        app = app,
                                        iconSize = uiState.settings.iconSize.dp,
                                        showLabel = true,
                                        labelColor = Color.White,
                                        animationSpeed = uiState.settings.animationSpeed,
                                        isDimmed = !item.isInstalled,
                                        onClick = { viewModel.onAppClicked(app) }
                                    )
                                }
                            }
                            WorkspaceItemType.FOLDER -> {
                                item.folder?.let { folder ->
                                    FolderIcon(
                                        folder = folder,
                                        iconSize = uiState.settings.iconSize.dp,
                                        labelColor = Color.White,
                                        animationSpeed = uiState.settings.animationSpeed,
                                        onClick = { viewModel.openFolder(folder) }
                                    )
                                }
                            }
                            WorkspaceItemType.WIDGET -> {
                                // Widget slot reserved for future Phase 4
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkspaceGlancePage(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Living Flow Dashboard",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                ),
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF00E5FF).copy(alpha = 0.16f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "LUMINOUS PREVIEW",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        letterSpacing = 0.8.sp
                    ),
                    color = Color(0xFF00E5FF)
                )
            }
        }

        // Living Media Glance Widget
        MediaGlanceWidget(
            isPlaying = uiState.isMediaPlaying,
            trackName = uiState.mediaTrackName,
            artistName = uiState.mediaArtist,
            onTogglePlay = { viewModel.toggleMediaPlayback() }
        )

        // Wellbeing / Wellness Widget
        WellbeingGlanceWidget(
            onOpenSettings = {
                viewModel.setOverlay(LauncherOverlay.Settings)
            }
        )

        // AlbkestrelOS Intelligence Quick Note Card
        LuminousSurface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    viewModel.setOverlay(LauncherOverlay.PrototypeInfo)
                },
            shape = RoundedCornerShape(24.dp),
            backgroundColor = Color(0xFF1E293B).copy(alpha = 0.45f),
            borderColor = Color.White.copy(alpha = 0.15f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Living Flow Principles",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "1. Luminous Effect: Restrained translucent surfaces & specular depth.\n2. Fluid Motion: Continuous spring physics without abrupt jumps.\n3. Flexible Feedback: Reactive haptics and touch scaling.",
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 18.sp),
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
