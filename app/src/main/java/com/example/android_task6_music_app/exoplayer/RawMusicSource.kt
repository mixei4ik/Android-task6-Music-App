package com.example.android_task6_music_app.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import android.util.Log
import androidx.core.net.toUri
import com.example.android_task6_music_app.data.remote.MusicDatabase
import com.example.android_task6_music_app.exoplayer.State.*
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val TAG = "Raw Music Source"

class RawMusicSource @Inject constructor(
    private val musicDatabase: MusicDatabase
) {

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.IO) {
        state = STATE_INITIALIZING
        val allSongs = musicDatabase.getAllSongs()
        Log.v(TAG, "allSongs " + allSongs.size)
        songs = allSongs.map { song ->
            MediaMetadataCompat.Builder()
                .putString(METADATA_KEY_ARTIST, song.artist)
                .putString(METADATA_KEY_MEDIA_ID, song.id)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.bitmapUri)
                .putString(METADATA_KEY_MEDIA_URI, song.trackUri)
                .putString(METADATA_KEY_ALBUM_ART_URI, song.bitmapUri)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artist)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.artist)
                .build()
        }
        Log.v(TAG, "songs " + songs.size)
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            Log.v(TAG, "start concatenatingMediaSource.addMediaSource(mediaSource) +1")
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            concatenatingMediaSource.addMediaSource(mediaSource)
            Log.v(TAG, "concatenatingMediaSource.addMediaSource(mediaSource) +1")
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = State.STATE_CREATED
        set(value) {
            if(value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        if(state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            return false
        } else {
            action(state == STATE_INITIALIZED)
            return true
        }
    }
}

enum class State{
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}