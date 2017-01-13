package com.goodiebag.pinview.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.goodiebag.pinview.Pinview;

public class MainActivity extends AppCompatActivity {
    private Pinview pinview1, pinview2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main);
        Pinview pin = new Pinview(this);
        pin.setPinHeight(40);
        pin.setPinWidth(40);
        pin.setInputType(Pinview.InputType.NUMBER);
        pin.setPinBackgroundRes(R.drawable.sample_background);
        pin.setValue("1234");
        String pinValue = pin.getValue();
        linearLayout.addView(pin);
        Log.d("value", pinValue);

    }
}
