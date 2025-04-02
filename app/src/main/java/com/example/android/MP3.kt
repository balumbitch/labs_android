package com.example.android

import android.Manifest
import androidx.core.app.ActivityCompat
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File

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
    private var songs = mutableListOf<String>()
    private var songTitles = mutableListOf<String>()
    private var currentSongIndex = 0
    private lateinit var handler: Handler
    private lateinit var updateSeekBar: Runnable
    private val logTag = "MP3_PLAYER"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadMusic()
        } else {
            Toast.makeText(this, "Permission denied. Cannot play music.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mp3)
        findViews()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        checkPermission()
    }

    private fun checkPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadMusic()
            }
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                permission
            ) -> {
                Toast.makeText(this, "Permission needed to play music", Toast.LENGTH_SHORT).show()
                requestPermissionLauncher.launch(permission)
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadMusic() {
        loadMusicFromMediaStore()
        loadMusicFromMusicDirectory()

        if (songs.isEmpty()) {
            Toast.makeText(this, "No music found on device", Toast.LENGTH_SHORT).show()
        } else {
            initializeMediaPlayer()
        }
    }

    private fun loadMusicFromMediaStore() {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.TITLE
        )
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = MediaStore.Audio.Media.TITLE + " ASC"

        contentResolver.query(uri, projection, selection, null, sortOrder)?.use { cursor ->
            while (cursor.moveToNext()) {
                val path = cursor.getString(0)
                val title = cursor.getString(1)
                if (!songs.contains(path)) {
                    songs.add(path)
                    songTitles.add(title)
                }
            }
        }
    }

    private fun loadMusicFromMusicDirectory() {
        val musicPath = Environment.getExternalStorageDirectory().path + "/Music"
        Log.d(logTag, "Music directory path: $musicPath")

        val directory = File(musicPath)
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isFile && isAudioFile(file)) {
                    if (!songs.contains(file.absolutePath)) {
                        songs.add(file.absolutePath)
                        songTitles.add(file.nameWithoutExtension)
                        Log.d(logTag, "Found audio file: ${file.absolutePath}")
                    }
                }
            }
        }
    }

    private fun isAudioFile(file: File): Boolean {
        val name = file.name.lowercase()
        return name.endsWith(".mp3")
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
        if (songs.isNotEmpty()) {
            mediaPlayer = MediaPlayer()
            try {
                mediaPlayer.setDataSource(songs[currentSongIndex])
                mediaPlayer.prepare()
                updateSongTitle()
                setupSeekBar()
                setupButtons()
            } catch (e: Exception) {
                Toast.makeText(this, "Error playing music: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(logTag, "Error initializing media player", e)
            }
        }
    }

    private fun updateSongTitle() {
        songname.text = "Now Playing: ${songTitles[currentSongIndex]}"
    }
    private fun setupSeekBar() {
        seekbar.max = mediaPlayer.duration
        handler = Handler()
        updateSeekBar = Runnable {
            seekbar.progress = mediaPlayer.currentPosition
            handler.postDelayed(updateSeekBar, 0)
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
        try {
            mediaPlayer.reset()
            mediaPlayer.setDataSource(songs[currentSongIndex])
            mediaPlayer.prepare()
            mediaPlayer.start()
            play.text = "pause"
            updateSongTitle()
            seekbar.max = mediaPlayer.duration
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing song: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e(logTag, "Error in playSong", e)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            mediaPlayer.release()
        }
        if (::handler.isInitialized && ::updateSeekBar.isInitialized) {
            handler.removeCallbacks(updateSeekBar)
        }
    }
}