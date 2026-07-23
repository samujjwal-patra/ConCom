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
    
    val labelAlpha by animateFloatAsState(if (isDragging) 0f else 1f, label = "LabelAlpha")
    val handleScale by animateFloatAsState(if (isDragging) 1.2f else 1f, label = "HandleScale")

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth().aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp)).clipToBounds()
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val density = LocalDensity.current
        
        val fullWidth = with(density) { widthPx.toDp() }
        val fullHeight = with(density) { heightPx.toDp() }

        // Bottom Layer: Processed
        AsyncImage(model = processedUri, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        ComparisonLabel("OPTIMIZED", Alignment.BottomEnd, labelAlpha * 0.7f)
        
        // Top Layer: Original
        Box(modifier = Modifier.fillMaxHeight().width(with(density) { (widthPx * sliderPosition).toDp() }).clipToBounds()) {
            AsyncImage(model = originalUri, contentDescription = null, modifier = Modifier.size(fullWidth, fullHeight), contentScale = ContentScale.Crop)
            ComparisonLabel("ORIGINAL", Alignment.BottomStart, labelAlpha * 0.7f)
        }
        
        // Divider
        Box(
            modifier = Modifier
                .offset { IntOffset((widthPx * sliderPosition).roundToInt() - 1.dp.toPx().toInt(), 0) }
                .fillMaxHeight().width(2.dp).background(Color.White).shadow(4.dp)
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
                .size(48.dp).scale(handleScale).shadow(8.dp, CircleShape)
                .background(Color.White, CircleShape).border(1.dp, Color.LightGray, CircleShape)
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
            Icon(Icons.Default.UnfoldMore, null, tint = Color.Black, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun ComparisonLabel(text: String, alignment: Alignment, alpha: Float) {
    Box(modifier = Modifier.fillMaxSize().padding(12.dp).alpha(alpha), contentAlignment = alignment) {
        Text(
            text = text, color = Color.White,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
            modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
