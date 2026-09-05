package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageCompressor {

    suspend fun compressAndSaveImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080,
        quality: Int = 85
    ): File? = withContext(Dispatchers.IO) {
        try {
            var inputStream: InputStream? = context.contentResolver.openInputStream(uri) ?: return@withContext null

            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var sampleSize = 1
            while (options.outWidth / sampleSize > maxWidth || options.outHeight / sampleSize > maxHeight) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            inputStream = context.contentResolver.openInputStream(uri)
            val decoded = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (decoded == null) return@withContext null

            // Scale to target if still exceeds
            val scaled = if (decoded.width > maxWidth || decoded.height > maxHeight) {
                val ratio = (maxWidth.toFloat() / decoded.width).coerceAtMost(maxHeight.toFloat() / decoded.height)
                val targetW = (decoded.width * ratio).toInt()
                val targetH = (decoded.height * ratio).toInt()
                Bitmap.createScaledBitmap(decoded, targetW, targetH, true).also {
                    if (it != decoded) decoded.recycle()
                }
            } else {
                decoded
            }

            val storageDir = File(context.filesDir, "clinical_media").apply { mkdirs() }
            val outputFile = File(storageDir, "clinical_${UUID.randomUUID()}.jpg")

            FileOutputStream(outputFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            scaled.recycle()

            outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
