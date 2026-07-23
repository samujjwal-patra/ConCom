package com.example.concom.ui.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.concom.ui.components.ImageComparisonSlider
import com.example.concom.util.CompressionResult
import com.example.concom.util.ImageFormat
import com.example.concom.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class SizeUnit(val factor: Long) {
    BYTE(1L), KB(1024L), MB(1024L * 1024L)
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BentoDashboard(mode: AppMode = AppMode.COMPRESS_SINGLE) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val processor = remember { ImageProcessor(context) }
    
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedImagesUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    
    var processedResult by remember { mutableStateOf<CompressionResult?>(null) }
    var targetFormat by remember { mutableStateOf(ImageFormat.WEBP) }
    var quality by remember { mutableFloatStateOf(80f) }
    
    var useTargetSize by remember { mutableStateOf(false) }
    var targetSizeValue by remember { mutableStateOf("500") }
    var selectedUnit by remember { mutableStateOf(SizeUnit.KB) }
    
    var isProcessing by remember { mutableStateOf(value = false) }

    val isMultiMode = mode == AppMode.COMPRESS_MULTI

    val singlePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            processedResult = null
        }
    }

    val multiPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(),
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImagesUris = uris
            selectedImageUri = uris.first()
            processedResult = null
        }
    }

    LaunchedEffect(selectedImageUri, targetFormat, quality, useTargetSize, targetSizeValue, selectedUnit) {
        selectedImageUri?.let { uri ->
            isProcessing = true
            val targetBytes = if (useTargetSize) (targetSizeValue.toLongOrNull() ?: 0L) * selectedUnit.factor else null
            processedResult = processor.processImage(
                inputUri = uri,
                targetFormat = if (mode == AppMode.CONVERT || mode == AppMode.BOTH) targetFormat else ImageFormat.JPEG,
                quality = quality.toInt(),
                targetSizeBytes = if (targetBytes != null && targetBytes > 0) targetBytes else null
            )
            isProcessing = false
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Image Engine",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = when(mode) {
                                AppMode.COMPRESS_SINGLE -> "Compression: Single"
                                AppMode.COMPRESS_MULTI -> "Compression: Batch"
                                AppMode.CONVERT -> "Format Migration"
                                AppMode.BOTH -> "Full Process"
                            },
                            color = Color(0xFF00FF41),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 2.sp
                        )
                    }
                    
                    if (processedResult != null) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val saved = saveToGallery(context, processedResult!!)
                                    if (saved) {
                                        Toast.makeText(context, "Saved to Gallery", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFF00FF41), contentColor = Color.Black)
                        ) {
                            Icon(Icons.Default.Save, null)
                        }
                    }
                }
            }

            // Preview Section
            item(span = { GridItemSpan(2) }) {
                BentoCard(
                    modifier = Modifier.height(350.dp),
                    onClick = {
                        if (isMultiMode) {
                            multiPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        } else {
                            singlePickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    }
                ) {
                    if (selectedImageUri == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("Select Image", color = Color.Gray, fontWeight = FontWeight.Medium)
                        }
                    } else {
                        Box(contentAlignment = Alignment.Center) {
                            ImageComparisonSlider(
                                originalUri = selectedImageUri,
                                processedUri = processedResult?.uri ?: selectedImageUri
                            )
                            if (isProcessing) {
                                CircularProgressIndicator(color = Color(0xFF00FF41))
                            }
                        }
                    }
                }
            }

            // Mode Selection
            item(span = { GridItemSpan(2) }) {
                BentoCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Custom File Size Mode", color = Color.White)
                        Switch(
                            checked = useTargetSize,
                            onCheckedChange = { useTargetSize = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF00FF41))
                        )
                    }
                }
            }

            // Format Selection - Only visible in Convert or Both modes
            if (mode == AppMode.CONVERT || mode == AppMode.BOTH) {
                item {
                    BentoCard(title = "Format") {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ImageFormat.entries.forEach { format ->
                                FilterChip(
                                    selected = targetFormat == format,
                                    onClick = { targetFormat = format },
                                    label = { Text(format.name, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF00FF41),
                                        selectedLabelColor = Color.Black,
                                        containerColor = Color(0xFF1E1E1E),
                                        labelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }
                    }
                }
            }

            // Control Selection
            item(span = { if (mode == AppMode.CONVERT || mode == AppMode.BOTH) GridItemSpan(1) else GridItemSpan(2) }) {
                if (useTargetSize) {
                    BentoCard(title = "Target Size") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextField(
                                value = targetSizeValue,
                                onValueChange = { targetSizeValue = it },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color(0xFF00FF41)
                                ),
                                singleLine = true
                            )
                            
                            var unitExpanded by remember { mutableStateOf(false) }
                            Box {
                                TextButton(onClick = { unitExpanded = true }) {
                                    Text(selectedUnit.name, color = Color(0xFF00FF41))
                                    Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFF00FF41))
                                }
                                DropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false },
                                    modifier = Modifier.background(Color(0xFF1E1E1E))
                                ) {
                                    SizeUnit.entries.forEach { unit ->
                                        DropdownMenuItem(
                                            text = { Text(unit.name, color = Color.White) },
                                            onClick = {
                                                selectedUnit = unit
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    BentoCard(title = "Quality: ${quality.toInt()}%") {
                        Slider(
                            value = quality,
                            onValueChange = { quality = it },
                            valueRange = 10f..100f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color(0xFF00FF41)
                            )
                        )
                    }
                }
            }

            // Statistics Card
            item(span = { GridItemSpan(2) }) {
                BentoCard(title = "Compression Intelligence") {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatItem("Original", getFileSize(context, selectedImageUri))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                        StatItem("Optimized", formatSize(processedResult?.sizeBytes ?: 0))
                        
                        val saving = calculateSaving(context, selectedImageUri, processedResult?.sizeBytes)
                        StatItem("Saving", "$saving%", color = Color(0xFF00FF41))
                    }
                }
            }
        }
    }
}

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F0F0F)),
        onClick = onClick ?: {},
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (title != null) {
                Text(
                    title,
                    color = Color.Gray,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 12.dp),
                    letterSpacing = 1.sp
                )
            }
            content()
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Spacer(Modifier.height(4.dp))
        Text(value, color = color, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
    }
}

fun getFileSize(context: Context, uri: Uri?): String {
    if (uri == null) return "0 KB"
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val size = inputStream?.available()?.toLong() ?: 0L
        inputStream?.close()
        formatSize(size)
    } catch (ignored: Exception) {
        "0 KB"
    }
}

fun formatSize(size: Long): String {
    if (size <= 0) return "0 KB"
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.2f MB".format(mb) else "%.1f KB".format(kb)
}

fun calculateSaving(context: Context, original: Uri?, optimized: Long?): Int {
    if ((original == null) || (optimized == null) || (optimized == 0L)) return 0
    val originalSize = try {
        context.contentResolver.openInputStream(original)?.use { it.available() } ?: 0
    } catch (e: Exception) { 0 }
    if (originalSize == 0) return 0
    val saving = (originalSize - optimized).toDouble() / originalSize * 100
    return saving.toInt().coerceIn(0, 99)
}

suspend fun saveToGallery(context: Context, result: CompressionResult): Boolean = withContext(Dispatchers.IO) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "ConCom_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, result.format.mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ConCom")
    }
    
    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    uri?.let { targetUri ->
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            val file = File(result.uri.path!!)
            file.inputStream().use { it.copyTo(outputStream) }
        }
        true
    } ?: false
}
