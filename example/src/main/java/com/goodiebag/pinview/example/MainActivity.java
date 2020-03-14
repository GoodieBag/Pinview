package com.goodiebag.pinview.example;

import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Pinview pinview1 = findViewById(R.id.pinview1);
        pinview1.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                Toast.makeText(MainActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();
            }
        });

        // pinView Customize
        Pinview pinview5 = findViewById(R.id.pinview5);
        pinview5.setCursorShape(R.drawable.example_cursor);
//        pinview5.setCursorColor(Color.BLUE);
        pinview5.setTextSize(12);
        pinview5.setTextColor(Color.BLACK);
        pinview5.showCursor(true);
    }
}
