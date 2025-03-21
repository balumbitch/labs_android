package com.example.android

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MP3 : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var tvSongTitle: TextView
    private lateinit var btnPlay: Button
    private lateinit var btnPreview: Button
    private lateinit var btnNext: Button
    private lateinit var btnLoop: Button

    private val songs = listOf(R.raw.song1, R.raw.song2, R.raw.song3) // Добавьте свои MP3-файлы в res/raw
    private var currentSongIndex = 0
    private var isLooping = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvSongTitle = findViewById(R.id.tvSongTitle)
        btnPlay = findViewById(R.id.btnPlay)
        btnPreview = findViewById(R.id.btnPreview)
        btnNext = findViewById(R.id.btnNext)
        btnLoop = findViewById(R.id.btnLoop)

        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex])
        updateSongTitle()

        btnPlay.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                btnPlay.text = "Pause"
            } else {
                mediaPlayer.pause()
                btnPlay.text = "Play"
            }
        }

        btnPreview.setOnClickListener {
            playPreviousSong()
        }

        btnNext.setOnClickListener {
            playNextSong()
        }

        btnLoop.setOnClickListener {
            isLooping = !isLooping
            mediaPlayer.isLooping = isLooping
            btnLoop.text = if (isLooping) "Loop: On" else "Loop: Off"
        }

        mediaPlayer.setOnCompletionListener {
            if (!isLooping) {
                playNextSong()
            } else {
                mediaPlayer.start()
            }
        }
    }

    private fun playNextSong() {
        currentSongIndex = (currentSongIndex + 1) % songs.size
        playSong()
    }

    private fun playPreviousSong() {
        currentSongIndex = (currentSongIndex - 1 + songs.size) % songs.size
        playSong()
    }

    private fun playSong() {
        mediaPlayer.reset()
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex])
        mediaPlayer.start()
        btnPlay.text = "Pause"
        updateSongTitle()
    }

    private fun updateSongTitle() {
        tvSongTitle.text = "Now Playing: Song ${currentSongIndex + 1}"
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}