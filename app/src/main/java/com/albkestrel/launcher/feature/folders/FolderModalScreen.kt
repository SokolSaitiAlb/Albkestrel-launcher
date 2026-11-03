package com.albkestrel.launcher.feature.folders

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.core.designsystem.LivingAppIcon
import com.albkestrel.launcher.core.designsystem.LuminousSurface
import com.albkestrel.launcher.domain.model.WorkspaceFolder
import com.albkestrel.launcher.feature.common.LauncherUiState
import com.albkestrel.launcher.feature.common.LauncherViewModel

@Composable
fun FolderModalScreen(
    folder: WorkspaceFolder,
    uiState: LauncherUiState,
    viewModel: LauncherViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.closeOverlay()
    }

    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember { mutableStateOf(folder.title) }

    // Modal Backdrop
    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("folder_modal_${folder.id}")
            .background(Color.Black.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                if (isEditingTitle) {
                    viewModel.renameFolder(folder.id, editedTitle)
                    isEditingTitle = false
                }
                viewModel.closeOverlay()
            },
        contentAlignment = Alignment.Center
    ) {
        // Expanded Folder Living Surface Card
        LuminousSurface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks to prevent background dismiss */ }
                .testTag("folder_expansion_card"),
            shape = RoundedCornerShape(32.dp),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.94f),
            borderColor = Color(folder.accentColorArgb).copy(alpha = 0.45f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                // Header with Editable Title & Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isEditingTitle) {
                        OutlinedTextField(
                            value = editedTitle,
                            onValueChange = { editedTitle = it },
                            singleLine = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        viewModel.renameFolder(folder.id, editedTitle)
                                        isEditingTitle = false
                                    },
                                    modifier = Modifier.testTag("save_folder_title_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Save Title",
                                        tint = Color(0xFF00E5FF)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF00E5FF),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                cursorColor = Color(0xFF00E5FF)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_folder_title_input")
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    editedTitle = folder.title
                                    isEditingTitle = true
                                }
                                .testTag("folder_title_display")
                        ) {
                            Text(
                                text = folder.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 22.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit title",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (isEditingTitle) {
                                viewModel.renameFolder(folder.id, editedTitle)
                                isEditingTitle = false
                            }
                            viewModel.closeOverlay()
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                            .testTag("folder_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Folder",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Folder Apps Grid (3 columns)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(folder.items, key = { it.id }) { app ->
                        LivingAppIcon(
                            app = app,
                            iconSize = uiState.settings.iconSize.dp,
                            showLabel = true,
                            labelColor = Color.White,
                            animationSpeed = uiState.settings.animationSpeed,
                            isDimmed = !app.isInstalledApp,
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
