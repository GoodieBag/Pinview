package com.goodiebag.example.pinview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.goodiebag.pinview.Pinview;

public class MainActivity extends AppCompatActivity {
    private Pinview pinview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pinview = (Pinview) findViewById(R.id.pinview);
        pinview.setPinHeight(35);
        pinview.setPinLength(2);
        pinview.setPinWidth(35);
        pinview.invalidate();
        Log.d("pinLength", pinview.getPinLength()+"");
        //pinview.invalidate();
    }
}
