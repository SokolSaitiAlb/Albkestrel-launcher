package com.albkestrel.launcher.feature.appdrawer

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.R
import com.albkestrel.launcher.core.designsystem.LivingAppIcon
import com.albkestrel.launcher.core.designsystem.LuminousSurface
import com.albkestrel.launcher.domain.model.AppCategory
import com.albkestrel.launcher.domain.search.AppSearch
import com.albkestrel.launcher.feature.common.LauncherUiState
import com.albkestrel.launcher.feature.common.LauncherViewModel

@Composable
fun AppDrawerScreen(
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeOverlay()
    }

    var drawerSearchQuery by remember { mutableStateOf("") }
    val closeDrawerLabel = stringResource(R.string.a11y_close_app_drawer)

    // Filter apps according to drawerCategory and drawerSearchQuery
    val filteredApps = remember(uiState.allApps, uiState.drawerCategory, drawerSearchQuery) {
        AppSearch.filterByLabel(
            AppSearch.filterByCategory(uiState.allApps, uiState.drawerCategory),
            drawerSearchQuery
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("app_drawer_screen")
            .background(Color.Black.copy(alpha = 0.65f))
    ) {
        // Frosted living drawer container
        LuminousSurface(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            backgroundColor = Color(0xFF0B1120).copy(alpha = 0.92f),
            borderColor = Color(0xFF00E5FF).copy(alpha = 0.25f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 10.dp)
            ) {
                // Drag dismiss handle & Close button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 88.dp, minHeight = 48.dp)
                            .semantics(mergeDescendants = true) {
                                role = Role.Button
                                contentDescription = closeDrawerLabel
                            }
                            .clickable(onClick = { viewModel.closeOverlay() }),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(44.dp)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                        )
                    }

                    IconButton(
                        onClick = { viewModel.closeOverlay() },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = closeDrawerLabel,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Drawer Search Bar
                OutlinedTextField(
                    value = drawerSearchQuery,
                    onValueChange = { drawerSearchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search all installed apps...",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 14.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null,
                            tint = Color(0xFF00E5FF)
                        )
                    },
                    trailingIcon = {
                        if (drawerSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { drawerSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Rounded.Clear,
                                    contentDescription = stringResource(R.string.a11y_clear_search),
                                    tint = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.06f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                        focusedBorderColor = Color(0xFF00E5FF).copy(alpha = 0.6f),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.14f),
                        cursorColor = Color(0xFF00E5FF)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("drawer_search_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category Filter Tabs
                ScrollableTabRow(
                    selectedTabIndex = AppCategory.values().indexOf(uiState.drawerCategory),
                    edgePadding = 16.dp,
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFF00E5FF),
                    divider = {},
                    indicator = { tabPositions ->
                        val index = AppCategory.values().indexOf(uiState.drawerCategory)
                        if (index in tabPositions.indices) {
                            Box(
                                Modifier
                                    .tabIndicatorOffset(tabPositions[index])
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                                    .background(Color(0xFF00E5FF))
                            )
                        }
                    }
                ) {
                    AppCategory.values().forEach { category ->
                        val isSelected = uiState.drawerCategory == category
                        Tab(
                            selected = isSelected,
                            onClick = { viewModel.setDrawerCategory(category) },
                            text = {
                                Text(
                                    text = category.displayName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) Color(0xFF00E5FF) else Color.White.copy(alpha = 0.65f)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // App Grid
                when {
                    uiState.isLoadingApps && uiState.allApps.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color = Color(0xFF00E5FF),
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Loading installed apps...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    filteredApps.isEmpty() -> {
                        val emptyMessage = when {
                            drawerSearchQuery.isNotBlank() ->
                                "No installed apps match \"$drawerSearchQuery\""
                            uiState.drawerCategory != AppCategory.ALL ->
                                "No ${uiState.drawerCategory.displayName.lowercase()} apps installed"
                            else -> "No launchable apps found"
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = emptyMessage,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                    else -> {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(uiState.settings.gridColumns),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("drawer_apps_grid")
                        ) {
                            items(filteredApps, key = { it.id }) { app ->
                                LivingAppIcon(
                                    app = app,
                                    iconSize = uiState.settings.iconSize.dp,
                                    showLabel = true,
                                    labelColor = Color.White.copy(alpha = 0.9f),
                                    animationSpeed = uiState.settings.animationSpeed,
                                    onClick = {
                                        viewModel.onAppClicked(app)
                                    },
                                    onLongPress = { pressedApp, anchorPosition ->
                                        viewModel.onAppLongPressed(pressedApp, anchorPosition)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
