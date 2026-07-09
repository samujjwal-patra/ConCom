package com.example.concom.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlin.math.roundToInt

@Composable
fun ImageComparisonSlider(
    originalUri: Uri?,
    processedUri: Uri?,
    modifier: Modifier = Modifier
) {
    var sliderPosition by remember { mutableStateOf(0.5f) }
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
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
        }
        
        // Divider Line
        Box(
            modifier = Modifier
                .offset { IntOffset((widthPx * sliderPosition).roundToInt() - 1.dp.toPx().toInt(), 0) }
                .fillMaxHeight()
                .width(2.dp)
                .background(Color.White.copy(alpha = 0.8f))
        )
        
        // Handle
        Box(
            modifier = Modifier
                .offset { 
                    IntOffset(
                        (widthPx * sliderPosition).roundToInt() - 20.dp.toPx().toInt(),
                        (heightPx / 2).roundToInt() - 20.dp.toPx().toInt()
                    ) 
                }
                .size(40.dp)
                .background(Color.White, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
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
