package com.example.concom.ui.screens

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.concom.ui.components.ImageComparisonSlider
import com.example.concom.util.CompressionResult
import com.example.concom.util.ImageFormat
import com.example.concom.util.ImageProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

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
    var isProcessing by remember { mutableStateOf(false) }

    val isMultiMode = mode == AppMode.COMPRESS_MULTI || mode == AppMode.CONVERT_MULTI || mode == AppMode.BOTH_MULTI
    val isConvertMode = mode == AppMode.CONVERT_SINGLE || mode == AppMode.CONVERT_MULTI
    val isBothMode = mode == AppMode.BOTH_SINGLE || mode == AppMode.BOTH_MULTI
    val themeColor = if (isBothMode) Color(0xFF00E5FF) else if (isConvertMode) Color(0xFFD0BCFF) else Color(0xFF00FF41)

    val originalFormatName = remember(selectedImageUri) {
        selectedImageUri?.let { getImageFormatFromUri(context, it) } ?: ""
    }

    LaunchedEffect(originalFormatName) {
        if (isConvertMode || isBothMode) {
            val originalEnum = ImageFormat.entries.find { 
                it.name == originalFormatName || it.extension.uppercase(Locale.ROOT) == originalFormatName 
            }
            if (targetFormat == originalEnum) {
                targetFormat = if (originalEnum == ImageFormat.WEBP) ImageFormat.JPEG else ImageFormat.WEBP
            }
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = if (isMultiMode) ActivityResultContracts.PickMultipleVisualMedia() else ActivityResultContracts.PickVisualMedia()
    ) { result ->
        when (result) {
            is Uri -> {
                selectedImageUri = result
                processedResult = null
            }
            is List<*> -> {
                val uris = result.filterIsInstance<Uri>()
                if (uris.isNotEmpty()) {
                    selectedImagesUris = uris
                    selectedImageUri = uris.first()
                    processedResult = null
                }
            }
        }
    }

    LaunchedEffect(selectedImageUri, targetFormat, quality, useTargetSize, targetSizeValue, selectedUnit) {
        selectedImageUri?.let { uri ->
            isProcessing = true
            val targetBytes = if (useTargetSize) (targetSizeValue.toLongOrNull() ?: 0L) * selectedUnit.factor else null
            processedResult = processor.processImage(
                inputUri = uri,
                targetFormat = if (isConvertMode || isBothMode) targetFormat else ImageFormat.JPEG,
                quality = if (isConvertMode) 100 else quality.toInt(),
                targetSizeBytes = if (targetBytes != null && targetBytes > 0) targetBytes else null
            )
            isProcessing = false
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFF050505)) {
        Column(modifier = Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())) {
            StudioToolbar(mode, themeColor, isProcessing) {
                processedResult?.let { result ->
                    scope.launch {
                        if (saveToGallery(context, result)) {
                            Toast.makeText(context, "Saved to Studio Folder", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(if (selectedImageUri == null) 280.dp else 420.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFF111111))
                    .clickable { 
                        pickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) 
                    },
                contentAlignment = Alignment.Center
            ) {
                if (selectedImageUri == null) {
                    EmptyStagePlaceholder(themeColor, isMultiMode)
                } else {
                    StagePreview(isConvertMode, selectedImageUri!!, processedResult?.uri, isProcessing, themeColor)
                }
            }

            if (isMultiMode && selectedImagesUris.isNotEmpty()) {
                AssetStrip(selectedImagesUris, selectedImageUri, themeColor) { selectedImageUri = it }
            }

            if (selectedImageUri != null) {
                DashboardControls(
                    isConvertMode, isBothMode, originalFormatName, targetFormat,
                    useTargetSize, targetSizeValue, selectedUnit, quality, themeColor,
                    onFormatSelect = { targetFormat = it },
                    onModeToggle = { useTargetSize = it },
                    onSizeChange = { targetSizeValue = it },
                    onUnitChange = { selectedUnit = it },
                    onQualityChange = { quality = it }
                )
                
                IntelligenceStrip(context, selectedImageUri, processedResult, themeColor, isConvertMode)
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DashboardControls(
    isConvertMode: Boolean,
    isBothMode: Boolean,
    originalFormat: String,
    targetFormat: ImageFormat,
    useTargetSize: Boolean,
    targetSizeValue: String,
    selectedUnit: SizeUnit,
    quality: Float,
    themeColor: Color,
    onFormatSelect: (ImageFormat) -> Unit,
    onModeToggle: (Boolean) -> Unit,
    onSizeChange: (String) -> Unit,
    onUnitChange: (SizeUnit) -> Unit,
    onQualityChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        if (!isConvertMode) {
            StudioCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Precise Size Mode", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Hit an exact file size target", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = useTargetSize, onCheckedChange = onModeToggle, colors = SwitchDefaults.colors(checkedThumbColor = themeColor))
                }
            }
        }

        if (isConvertMode || isBothMode) {
            FormatControlCard(originalFormat, targetFormat, themeColor, onFormatSelect)
        }

        if (!isConvertMode) {
            OptimizationControlCard(useTargetSize, targetSizeValue, onSizeChange, selectedUnit, onUnitChange, quality, onQualityChange, themeColor)
        }
    }
}

@Composable
fun StudioToolbar(mode: AppMode, themeColor: Color, isProcessing: Boolean, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Studio View", color = Color.White, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black))
            Text(
                text = when(mode) {
                    AppMode.COMPRESS_SINGLE -> "Precision Compression"
                    AppMode.COMPRESS_MULTI -> "Batch Compression"
                    AppMode.CONVERT_SINGLE -> "Format migration"
                    AppMode.CONVERT_MULTI -> "Batch Migration"
                    AppMode.BOTH_SINGLE -> "Full Optimization"
                    AppMode.BOTH_MULTI -> "Full Batch Optimization"
                },
                color = themeColor,
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp)
            )
        }
        
        IconButton(
            onClick = onSave,
            enabled = !isProcessing,
            colors = IconButtonDefaults.iconButtonColors(containerColor = themeColor, contentColor = Color.Black),
            modifier = Modifier.size(52.dp)
        ) {
            Icon(Icons.Default.Download, null)
        }
    }
}

@Composable
fun StagePreview(isConvertMode: Boolean, selectedUri: Uri, processedUri: Uri?, isProcessing: Boolean, themeColor: Color) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isConvertMode) {
            AsyncImage(
                model = processedUri ?: selectedUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            ImageComparisonSlider(originalUri = selectedUri, processedUri = processedUri ?: selectedUri)
        }
        
        if (isProcessing) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = themeColor, strokeWidth = 3.dp)
            }
        }
    }
}

@Composable
fun EmptyStagePlaceholder(themeColor: Color, isMultiMode: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(80.dp).background(themeColor.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
            Icon(if (isMultiMode) Icons.Default.LibraryAdd else Icons.Default.AddAPhoto, null, tint = themeColor, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("Import Media", color = Color.White, fontWeight = FontWeight.Bold)
        Text("Select files to begin optimization", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun AssetStrip(uris: List<Uri>, selectedUri: Uri?, themeColor: Color, onSelect: (Uri) -> Unit) {
    LazyRow(modifier = Modifier.padding(bottom = 20.dp), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(uris) { uri ->
            val active = uri == selectedUri
            Box(
                modifier = Modifier
                    .size(70.dp).clip(RoundedCornerShape(16.dp))
                    .border(2.dp, if (active) themeColor else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onSelect(uri) }
            ) {
                AsyncImage(model = uri, null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if (active) Box(Modifier.fillMaxSize().background(themeColor.copy(alpha = 0.2f)))
            }
        }
    }
}

@Composable
fun FormatControlCard(originalFormat: String, targetFormat: ImageFormat, themeColor: Color, onFormatSelect: (ImageFormat) -> Unit) {
    StudioCard(title = "Step 1: Format Migration") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Original", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                Text(originalFormat, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.DarkGray, modifier = Modifier.padding(horizontal = 16.dp))
            Column(modifier = Modifier.weight(2f)) {
                Text("Target", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ImageFormat.entries.filter { it.name != originalFormat }.forEach { format ->
                        FilterChip(
                            selected = targetFormat == format,
                            onClick = { onFormatSelect(format) },
                            label = { Text(format.name, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = themeColor, selectedLabelColor = Color.Black, containerColor = Color(0xFF1A1A1A), labelColor = Color.White),
                            border = null,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationControlCard(useTargetSize: Boolean, targetSizeValue: String, onSizeChange: (String) -> Unit, selectedUnit: SizeUnit, onUnitChange: (SizeUnit) -> Unit, quality: Float, onQualityChange: (Float) -> Unit, themeColor: Color) {
    StudioCard(title = "Step 2: Size Engine") {
        if (useTargetSize) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = targetSizeValue, onValueChange = onSizeChange, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent, focusedIndicatorColor = themeColor),
                    singleLine = true, textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) { Text(selectedUnit.name, color = themeColor, fontWeight = FontWeight.Bold) }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        SizeUnit.entries.forEach { DropdownMenuItem(text = { Text(it.name) }, onClick = { onUnitChange(it); expanded = false }) }
                    }
                }
            }
        } else {
            Column {
                Slider(value = quality, onValueChange = onQualityChange, valueRange = 10f..100f, colors = SliderDefaults.colors(activeTrackColor = themeColor, thumbColor = Color.White))
                Text("${quality.toInt()}% Quality", color = themeColor, modifier = Modifier.align(Alignment.End), style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun IntelligenceStrip(context: Context, selectedUri: Uri?, processedResult: CompressionResult?, themeColor: Color, isConvertMode: Boolean) {
    Box(modifier = Modifier.padding(top = 20.dp, start = 20.dp, end = 20.dp)) {
        StudioCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                IntelligenceItem("Source", getFileSize(context, selectedUri))
                Icon(Icons.Default.Memory, null, tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                IntelligenceItem("Result", formatSize(processedResult?.sizeBytes ?: 0), color = themeColor)
                if (!isConvertMode) {
                    val saving = calculateSaving(context, selectedUri, processedResult?.sizeBytes)
                    IntelligenceItem("Efficiency", "$saving%", color = themeColor)
                }
            }
        }
    }
}

@Composable
fun StudioCard(title: String? = null, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF111111))) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (title != null) {
                Text(title.uppercase(), color = Color.White.copy(alpha = 0.3f), style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.5.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
            }
            content()
        }
    }
}

@Composable
fun IntelligenceItem(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, style = MaterialTheme.typography.labelSmall)
        Text(value, color = color, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black))
    }
}

fun getFileSize(context: Context, uri: Uri?): String {
    if (uri == null) return "0 KB"
    return try {
        context.contentResolver.openInputStream(uri)?.use { formatSize(it.available().toLong()) } ?: "0 KB"
    } catch (e: Exception) { "0 KB" }
}

fun formatSize(size: Long): String {
    if (size <= 0) return "0 KB"
    val kb = size / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1) "%.2f MB".format(mb) else "%.1f KB".format(kb)
}

fun calculateSaving(context: Context, original: Uri?, optimized: Long?): Int {
    if (original == null || optimized == null || optimized == 0L) return 0
    val originalSize = try { context.contentResolver.openInputStream(original)?.use { it.available() } ?: 0 } catch (e: Exception) { 0 }
    if (originalSize == 0) return 0
    return ((originalSize - optimized).toDouble() / originalSize * 100).toInt().coerceIn(0, 99)
}

suspend fun saveToGallery(context: Context, result: CompressionResult): Boolean = withContext(Dispatchers.IO) {
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, "ConCom_${System.currentTimeMillis()}")
        put(MediaStore.MediaColumns.MIME_TYPE, result.format.mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ConCom")
    }
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let { targetUri ->
        context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
            File(result.uri.path!!).inputStream().use { it.copyTo(outputStream) }
        }
        true
    } ?: false
}

fun getImageFormatFromUri(context: Context, uri: Uri): String {
    val mimeType = context.contentResolver.getType(uri)
    return if (mimeType != null) {
        MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.uppercase(Locale.ROOT) ?: "UNKNOWN"
    } else {
        uri.path?.let { path ->
            val lastDot = path.lastIndexOf('.')
            if (lastDot != -1) path.substring(lastDot + 1).uppercase(Locale.ROOT) else "UNKNOWN"
        } ?: "UNKNOWN"
    }
}
