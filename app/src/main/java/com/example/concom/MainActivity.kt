package com.example.concom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import com.example.concom.ui.screens.AppMode
import com.example.concom.ui.screens.BentoDashboard
import com.example.concom.ui.screens.ModeSelectionScreen
import com.example.concom.ui.theme.ConComTheme
import com.example.concom.ui.theme.DeepBlack
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ConComTheme(darkTheme = true) {
                var currentScreen by remember { mutableStateOf("splash") }
                var selectedMode by remember { mutableStateOf<AppMode?>(null) }
                var showExitDialog by remember { mutableStateOf(false) }

                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Exit ConCom?") },
                        text = { Text("Are you sure you want to close the app?") },
                        confirmButton = {
                            Button(
                                onClick = { finish() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00FF41), contentColor = Color.Black)
                            ) {
                                Text("Exit")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showExitDialog = false }) {
                                Text("Cancel", color = Color.White)
                            }
                        },
                        containerColor = Color(0xFF1A1A1A),
                        titleContentColor = Color.White,
                        textContentColor = Color.Gray
                    )
                }

                BackHandler {
                    when (currentScreen) {
                        "dashboard" -> currentScreen = "selection"
                        "selection" -> showExitDialog = true
                        else -> finish()
                    }
                }

                AnimatedContent(
                    targetState = currentScreen,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(1000)) togetherWith
                                fadeOut(animationSpec = tween(800))
                    },
                    label = "AppNavigation"
                ) { screen ->
                    when (screen) {
                        "splash" -> SplashScreen(onAnimationFinish = { currentScreen = "selection" })
                        "selection" -> ModeSelectionScreen(onModeSelected = { mode ->
                            selectedMode = mode
                            currentScreen = "dashboard"
                        })
                        "dashboard" -> BentoDashboard(mode = selectedMode ?: AppMode.COMPRESS_SINGLE)
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
    val logoFlare = remember { Animatable(0f) }

    // Professional floating effect
    val infiniteTransition = rememberInfiniteTransition(label = "FloatingEffect")
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutQuart),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatY"
    )

    LaunchedEffect(Unit) {
        // Parallelized Staggered Reveal
        launch {
            logoProgress.animateTo(1f, tween(1200, easing = EaseInOutQuart))
            // Flare effect once logo finishes
            logoFlare.animateTo(1f, tween(400))
            logoFlare.animateTo(0f, tween(800))
        }
        
        delay(400)
        launch {
            scannerOffset.animateTo(1f, tween(1800, easing = EaseInOutQuart))
        }
        
        launch {
            delay(600)
            textAlpha.animateTo(1f, tween(1200))
            contentScale.animateTo(1f, tween(1200, easing = EaseInOutQuart))
        }
        
        delay(3200) // Slightly longer to appreciate the "Precision" feel
        onAnimationFinish()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = DeepBlack
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val scannerGreen = Color(0xFF00FF41)
            
            // Technical Background: Dynamic Grid
            Canvas(modifier = Modifier.fillMaxSize()) {
                val gridStep = 40.dp.toPx()
                val scanY = scannerOffset.value * size.height
                
                // Draw Grid Lines
                for (x in 0 until (size.width / gridStep).toInt() + 1) {
                    val xPos = x * gridStep
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(xPos, 0f),
                        end = Offset(xPos, size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0 until (size.height / gridStep).toInt() + 1) {
                    val yPos = y * gridStep
                    val dist = abs(yPos - scanY)
                    // Grid lights up near the scanner
                    val gridAlpha = if (dist < 200.dp.toPx()) {
                        0.05f + (1f - dist / 200.dp.toPx()) * 0.15f
                    } else 0.05f
                    
                    drawLine(
                        color = Color.White.copy(alpha = gridAlpha),
                        start = Offset(0f, yPos),
                        end = Offset(size.width, yPos),
                        strokeWidth = 1f
                    )
                }

                // The Scanner "Laser" and Trail
                if (scannerOffset.value > 0f && scannerOffset.value < 1f) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                scannerGreen.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            startY = scanY - 400.dp.toPx(),
                            endY = scanY
                        ),
                        topLeft = Offset(0f, scanY - 400.dp.toPx()),
                        size = size.copy(height = 400.dp.toPx())
                    )
                    
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(Color.Transparent, scannerGreen, Color.Transparent)
                        ),
                        start = Offset(0f, scanY),
                        end = Offset(size.width, scanY),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer {
                        translationY = floatAnim
                        scaleX = contentScale.value
                        scaleY = contentScale.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Logo Flare Glow
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(scannerGreen.copy(alpha = 0.4f * logoFlare.value), Color.Transparent),
                                center = center,
                                radius = size.width / 2
                            )
                        )
                    }

                    ConComLogo(
                        progress = logoProgress.value,
                        modifier = Modifier.size(160.dp)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CONCOM",
                        color = Color.White,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 20.sp
                        ),
                        modifier = Modifier
                            .alpha(textAlpha.value)
                            .graphicsLayer {
                                rotationX = (1f - textAlpha.value) * 30f
                            }
                    )

                    Text(
                        text = "PRECISION IMAGE ENGINE",
                        color = scannerGreen.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.labelLarge.copy(
                            letterSpacing = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .alpha(textAlpha.value)
                            .padding(top = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ConComLogo(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val scannerGreen = Color(0xFF00FF41)
        val brandPurple = Color(0xFFD0BCFF)
        
        // Layered Glow effect
        drawArc(
            brush = Brush.sweepGradient(listOf(brandPurple, scannerGreen, brandPurple)),
            startAngle = 150f,
            sweepAngle = 240f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth * 1.8f, cap = StrokeCap.Round),
            alpha = 0.15f * progress
        )

        drawArc(
            brush = Brush.linearGradient(listOf(brandPurple, scannerGreen)),
            startAngle = 150f,
            sweepAngle = 240f * progress,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        if (progress > 0.4f) {
            val p = (progress - 0.4f) / 0.6f
            val innerSize = size.width * 0.45f
            val offset = (size.width - innerSize) / 2
            
            drawArc(
                color = Color.White,
                startAngle = -90f,
                sweepAngle = 360f * p,
                useCenter = false,
                style = Stroke(width = strokeWidth / 4, cap = StrokeCap.Round),
                topLeft = Offset(offset, offset),
                size = androidx.compose.ui.geometry.Size(innerSize, innerSize)
            )
        }
    }
}

