package com.example.myfirstapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity
{

    private ImageView play;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the android image
        this.play = (ImageView) findViewById(R.id.play);

        // Set an OnClick event on the android image
        play.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Declare which activity you want to be activated
                Intent otherActivity = new Intent(getApplicationContext(), CookieActivity.class);

                // Start this new activity
                startActivity(otherActivity);

                // End the current activity
                finish();
            }
        });
    }
}