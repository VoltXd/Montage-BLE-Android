package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class CookieActivity extends AppCompatActivity {

    private ImageView chopper;
    private TextView pat;
    private int numberOfPats = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cookie);

        chopper = (ImageView) findViewById(R.id.chopper);
        pat = (TextView) findViewById(R.id.pat);

        chopper.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                numberOfPats++;
                pat.setText("Caresses : " + numberOfPats);
            }
        });
    }
}