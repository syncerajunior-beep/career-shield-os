package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val TAG = "SitePhotoStorage"
private const val PHOTOS_DIR = "site_photos"

object SitePhotoStorage {

    private fun getPhotosDir(context: Context): File {
        val dir = File(context.filesDir, PHOTOS_DIR)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun createSitePhotoUri(context: Context): Uri {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(getPhotosDir(context), "SITE_${timeStamp}_${System.nanoTime()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun persistPickedPhoto(context: Context, sourceUri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val destFile = File(getPhotosDir(context), "PICKED_${timeStamp}_${System.nanoTime()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                destFile
            ).toString()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist picked site photo", e)
            null
        }
    }

    suspend fun loadScaledBitmap(
        context: Context,
        uriString: String,
        maxDimension: Int = 1280
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            var sampleSize = 1
            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight
            if (rawWidth > 0 && rawHeight > 0) {
                while ((rawWidth / sampleSize) > maxDimension || (rawHeight / sampleSize) > maxDimension) {
                    sampleSize *= 2
                }
            }
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load scaled bitmap from $uriString", e)
            null
        }
    }

    fun deletePhotoFileIfLocal(context: Context, uriString: String?) {
        if (uriString.isNullOrBlank()) return
        try {
            val uri = Uri.parse(uriString)
            val fileName = uri.lastPathSegment ?: return
            val targetFile = File(getPhotosDir(context), File(fileName).name)
            if (targetFile.exists()) {
                targetFile.delete()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Unable to delete local site photo file", e)
        }
    }
}
