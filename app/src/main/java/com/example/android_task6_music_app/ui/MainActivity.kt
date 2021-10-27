package com.example.android_task6_music_app.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.RequestManager
import com.example.android_task6_music_app.data.entities.Song
import com.example.android_task6_music_app.databinding.ActivityMainBinding
import com.example.android_task6_music_app.exoplayer.toSong
import com.example.android_task6_music_app.other.Status
import com.example.android_task6_music_app.ui.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var logs = "start MediaSession..."

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        binding.textLogs.text = logs

        binding.pauseButton.setOnClickListener {
            mainViewModel.pauseTo()
            logs = logs + "\ntrack " + curPlayingSong?.title + " is paused..."
            binding.textLogs.text = logs
        }

        binding.playButton.setOnClickListener {
            mainViewModel.playTo()
            logs = logs + "\ntrack " + curPlayingSong?.title + " is playing..."
            binding.textLogs.text = logs
        }

        binding.prevButton.setOnClickListener {
            mainViewModel.skipToPreviousSong()
        }

        binding.nextButton.setOnClickListener {
            mainViewModel.skipToNextSong()
        }
    }

    private fun updateTitleAndSongImage(song: Song) {
        val title = "${song.title} - ${song.artist}"
        binding.textTitle.text = title
        logs = logs + "\ntrack " + curPlayingSong?.title + " was chosen..."
        logs = logs + "\ntrack " + curPlayingSong?.title + " is playing..."
        binding.textLogs.text = logs
        glide.load(song.bitmapUri).into(binding.image)
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if(curPlayingSong == null && songs.isNotEmpty()) {
                                curPlayingSong = songs[0]
                                updateTitleAndSongImage(songs[0])
                            }
                        }
                    }
                    else -> {
                        Unit
                    }
                }
            }
        }
        mainViewModel.curPlayingSong.observe(this) {
            if (it == null) return@observe
            curPlayingSong = it.toSong()
            updateTitleAndSongImage(curPlayingSong!!)
        }
    }
}
