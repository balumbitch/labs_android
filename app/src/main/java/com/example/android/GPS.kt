package com.example.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GPS : AppCompatActivity(), LocationListener {

    private lateinit var locationManager: LocationManager
    private lateinit var tvLocation: TextView
    private lateinit var tvTime: TextView
    private val locationPermissionCode = 101
    private val gpsDataFile = "gps_data.json"
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
    private lateinit var handler: Handler
    private var currentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gps)

        tvLocation = findViewById(R.id.tvLocation)
        tvTime = findViewById(R.id.tvTime)
        findViewById<Button>(R.id.btnSaveLocation).setOnClickListener {
            saveCurrentLocation()
        }

        handler = Handler(Looper.getMainLooper())
        startTimeUpdates()

        checkLocationPermission()
    }

    private fun startTimeUpdates() {
        val timeUpdateRunnable = object : Runnable {
            override fun run() {
                updateTime()
                handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1))
            }
        }
        handler.post(timeUpdateRunnable)
    }

    private fun updateTime() {
        val currentTime = dateFormat.format(Date())
        tvTime.text = "Current time: $currentTime"
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1000,
                1f,
                this
            )

            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            lastLocation?.let { onLocationChanged(it) }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Access error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(location: Location) {
        currentLocation = location
        runOnUiThread {
            tvLocation.text = """
                Latitude: ${"%.6f".format(location.latitude)}
                Longitude: ${"%.6f".format(location.longitude)}
                Altitude: ${"%.1f".format(location.altitude)} м
            """.trimIndent()
        }
    }

    private fun saveCurrentLocation() {
        currentLocation?.let {
            saveLocationToJson(it)
            Toast.makeText(this, "Save", Toast.LENGTH_SHORT).show()
        } ?: Toast.makeText(this, "Unaviable", Toast.LENGTH_SHORT).show()
    }

    private fun saveLocationToJson(location: Location) {
        try {
            val jsonData = JSONObject().apply {
                put("latitude", location.latitude)
                put("longitude", location.longitude)
                put("altitude", location.altitude)
                put("time", dateFormat.format(Date(location.time)))
            }

            val file = File(getExternalFilesDir(null), gpsDataFile)
            file.writeText(jsonData.toString(4))
        } catch (e: Exception) {
            Toast.makeText(this, "Save error", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        if (::locationManager.isInitialized) {
            locationManager.removeUpdates(this)
        }
    }
}