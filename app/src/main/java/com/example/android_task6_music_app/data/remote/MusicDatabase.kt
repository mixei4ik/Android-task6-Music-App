package com.example.android_task6_music_app.data.remote

import android.content.Context
import android.util.Log
import com.example.android_task6_music_app.R
import com.example.android_task6_music_app.data.entities.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

private const val TAG = "Music Database"

class MusicDatabase(context: Context) {

    private val jsonFileString = getJsonDataFromAsset(context)

    fun getAllSongs(): List<Song> {
        val gson = Gson()
        val listSongType = object : TypeToken<List<Song>>() {}.type

        val songs: List<Song> = gson.fromJson(jsonFileString, listSongType)
        Log.v(TAG, "songs size " + songs.size.toString())
        return songs
    }

    fun getJsonDataFromAsset(context: Context): String? {
        val jsonString: String
        try {
            jsonString = context.resources.openRawResource(R.raw.playlist).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}