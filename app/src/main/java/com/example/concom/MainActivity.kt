package com.example.concom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.concom.ui.theme.ConComTheme
import com.example.concom.ui.theme.DeepBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConComTheme(darkTheme = true) {
                var showSplash by remember { mutableStateOf(true) }

                AnimatedContent(
                    targetState = showSplash,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(800)) togetherWith
                                fadeOut(animationSpec = tween(800))
                    },
                    label = "SplashTransition"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(onAnimationFinish = { showSplash = false })
                    } else {
                        MainScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val logoProgress = remember { Animatable(0f) }
    val scannerOffset = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        launch {
            logoProgress.animateTo(1f, tween(1000, easing = EaseInOutQuart))
        }
        delay(300)
        launch {
            scannerOffset.animateTo(1f, tween(1400, easing = EaseInOutQuart))
        }
        launch {
            delay(500)
            textAlpha.animateTo(1f, tween(1000))
            contentScale.animateTo(1f, tween(1000, easing = EaseInOutQuart))
        }
        delay(2200)
        onAnimationFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Creative Background: Floating digital particles or just the scan glow
            val scannerGreen = Color(0xFF00FF41)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val y = scannerOffset.value * size.height
                
                // Scanner Glow Trail
                if (scannerOffset.value > 0f && scannerOffset.value < 1f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                scannerGreen.copy(alpha = 0f),
                                scannerGreen.copy(alpha = 0.15f),
                                Color.Transparent
                            ),
                            startY = y - 300.dp.toPx(),
                            endY = y
                        ),
                        topLeft = androidx.compose.ui.geometry.Offset(0f, y - 300.dp.toPx()),
                        size = size.copy(height = 300.dp.toPx())
                    )
                    
                    // The "Laser" Line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, scannerGreen, Color.Transparent)
                        ),
                        start = androidx.compose.ui.geometry.Offset(0f, y),
                        end = androidx.compose.ui.geometry.Offset(size.width, y),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .scale(contentScale.value),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ConComLogo(
                    progress = logoProgress.value,
                    modifier = Modifier.size(150.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CONCOM",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 16.sp
                        ),
                        modifier = Modifier
                            .alpha(textAlpha.value)
                            .graphicsLayer {
                                // Subtle tilt for "creative" look
                                rotationX = (1f - textAlpha.value) * 20f
                            }
                    )

                    Text(
                        text = "PRECISION IMAGE ENGINE",
                        color = scannerGreen.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 8.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .alpha(textAlpha.value)
                            .padding(top = 12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConComLogo(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 12.dp.toPx()
        val scannerGreen = Color(0xFF00FF41)
        val brandPurple = Color(0xFFD0BCFF)
        
        // 1. Outer Glow (Faked with multiple arcs)
        drawArc(
            brush = Brush.sweepGradient(listOf(brandPurple, scannerGreen, brandPurple)),
            startAngle = 150f,
            sweepAngle = 240f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth * 1.5f, cap = StrokeCap.Round),
            alpha = 0.2f * progress
        )

        // 2. Main Outer Arc
        drawArc(
            brush = Brush.linearGradient(listOf(brandPurple, scannerGreen)),
            startAngle = 150f,
            sweepAngle = 240f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // 3. Inner "Conversion" element (Square to Circle transition feel)
        if (progress > 0.4f) {
            val p = (progress - 0.4f) / 0.6f
            val innerSize = size.width * 0.4f
            val offset = (size.width - innerSize) / 2
            
            // Draw a "processing" line or arrow
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * p,
                useCenter = false,
                style = Stroke(width = strokeWidth / 3, cap = StrokeCap.Round),
                topLeft = androidx.compose.ui.geometry.Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(innerSize, innerSize)
            )
        }
    }
}

@Composable
fun MainScreen() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Bento Grid & Visual Slider",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    "Ready to process",
                    color = Color.Gray,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
