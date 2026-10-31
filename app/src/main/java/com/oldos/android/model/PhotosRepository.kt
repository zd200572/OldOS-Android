package com.oldos.android.model

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhotoItem(
    val id: Long,
    val uri: Uri,
    val dateTakenMs: Long
)

/**
 * Device photo library via MediaStore. Handles the SDK split: scoped media
 * permissions on 13+, visual-user-selected partial grants on 14+, and legacy
 * READ_EXTERNAL_STORAGE below 13.
 */
object PhotosRepository {

    fun loadPhotos(context: Context): List<PhotoItem> {
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATE_ADDED
        )

        val photos = mutableListOf<PhotoItem>()
        try {
            context.contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    photos += PhotoItem(
                        id = id,
                        uri = ContentUris.withAppendedId(collection, id),
                        dateTakenMs = cursor.getLong(dateCol) * 1000L
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("PhotosRepository", "loadPhotos failed", e)
        }
        return photos
    }

    fun thumbnail(context: Context, photo: PhotoItem, sizePx: Int): android.graphics.Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(photo.uri, android.util.Size(sizePx, sizePx), null)
        } else {
            MediaStore.Images.Thumbnails.getThumbnail(
                context.contentResolver, photo.id,
                MediaStore.Images.Thumbnails.MINI_KIND, null
            )
        }
    } catch (e: Exception) {
        null
    }

    fun formatDate(ms: Long): String =
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(Date(ms))
}
