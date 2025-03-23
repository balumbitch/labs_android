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

    // Элементы интерфейса
    private lateinit var tvSongTitle: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var btnPlay: Button
    private lateinit var btnPreview: Button
    private lateinit var btnNext: Button
    private lateinit var btnLoop: Button
    private lateinit var btnVolumeUp: Button
    private lateinit var btnVolumeDown: Button

    // Медиаплеер и управление громкостью
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var audioManager: AudioManager

    // Список песен и их названий
    private val songs = listOf(R.raw.stardew, R.raw.festival)
    private val songTitles = listOf("Stardew", "Festival")
    private var currentSongIndex = 0

    // Для обновления SeekBar
    private lateinit var handler: Handler
    private lateinit var updateSeekBar: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)

        // Находим все элементы интерфейса
        findViews()

        // Инициализация AudioManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Инициализация MediaPlayer
        initializeMediaPlayer()

        // Настройка SeekBar
        setupSeekBar()

        // Настройка кнопок
        setupButtons()
    }

    private fun findViews() {
        tvSongTitle = findViewById(R.id.tvSongTitle)
        seekBar = findViewById(R.id.seekBar)
        btnPlay = findViewById(R.id.btnPlay)
        btnPreview = findViewById(R.id.btnPreview)
        btnNext = findViewById(R.id.btnNext)
        btnLoop = findViewById(R.id.btnLoop)
        btnVolumeUp = findViewById(R.id.btnVolumeUp)
        btnVolumeDown = findViewById(R.id.btnVolumeDown)
    }

    private fun initializeMediaPlayer() {
        mediaPlayer = MediaPlayer.create(this, songs[currentSongIndex])
        updateSongTitle()
    }

    private fun updateSongTitle() {
        tvSongTitle.text = "Now Playing: ${songTitles[currentSongIndex]}"
    }

    private fun setupSeekBar() {
        seekBar.max = mediaPlayer.duration
        handler = Handler()
        updateSeekBar = Runnable {
            seekBar.progress = mediaPlayer.currentPosition
            handler.postDelayed(updateSeekBar, 1000)
        }
        handler.post(updateSeekBar)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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
            mediaPlayer.isLooping = !mediaPlayer.isLooping
            btnLoop.text = if (mediaPlayer.isLooping) "Cycle: On" else "Cycle: Off"
        }

        btnVolumeUp.setOnClickListener {
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }

        btnVolumeDown.setOnClickListener {
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
        btnPlay.text = "Pause"
        updateSongTitle()
        seekBar.max = mediaPlayer.duration
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