package com.example.myfirstapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private Button play;
    private LinearLayout myDynamicLayout;
    private ImageView imageChopper;
    private MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Set the main activity to be this activity
        activity = this;

        // Get the android image
        this.play = (Button) findViewById(R.id.play);

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

        // Dynamic Layout
        this.myDynamicLayout = (LinearLayout) findViewById(R.id.myDynamicLayout);
        this.imageChopper = (ImageView) findViewById(R.id.imageChopper);

        imageChopper.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Chopper clicked ask for a new chopper
                AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);
                chopperPopup.setTitle("Chopper");
                chopperPopup.setMessage("Ajouter un Chopper ?");
                chopperPopup.setNegativeButton("Non", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(getApplicationContext(), "Vous rendez Chopper triste...", Toast.LENGTH_SHORT).show();
                    }
                });
                chopperPopup.setPositiveButton("Oui", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(getApplicationContext(), "Ajout d'un Chopper", Toast.LENGTH_SHORT).show();

                        Button button = new Button(activity);
                        button.setText("Texte Bouton");
                        myDynamicLayout.addView(button);
                    }
                });

                chopperPopup.show();
            }
        });

    }
}