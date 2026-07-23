package com.example.concom.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.concom.ui.theme.DeepBlack

enum class AppMode {
    COMPRESS_SINGLE, COMPRESS_MULTI, CONVERT, BOTH
}

@Composable
fun ModeSelectionScreen(onModeSelected: (AppMode) -> Unit) {
    var showCompressSubMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (showCompressSubMenu) "COMPRESSION TYPE" else "SELECT ENGINE",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge.copy(
                    letterSpacing = 8.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 32.dp)
            )

            AnimatedContent(
                targetState = showCompressSubMenu,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "SelectionTransition"
            ) { isSubMenu ->
                if (isSubMenu) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModeCard(
                                title = "SINGLE",
                                subtitle = "One Image",
                                icon = Icons.Default.Photo,
                                color = Color(0xFF00FF41),
                                modifier = Modifier.weight(1f),
                                onClick = { onModeSelected(AppMode.COMPRESS_SINGLE) }
                            )
                            ModeCard(
                                title = "MULTIPLE",
                                subtitle = "Batch Process",
                                icon = Icons.Default.PhotoLibrary,
                                color = Color(0xFF00FF41),
                                modifier = Modifier.weight(1f),
                                onClick = { onModeSelected(AppMode.COMPRESS_MULTI) }
                            )
                        }
                        
                        TextButton(
                            onClick = { showCompressSubMenu = false },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text("Back to Engines", color = Color.Gray)
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ModeCard(
                                title = "COMPRESS",
                                subtitle = "Reduce Size",
                                icon = Icons.Default.Compress,
                                color = Color(0xFF00FF41),
                                modifier = Modifier.weight(1f),
                                onClick = { showCompressSubMenu = true }
                            )
                            
                            ModeCard(
                                title = "CONVERT",
                                subtitle = "Change Format",
                                icon = Icons.Default.Transform,
                                color = Color(0xFFD0BCFF),
                                modifier = Modifier.weight(1f),
                                onClick = { onModeSelected(AppMode.CONVERT) }
                            )
                        }

                        ModeCard(
                            title = "COMPRESS & CONVERT",
                            subtitle = "Full Optimization Engine",
                            icon = Icons.Default.AutoMode,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            onClick = { onModeSelected(AppMode.BOTH) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(200.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF0F0F0F))
            .clickable(onClick = onClick)
            .padding(2.dp)
            .background(
                Brush.verticalGradient(
                    listOf(color.copy(alpha = 0.1f), Color.Transparent)
                ),
                RoundedCornerShape(32.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                fontSize = 14.sp
            )
            Text(
                text = subtitle,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
