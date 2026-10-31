package com.oldos.android.model

import android.content.ContentUris
import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class SongItem(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val uri: android.net.Uri
)

/**
 * App-wide iPod playback engine. The multitasking tray's media controls and
 * the iPod app share this single MediaPlayer-backed state holder.
 */
object MusicPlayerManager {

    var isPlaying: Boolean by mutableStateOf(false)
        private set
    var currentIndex: Int by mutableStateOf(-1)
        private set
    var positionMs: Long by mutableLongStateOf(0L)
    var queue: List<SongItem> by mutableStateOf(emptyList())
        private set

    val currentSong: SongItem?
        get() = queue.getOrNull(currentIndex)

    private var player: MediaPlayer? = null

    /** Starts (or restarts) playback with the given queue. */
    fun play(context: Context, songs: List<SongItem>, index: Int) {
        if (songs.isEmpty()) return
        queue = songs
        currentIndex = index.coerceIn(0, songs.lastIndex)
        startCurrent(context)
    }

    fun togglePlayPause(context: Context) {
        if (currentSong == null) return
        val current = player
        if (current == null) {
            startCurrent(context)
            return
        }
        if (isPlaying) {
            current.pause()
            isPlaying = false
        } else {
            current.start()
            isPlaying = true
        }
    }

    fun next(context: Context) {
        if (queue.isEmpty()) return
        currentIndex = (currentIndex + 1) % queue.size
        startCurrent(context)
    }

    fun previous(context: Context) {
        if (queue.isEmpty()) return
        currentIndex = if (currentIndex > 0) currentIndex - 1 else queue.lastIndex
        startCurrent(context)
    }

    fun seekTo(fraction: Float) {
        val current = player ?: return
        val target = (current.duration * fraction.coerceIn(0f, 1f)).toInt()
        current.seekTo(target)
        positionMs = target.toLong()
    }

    /** Polls the live playback head; called from a UI ticker. */
    fun updatePosition() {
        positionMs = player?.currentPosition?.toLong() ?: 0L
    }

    private fun startCurrent(context: Context) {
        val song = currentSong ?: return
        player?.release()
        isPlaying = false
        positionMs = 0L
        try {
            val newPlayer = MediaPlayer().apply {
                setDataSource(context.applicationContext, song.uri)
                prepare()
                setOnCompletionListener {
                    this@MusicPlayerManager.isPlaying = false
                    next(context)
                }
                start()
            }
            player = newPlayer
            isPlaying = true
        } catch (e: Exception) {
            android.util.Log.w("MusicPlayerManager", "playback failed for ${song.title}", e)
            player = null
            isPlaying = false
        }
    }

    fun release() {
        player?.release()
        player = null
        isPlaying = false
    }
}

/** Device music library via MediaStore. */
object AudioRepository {

    fun loadSongs(context: Context): List<SongItem> {
        val collection = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val songs = mutableListOf<SongItem>()
        try {
            context.contentResolver.query(
                collection,
                projection,
                "${MediaStore.Audio.Media.DURATION} > 10000",
                null,
                "${MediaStore.Audio.Media.TITLE} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    songs += SongItem(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown Track",
                        artist = cursor.getString(artistCol)?.takeIf { it != "<unknown>" }
                            ?: "Unknown Artist",
                        album = cursor.getString(albumCol)?.takeIf { it != "<unknown>" }
                            ?: "Unknown Album",
                        durationMs = cursor.getLong(durationCol),
                        uri = ContentUris.withAppendedId(collection, id)
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("AudioRepository", "loadSongs failed", e)
        }
        return songs
    }

    /** Embedded album art, read lazily for the Now Playing view. */
    fun albumArt(context: Context, song: SongItem): android.graphics.Bitmap? = try {
        val retriever = android.media.MediaMetadataRetriever()
        retriever.setDataSource(context.applicationContext, song.uri)
        val bytes = retriever.embeddedPicture
        retriever.release()
        bytes?.let { android.graphics.BitmapFactory.decodeByteArray(it, 0, it.size) }
    } catch (e: Exception) {
        null
    }

    fun formatDuration(ms: Long): String {
        val totalSec = ms / 1000
        return "%d:%02d".format(totalSec / 60, totalSec % 60)
    }
}
