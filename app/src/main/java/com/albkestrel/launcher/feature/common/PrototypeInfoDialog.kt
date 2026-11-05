package com.albkestrel.launcher.feature.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albkestrel.launcher.R
import com.albkestrel.launcher.core.designsystem.LuminousSurface

@Composable
fun PrototypeInfoDialog(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("prototype_info_dialog")
            .background(Color.Black.copy(alpha = 0.75f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        LuminousSurface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume */ },
            shape = RoundedCornerShape(28.dp),
            backgroundColor = Color(0xFF0F172A).copy(alpha = 0.96f),
            borderColor = Color(0xFF00E5FF).copy(alpha = 0.4f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF00E5FF).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = Color(0xFF00E5FF),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AlbkestrelOS Prototype",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "Phase 1: Persistent Launcher Foundation",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.a11y_dismiss_dialog),
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Section 1: Implemented
                ScopeSection(
                    title = "Verified & Implemented Features (Phase 1)",
                    iconColor = Color(0xFF10B981),
                    items = listOf(
                        "Room Database persistence for Home pages, items, dock, and folders",
                        "DataStore Preferences settings with SharedPreferences migration",
                        "Real-time battery telemetry via Android system broadcast receiver",
                        "HOME singleTask lifecycle & onNewIntent() overlay collapse",
                        "App Drawer with real installed-app discovery, icons, and launch intents",
                        "Category-based filtering and instant search with empty states",
                        "Folder expansion modal with editable titles persisted to Room",
                        "Luminous frosted dock shelf with persistent dock items",
                        "Pixel 8a edge-to-edge support with safe drawing insets"
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Mocked Data
                ScopeSection(
                    title = "Mocked Features (Clearly Labeled)",
                    iconColor = Color(0xFFF59E0B),
                    items = listOf(
                        "Weather Widget uses labeled mock data (no network weather API)",
                        "Digital Wellbeing & Media Player widgets use interactive mock state",
                        "Wifi & Cellular indicators in telemetry bar are decorative icons"
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Section 3: Limitations
                ScopeSection(
                    title = "Scope Limitations (Deferred to Later Phases)",
                    iconColor = Color(0xFFEF4444),
                    items = listOf(
                        "AppWidgetHost system provider (hosting third-party Android widgets)",
                        "Quickstep Recents & OS overview task switching integration",
                        "Full interactive drag-and-drop workspace reorganization",
                        "Multi-profile / Work Profile launcher separation"
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E5FF),
                        contentColor = Color(0xFF00363F)
                    )
                ) {
                    Text(
                        text = "Understood",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeSection(
    title: String,
    iconColor: Color,
    items: List<String>
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            ),
            color = iconColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    color = iconColor,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodySmall.copy(lineHeight = 16.sp),
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
