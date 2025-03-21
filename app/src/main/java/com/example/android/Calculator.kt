package com.example.android

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import net.objecthunter.exp4j.ExpressionBuilder

class Calculator : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private var input: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        tvResult = findViewById(R.id.tvResult)

        val buttons = listOf(
            R.id.btn0 to "0",
            R.id.btn1 to "1",
            R.id.btn2 to "2",
            R.id.btn3 to "3",
            R.id.btn4 to "4",
            R.id.btn5 to "5",
            R.id.btn6 to "6",
            R.id.btn7 to "7",
            R.id.btn8 to "8",
            R.id.btn9 to "9",
            R.id.btnPlus to "+",
            R.id.btnMinus to "-",
            R.id.btnMultiply to "*",
            R.id.btnDivide to "/"
        )

        buttons.forEach { (id, value) ->
            findViewById<Button>(id).setOnClickListener {
                input += value
                tvResult.text = input
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            input = ""
            tvResult.text = "0"
        }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            try {
                val result = ExpressionBuilder(input).build().evaluate()
                tvResult.text = result.toString()
                input = result.toString()
            } catch (e: Exception) {
                tvResult.text = "Ошибка"
                input = ""
            }
        }
    }
}
