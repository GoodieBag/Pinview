package com.goodiebag.pinview.example

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodiebag.pinview.Pinview

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val pinViews = listOf<Pinview>(
            findViewById(R.id.pinview0),
            findViewById(R.id.pinview1),
            findViewById(R.id.pinview2),
            findViewById(R.id.pinview3),
            findViewById(R.id.pinview4),
            findViewById(R.id.pinview5),
            findViewById(R.id.pinview6),
            findViewById(R.id.pinview7),
        )
        findViewById<Button>(R.id.clearButton).setOnClickListener {
            pinViews.forEach { it.value = "" }
            pinViews[0].requestPinEntryFocus() // Reopen keyboard on first pin
        }

        val pinview1 = pinViews[0]
        pinview1.setPinViewEventListener { pinview: Pinview, fromUser: Boolean ->
            Toast.makeText(this@MainActivity, pinview.value, Toast.LENGTH_SHORT).show()
        }
        setFont(pinview1)
        val fontScaledDensity = resources.displayMetrics.scaledDensity
        pinview1.setTextPadding(0, (4 * fontScaledDensity).toInt(), 0, 0)

        // pinView Customize
        val pinview5 = pinViews[4]
        pinview5.apply {
            setCursorShape(R.drawable.example_cursor)
            //setCursorColor(Color.BLUE);
            setTextSize(12)
            setTextColor(Color.BLACK)
            showCursor(true)
        }
    }

    @SuppressLint("NewApi")
    private fun setFont(pinview1: Pinview) {
        val typeface = resources.getFont(R.font.poppins_semibold)
        pinview1.setTypeface(typeface)
    }

}