/**
 * File:         ShareHelper.kt
 * Owner:        Serena
 * Phase:        2
 *
 * Purpose:
 *   Shares a drawing with other apps and imports images from the
 *   device's gallery.
 *
 * Used by:
 *   - DrawingScreen
 *
 * Notes:
 *   - Sharing uses FileProvider
 *     The image is written to cacheDir/shared/ and shared from there.
 *   - Importing uses the photo picker
 */
package com.example.drawingapp.share

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.core.content.FileProvider
import java.io.File

object ShareHelper {

    // Must match the path in res/xml/file_paths.xml.
    private const val SHARE_FOLDER = "shared"

    // Opens Android's share sheet with bitmap as a PNG.
    // title is shown at the top of the share sheet.

    fun shareImage(context: Context, bitmap: Bitmap, title: String = "Share drawing") {
        val file = writeShareFile(context, bitmap)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            clipData = ClipData.newRawUri(null, uri) // shows a preview in the share sheet
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, title).apply {
            if (context !is Activity) addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    // Saves the bitmap to cacheDir/shared/, removing older shared files first.
    private fun writeShareFile(context: Context, bitmap: Bitmap): File {
        val folder = File(context.cacheDir, SHARE_FOLDER).apply { mkdirs() }
        folder.listFiles()?.forEach { it.delete() } // only keep the latest share

        // Unique name so receiving apps don't show a cached older image
        val file = File(folder, "drawing_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }


    // Loads a picked gallery image as a Bitmap, scaled down
    // Returns null if the image can't be read.
    fun loadBitmap(context: Context, uri: Uri, maxSize: Int = 2048): Bitmap? {
        val resolver = context.contentResolver

        // read only the image size
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        // Pick a power-of-two scale that fits within maxSize
        var sampleSize = 1
        while (bounds.outWidth / sampleSize > maxSize || bounds.outHeight / sampleSize > maxSize) {
            sampleSize *= 2
        }

        // decode the scaled image
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) }
    }
}


 // Remembers a gallery image picker. Call the returned function to open it
@Composable
fun rememberImagePicker(onPicked: (Uri) -> Unit): () -> Unit {
    val currentOnPicked by rememberUpdatedState(onPicked)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) currentOnPicked(uri)
    }

    return remember(launcher) {
        {
            launcher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }
    }
}