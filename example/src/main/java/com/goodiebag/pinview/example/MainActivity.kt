package com.goodiebag.pinview.example

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.goodiebag.pinview.Pinview

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val scaledDensity = resources.displayMetrics.scaledDensity

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

        val pinview0 = pinViews[0]
        pinview0.setPinViewEventListener { pinview: Pinview, fromUser: Boolean ->
            Toast.makeText(this@MainActivity, pinview.value, Toast.LENGTH_SHORT).show()
        }
        setFont(pinview0)
        pinview0.setTextPadding(0, (4 * scaledDensity).toInt(), 0, 0)

        // pinView Customize
        pinViews[4].apply {
            setCursorShape(R.drawable.example_cursor)
            //setCursorColor(Color.BLUE);
            textSize = 12
            setTextColor(Color.BLACK)
            showCursor(true)
        }

        val widePinview = pinViews[6]
        val defaultTextSize = widePinview.textSize
        val minTextSizeScalePercentage = resources.getDimension(R.dimen.minimum_text_size) / resources.getDimension(R.dimen.desired_text_size)
        // Auto adjust text size for when the Pinview which is wider than screen
        widePinview.setPinViewWidthUpdateListener { pinView: Pinview, width: Int ->
            // Scale text, but avoid going smaller than a minimum size
            val percentDownScaledWidth = (width / pinView.pinWidth.toFloat()).coerceAtLeast(minTextSizeScalePercentage)
            pinView.textSize = (defaultTextSize * percentDownScaledWidth).toInt()
        }
    }

    @SuppressLint("NewApi")
    private fun setFont(pinview1: Pinview) {
        // resources.getFont only available from SDK 26
        val typeface = ResourcesCompat.getFont(this, R.font.poppins_semibold)
        pinview1.setTypeface(typeface)
    }

}