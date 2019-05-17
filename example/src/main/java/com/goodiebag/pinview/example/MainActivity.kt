package com.goodiebag.pinview.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.goodiebag.pinview.Pinview
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pinview1.setPinViewEventListener(object : Pinview.PinViewEventListener {
            override fun onDataEntered(pinview: Pinview, fromUser: Boolean) {
                Toast.makeText(this@MainActivity, pinview.value, Toast.LENGTH_SHORT).show()
            }
        })
    }
}
