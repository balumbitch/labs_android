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
    }
}