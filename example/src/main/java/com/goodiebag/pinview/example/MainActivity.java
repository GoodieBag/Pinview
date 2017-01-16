package com.goodiebag.pinview.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.goodiebag.pinview.Pinview;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {
    private Pinview pinview1, pinview2, pinview3, pinview4;
    private Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.activity_main);
        pinview1 = (Pinview) findViewById(R.id.pinview1);
        pinview2 = (Pinview) findViewById(R.id.pinview2);
        pinview3 = (Pinview) findViewById(R.id.pinview3);
        pinview4 = (Pinview) findViewById(R.id.pinview4);
        b = (Button) findViewById(R.id.btn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                 int random = ThreadLocalRandom.current().nextInt(50, 60);
//                random*=getResources().getDisplayMetrics().density;
//                Log.d("random", "" + random);
//                pinview1.setPinWidth(random);
//                pinview2.setPinWidth(random);
//                pinview3.setPinWidth(random);
//                pinview4.setPinWidth(random);

                pinview1.setPinBackgroundRes(R.drawable.sample_background);
                pinview1.setInputType(Pinview.InputType.TEXT);
                pinview2.setPassword(!pinview2.isPassword());
                pinview3.setPassword(!pinview3.isPassword());
                pinview4.setPassword(!pinview4.isPassword());
            }
        });
    }
}
