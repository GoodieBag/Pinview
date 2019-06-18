package com.goodiebag.pinview.example;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Pinview pinview1 = findViewById(R.id.pinview1);
        pinview1.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                Toast.makeText(MainActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();

                //test or check accepted rejected
                if(pinview1.getValue().equals("1234")){
                    pinview1.setAccepted();
                } else {
                    pinview1.setRejected();
                }
            }

            @Override
            public void onAccepted() {

            }

            @Override
            public void onRejected() {

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
