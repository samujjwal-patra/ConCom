package com.example.concom.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

enum class ImageFormat(val extension: String, val mimeType: String) {
    JPEG("jpg", "image/jpeg"),
    PNG("png", "image/png"),
    WEBP("webp", "image/webp")
}

data class CompressionResult(
    val uri: Uri,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val qualityUsed: Int
)

class ImageProcessor(private val context: Context) {

    suspend fun processImage(
        inputUri: Uri,
        targetFormat: ImageFormat,
        quality: Int = 80,
        targetSizeBytes: Long? = null
    ): CompressionResult = withContext(Dispatchers.IO) {
        val inputStream = context.contentResolver.openInputStream(inputUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val compressFormat = getCompressFormat(targetFormat)
        var finalQuality = quality
        var finalBytes: ByteArray

        if (targetSizeBytes != null && targetSizeBytes > 0 && targetFormat != ImageFormat.PNG) {
            val result = binarySearchCompression(bitmap, compressFormat, targetSizeBytes)
            finalQuality = result.first
            finalBytes = result.second
        } else {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(compressFormat, quality, outputStream)
            finalBytes = outputStream.toByteArray()
        }

        val outputFile = saveBytesToCache(finalBytes, targetFormat)
        
        CompressionResult(
            uri = Uri.fromFile(outputFile),
            sizeBytes = outputFile.length(),
            width = bitmap.width,
            height = bitmap.height,
            format = targetFormat,
            qualityUsed = finalQuality
        )
    }

    private fun binarySearchCompression(
        bitmap: Bitmap, 
        format: Bitmap.CompressFormat, 
        targetSize: Long
    ): Pair<Int, ByteArray> {
        var low = 1
        var high = 100
        var bestQuality = 1
        var bestBytes = ByteArray(0)
        
        while (low <= high) {
            val mid = (low + high) / 2
            val stream = ByteArrayOutputStream()
            bitmap.compress(format, mid, stream)
            val bytes = stream.toByteArray()
            
            if (bytes.size <= targetSize) {
                bestQuality = mid
                bestBytes = bytes
                low = mid + 1
            } else {
                high = mid - 1
            }
        }
        
        if (bestBytes.isEmpty()) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(format, 1, stream)
            bestBytes = stream.toByteArray()
        }
        
        return Pair(bestQuality, bestBytes)
    }

    private fun saveBytesToCache(bytes: ByteArray, format: ImageFormat): File {
        val fileName = "concom_${System.currentTimeMillis()}.${format.extension}"
        val outputFile = File(context.cacheDir, fileName)
        FileOutputStream(outputFile).use { it.write(bytes) }
        return outputFile
    }

    private fun getCompressFormat(format: ImageFormat): Bitmap.CompressFormat {
        return when (format) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
        }
    }
}
