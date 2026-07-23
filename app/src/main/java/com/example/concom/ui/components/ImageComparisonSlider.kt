package com.example.concom.ui.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun ImageComparisonSlider(
    originalUri: Uri?,
    processedUri: Uri?,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    var isDragging by remember { mutableStateOf(false) }
    
    val labelAlpha by animateFloatAsState(
        targetValue = if (isDragging) 0f else 1f,
        label = "LabelAlpha"
    )

    val handleScale by animateFloatAsState(
        targetValue = if (isDragging) 1.2f else 1f,
        label = "HandleScale"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .clipToBounds()
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        
        val fullWidth = with(density) { widthPx.toDp() }
        val fullHeight = with(density) { heightPx.toDp() }

        // Bottom Layer: Processed Image
        AsyncImage(
            model = processedUri,
            contentDescription = "Processed Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Label for Optimized
        Text(
            text = "OPTIMIZED",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .alpha(labelAlpha * 0.7f)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        
        // Top Layer: Original Image (Clipped)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(density) { (widthPx * sliderPosition).toDp() })
                .clipToBounds()
        ) {
            AsyncImage(
                model = originalUri,
                contentDescription = "Original Image",
                modifier = Modifier.size(fullWidth, fullHeight),
                contentScale = ContentScale.Crop
            )
            
            // Label for Original
            Text(
                text = "ORIGINAL",
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(12.dp)
                    .alpha(labelAlpha * 0.7f)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        
        // Divider Line with Glow
        Box(
            modifier = Modifier
                .offset { IntOffset((widthPx * sliderPosition).roundToInt() - 1.dp.toPx().toInt(), 0) }
                .fillMaxHeight()
                .width(2.dp)
                .background(Color.White)
                .shadow(elevation = 4.dp, spotColor = Color.Black)
        )
        
        // Handle
        Box(
            modifier = Modifier
                .offset { 
                    IntOffset(
                        (widthPx * sliderPosition).roundToInt() - 24.dp.toPx().toInt(),
                        (heightPx / 2).roundToInt() - 24.dp.toPx().toInt()
                    ) 
                }
                .size(48.dp)
                .scale(handleScale)
                .shadow(8.dp, CircleShape)
                .background(Color.White, CircleShape)
                .border(1.dp, Color.LightGray, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false }
                    ) { change, dragAmount ->
                        change.consume()
                        sliderPosition = (sliderPosition + dragAmount.x / widthPx).coerceIn(0f, 1f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.UnfoldMore,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
