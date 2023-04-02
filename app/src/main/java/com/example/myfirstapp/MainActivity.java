package com.example.myfirstapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 0;
    private Button play;
    private LinearLayout myDynamicLayout;
    private ImageView imageChopper;
    private MainActivity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Step 1. get bluetooth adapter
        BluetoothManager bluetoothManager = getSystemService(BluetoothManager.class);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();                     // Required for every bluetooth activities
        if (bluetoothAdapter == null)
        {
            Toast.makeText(this, R.string.ble_adapter_null, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Step 2. Enable bluetooth
        if (!bluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
            {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
            else
            {
                finish();
            }
        }

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
                // SIMPLE POP-UP
                /*AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);
                chopperPopup.setTitle("Chopper");
                chopperPopup.setMessage("Ajouter un Chopper ?");
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
                chopperPopup.setNegativeButton("Non", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        Toast.makeText(getApplicationContext(), "Vous rendez Chopper triste...", Toast.LENGTH_SHORT).show();
                    }
                });

                chopperPopup.show();*/

                // PERSONALIZED POP-UP
                CustomPopup customPopup = new CustomPopup(activity);
                customPopup.setTitle("Bonne année Chopper");
                customPopup.setSubTitle("Merci Merry pour le boulot !");
                customPopup.getYesButton().setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(getApplicationContext(), "Ajout d'un Chopper", Toast.LENGTH_SHORT).show();

                        Button button = new Button(activity);
                        button.setText("Texte Bouton");
                        myDynamicLayout.addView(button);

                        customPopup.dismiss();
                    }
                });
                customPopup.getNoButton().setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Toast.makeText(getApplicationContext(), "Vous rendez Chopper triste...", Toast.LENGTH_SHORT).show();

                        customPopup.dismiss();
                    }
                });
                customPopup.build();
            }
        });

    }
}