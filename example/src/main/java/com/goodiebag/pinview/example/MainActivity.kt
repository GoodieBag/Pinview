package com.goodiebag.pinview.example

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goodiebag.pinview.Pinview
import com.goodiebag.pinview.Pinview.PinViewEventListener

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pinview1 = findViewById<Pinview>(R.id.pinview1)
        pinview1.setPinViewEventListener(object : PinViewEventListener {
            override fun onDataEntered(pinview: Pinview?, fromUser: Boolean) {
                Toast.makeText(this@MainActivity, pinview!!.value, Toast.LENGTH_SHORT).show()
            }
        })
        setFont(pinview1)

        // pinView Customize
        val pinview5 = findViewById<Pinview>(R.id.pinview5)
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