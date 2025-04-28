package com.example.android

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnGoToCalculator: Button = findViewById(R.id.btnGoToCalculator)
        btnGoToCalculator.setOnClickListener {
            val intent = Intent(this, Calculator::class.java)
            startActivity(intent)
        }

        val btnGoToMP3: Button = findViewById(R.id.btnGoToMP3)
        btnGoToMP3.setOnClickListener {
            val intent = Intent(this, MP3::class.java)
            startActivity(intent)
        }

        val btnGoToGPS: Button = findViewById(R.id.btnGoToGPS)
        btnGoToGPS.setOnClickListener {
            val intent = Intent(this, GPS::class.java)
            startActivity(intent)
        }

        val btnGoToCellularInfo: Button = findViewById(R.id.btnGoToCellularInfo)
        btnGoToCellularInfo.setOnClickListener {
            val intent = Intent(this, CellularInfo::class.java)
            startActivity(intent)
        }
    }
}