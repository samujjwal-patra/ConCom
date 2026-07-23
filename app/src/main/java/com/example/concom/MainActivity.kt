package com.example.concom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                    ExitConfirmationDialog(
                        onConfirm = { finish() },
                        onDismiss = { showExitDialog = false }
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
fun ExitConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exit ConCom?") },
        text = { Text("Are you sure you want to close the image engine?") },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FF41),
                    contentColor = Color.Black
                )
            ) {
                Text("Exit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color.White)
            }
        },
        containerColor = Color(0xFF1A1A1A),
        titleContentColor = Color.White,
        textContentColor = Color.Gray
    )
}

@Composable
fun SplashScreen(onAnimationFinish: () -> Unit) {
    val logoProgress = remember { Animatable(0f) }
    val scannerOffset = remember { Animatable(0f) }
    val textAlpha = remember { Animatable(0f) }
    val contentScale = remember { Animatable(0.96f) }
    val logoFlare = remember { Animatable(0f) }

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
        launch {
            logoProgress.animateTo(1f, tween(1200, easing = EaseInOutQuart))
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
        
        delay(3200)
        onAnimationFinish()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = DeepBlack) {
        Box(modifier = Modifier.fillMaxSize()) {
            val scannerGreen = Color(0xFF00FF41)
            
            TechnicalGrid(scannerOffset.value)

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

                BrandingSection(textAlpha.value, scannerGreen)
            }
        }
    }
}

@Composable
fun TechnicalGrid(scannerValue: Float) {
    val scannerGreen = Color(0xFF00FF41)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val gridStep = 40.dp.toPx()
        val scanY = scannerValue * size.height
        
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

        if (scannerValue in 0.01f..0.99f) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, scannerGreen.copy(alpha = 0.1f), Color.Transparent),
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
}

@Composable
fun BrandingSection(alpha: Float, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "CONCOM",
            color = Color.White,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 20.sp
            ),
            modifier = Modifier
                .alpha(alpha)
                .graphicsLayer { rotationX = (1f - alpha) * 30f }
        )

        Text(
            text = "PRECISION IMAGE ENGINE",
            color = accentColor.copy(alpha = 0.6f),
            style = MaterialTheme.typography.labelLarge.copy(
                letterSpacing = 10.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .alpha(alpha)
                .padding(top = 16.dp)
        )
    }
}

@Composable
fun ConComLogo(progress: Float, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 14.dp.toPx()
        val scannerGreen = Color(0xFF00FF41)
        val brandPurple = Color(0xFFD0BCFF)
        
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
