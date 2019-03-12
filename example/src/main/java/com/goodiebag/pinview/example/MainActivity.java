package com.goodiebag.pinview.example;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.goodiebag.pinview.Pinview;

public class MainActivity extends AppCompatActivity {
    private Pinview pinview1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pinview1 = (Pinview) findViewById(R.id.pinview1);
        Button btnClear = (Button) findViewById(R.id.btnClear);
        pinview1.setPinViewEventListener(new Pinview.PinViewEventListener() {
            @Override
            public void onDataEntered(Pinview pinview, boolean fromUser) {
                Toast.makeText(MainActivity.this, pinview.getValue(), Toast.LENGTH_SHORT).show();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pinview1.clearEditText();
            }
        });
    }


}
