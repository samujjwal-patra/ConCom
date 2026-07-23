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
    COMPRESS_SINGLE, COMPRESS_MULTI, CONVERT_SINGLE, CONVERT_MULTI, BOTH_SINGLE, BOTH_MULTI
}

enum class SelectionState {
    MAIN, COMPRESS_SUB, CONVERT_SUB, BOTH_SUB
}

@Composable
fun ModeSelectionScreen(onModeSelected: (AppMode) -> Unit) {
    var selectionState by remember { mutableStateOf(SelectionState.MAIN) }

    Surface(modifier = Modifier.fillMaxSize(), color = DeepBlack) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding().padding(24.dp)) {
            Text(
                text = "IMAGE ENGINE",
                color = Color.White.copy(alpha = 0.5f),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
            )
            
            Text(
                text = when(selectionState) {
                    SelectionState.MAIN -> "Process Studio"
                    SelectionState.COMPRESS_SUB -> "Size Optimization"
                    SelectionState.CONVERT_SUB -> "Format Migration"
                    SelectionState.BOTH_SUB -> "Full Engine"
                },
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black),
                modifier = Modifier.padding(top = 8.dp, bottom = 48.dp)
            )

            AnimatedContent(
                targetState = selectionState,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SelectionTransition"
            ) { state ->
                when (state) {
                    SelectionState.COMPRESS_SUB -> SubMenuPortal(
                        color = Color(0xFF00FF41),
                        onSingleClick = { onModeSelected(AppMode.COMPRESS_SINGLE) },
                        onMultiClick = { onModeSelected(AppMode.COMPRESS_MULTI) },
                        onBack = { selectionState = SelectionState.MAIN }
                    )
                    SelectionState.CONVERT_SUB -> SubMenuPortal(
                        color = Color(0xFFD0BCFF),
                        onSingleClick = { onModeSelected(AppMode.CONVERT_SINGLE) },
                        onMultiClick = { onModeSelected(AppMode.CONVERT_MULTI) },
                        onBack = { selectionState = SelectionState.MAIN }
                    )
                    SelectionState.BOTH_SUB -> SubMenuPortal(
                        color = Color(0xFF00E5FF),
                        onSingleClick = { onModeSelected(AppMode.BOTH_SINGLE) },
                        onMultiClick = { onModeSelected(AppMode.BOTH_MULTI) },
                        onBack = { selectionState = SelectionState.MAIN }
                    )
                    SelectionState.MAIN -> MainMenu(
                        onCompressClick = { selectionState = SelectionState.COMPRESS_SUB },
                        onConvertClick = { selectionState = SelectionState.CONVERT_SUB },
                        onBothClick = { selectionState = SelectionState.BOTH_SUB }
                    )
                }
            }
        }
    }
}

@Composable
fun MainMenu(onCompressClick: () -> Unit, onConvertClick: () -> Unit, onBothClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        StudioEngineCard("Compress", "Drastically shrink file sizes", Icons.Default.Compress, Color(0xFF00FF41), onCompressClick)
        StudioEngineCard("Convert", "Migrate to modern formats", Icons.Default.Transform, Color(0xFFD0BCFF), onConvertClick)
        StudioEngineCard("Full Engine", "Deep size & format optimization", Icons.Default.AutoMode, Color(0xFF00E5FF), onBothClick)
    }
}

@Composable
fun StudioEngineCard(title: String, desc: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth().height(140.dp).clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF111111)).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .background(Brush.linearGradient(colors = listOf(color.copy(alpha = 0.15f), Color.Transparent)))
        )
        Row(modifier = Modifier.fillMaxSize().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(64.dp).background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(20.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(32.dp))
            }
            Column(modifier = Modifier.padding(start = 20.dp)) {
                Text(text = title.uppercase(), color = Color.White, fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp, fontSize = 18.sp)
                Text(text = desc, color = Color.Gray, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null, tint = Color.DarkGray)
        }
    }
}

@Composable
fun SubMenuPortal(color: Color, onSingleClick: () -> Unit, onMultiClick: () -> Unit, onBack: () -> Unit) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PortalCard("Single", "Precision Edit", Icons.Default.FilterCenterFocus, color, Modifier.weight(1f), onSingleClick)
            PortalCard("Batch", "High Speed", Icons.Default.AllLayers, color, Modifier.weight(1f), onMultiClick)
        }
        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
            Spacer(Modifier.width(8.dp))
            Text("Back to Selection", color = Color.Gray, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PortalCard(title: String, subtitle: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier.height(220.dp).clip(RoundedCornerShape(32.dp))
            .background(Color(0xFF111111)).clickable(onClick = onClick).padding(2.dp)
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.2f), Color.Transparent)), RoundedCornerShape(32.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(title.uppercase(), color = Color.White, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
            Text(subtitle, color = Color.Gray, fontSize = 12.sp)
        }
    }
}
