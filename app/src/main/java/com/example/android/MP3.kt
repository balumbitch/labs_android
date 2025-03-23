package com.example.android

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MP3 : AppCompatActivity() {

    private lateinit var songname: TextView
    private lateinit var seekbar: SeekBar
    private lateinit var play: Button
    private lateinit var back: Button
    private lateinit var next: Button
    private lateinit var cycle: Button
    private lateinit var volumeup: Button
    private lateinit var volumedown: Button
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager
    private val songs = listOf(R.raw.stardew, R.raw.festival)
    private val songTitles = listOf("Stardew", "Festival")
    private var currentSongIndex = 0
    private lateinit var handler: Handler
    private lateinit var updateSeekBar: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)
        findViews()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        initializeMediaPlayer()
        setupSeekBar()
        setupButtons()
    }

    private fun findViews() {
        songname = findViewById(R.id.songname)
        seekbar = findViewById(R.id.seekbar)
        play = findViewById(R.id.play)
        back = findViewById(R.id.back)
        next = findViewById(R.id.next)
        cycle = findViewById(R.id.cycle)
        volumeup = findViewById(R.id.volumeup)
        volumedown = findViewById(R.id.volumedown)
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex])
        updateSongTitle()
    }

    private fun updateSongTitle() {
        songname.text = "Now Playing: ${songTitles[currentSongIndex]}"
    }

    private fun setupSeekBar() {
        seekbar.max = mediaPlayer.duration
        handler = Handler()
        updateSeekBar = Runnable {
            seekbar.progress = mediaPlayer.currentPosition
            handler.postDelayed(updateSeekBar, 1000)
        }
        handler.post(updateSeekBar)

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupButtons() {
        play.setOnClickListener {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
                play.text = "pause"
            } else {
                mediaPlayer.pause()
                play.text = "play"
            }
        }

        back.setOnClickListener {
            playPreviousSong()
        }

        next.setOnClickListener {
            playNextSong()
        }

        cycle.setOnClickListener {
            mediaPlayer.isLooping = !mediaPlayer.isLooping
            cycle.text = if (mediaPlayer.isLooping) "cycle: on" else "cycle: off"
        }

        volumeup.setOnClickListener {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }

        volumedown.setOnClickListener {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
        }

        mediaPlayer.setOnCompletionListener {
            if (!mediaPlayer.isLooping) {
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
        play.text = "pause"
        updateSongTitle()
        seekbar.max = mediaPlayer.duration
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
        handler.removeCallbacks(updateSeekBar)
    }
}