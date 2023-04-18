package com.example.myfirstapp;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private Button play;
    private Button scan;
    private LinearLayout myDynamicLayout;
    private ImageView imageChopper;
    private MainActivity activity;

    // Step 1. Variables
    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;                     // Required for every bluetooth activities

    private boolean isBluetoothDiscoveryStarted = false;

    // Step 3. Define BLE devices scanning functions
    private boolean isScanningAllowed = false;
    private boolean isConnexionAllowed = false;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private Handler handler = new Handler();
    /*private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();*/

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            /*leDeviceListAdapter.addDevice(result.getDevice());
            leDeviceListAdapter.notifyDataSetChanged();*/

            Toast.makeText(getApplicationContext(), "Chopper a trouvé un appareil !", Toast.LENGTH_SHORT).show();
            System.out.println(result);
        }
    };

    // Scan stops after 10 seconds
    private final long SCAN_PERIOD = 10000;

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver receiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Discorvery has found a device. Get the bluetooth device
                // object and its indo from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                System.out.println("\nDiscovered device.\nName: " + device.getName() + "\nMAC: " + device.getAddress());

                if (device.getName() == null)
                    return;

                // Test connection BLE
                if (device.getName().equals("GattServer"))
                {
                    bluetoothAdapter.cancelDiscovery();
                    System.out.println("µC Trouvé");
                    Intent deviceControlActivity_intent = new Intent(getApplicationContext(), DeviceControlActivity.class);

                    // Put the device address in the intent
                    Bundle bundle = new Bundle();
                    bundle.putString("DeviceAddress", device.getAddress());
                    deviceControlActivity_intent.putExtras(bundle);

                    // Switch activities
                    startActivity(deviceControlActivity_intent);
                    finish();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
        System.out.println("Test");

        // Step 1. get bluetooth adapter
        bluetoothManager = getSystemService(BluetoothManager.class);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_adapter_null, Toast.LENGTH_SHORT).show();
            finish();
        }

        // Step 2. Enable bluetooth
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Step 3. Prepare scanning parameters
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);

        setContentView(R.layout.activity_main2);

        // Set the main activity to be this activity
        activity = this;

        // Get the android image
        this.play = (Button) findViewById(R.id.play);
        this.scan = (Button) findViewById(R.id.scan);

        // Set an OnClick event on the android image
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Declare which activity you want to be activated
                Intent otherActivity = new Intent(getApplicationContext(), CookieActivity.class);

                // Start this new activity
                startActivity(otherActivity);

                // End the current activity
                finish();
            }
        });
        
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled())
                    Toast.makeText(getApplicationContext(), "Le bluetooth n'est pas activé...", Toast.LENGTH_SHORT).show();
                else
                {
                    scanBluetoothDevice();
                    scanLeDevice();
                }
            }
        });

        // Dynamic Layout
        this.myDynamicLayout = (LinearLayout) findViewById(R.id.myDynamicLayout);
        this.imageChopper = (ImageView) findViewById(R.id.imageChopper);

        imageChopper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // SIMPLE POP-UP
                AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);
                chopperPopup.setTitle("BLE");
                chopperPopup.setMessage("Rechercher des appareils BLE ?");
                chopperPopup.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Chopper a la flemme...", Toast.LENGTH_SHORT).show();
                    }
                });
                chopperPopup.setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "Chopper attend...", Toast.LENGTH_SHORT).show();
                    }
                });

                chopperPopup.show();

                // PERSONALIZED POP-UP
                /*CustomPopup customPopup = new CustomPopup(activity);
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
                customPopup.build();*/
            }
        });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(receiver);
    }

    private void scanBluetoothDevice()
    {
        // Paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                System.out.println("\nPaired Device.\nName: " + device.getName() + "\nMAC: " + device.getAddress());
            }
        }

        // Discover devices
        isBluetoothDiscoveryStarted = bluetoothAdapter.startDiscovery();
    }

    private void scanLeDevice()
    {
        if (bluetoothLeScanner == null)
        {
            Toast.makeText(activity, "BluetoothLeScanner is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!scanning) {
            // Scan until defined period elapsed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    Toast.makeText(getApplicationContext(), "Chopper a cherché pendant 10 secondes...", Toast.LENGTH_SHORT).show();
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            Toast.makeText(getApplicationContext(), "Chopper recherche des appareils !", Toast.LENGTH_SHORT).show();
        }
        else
        {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            Toast.makeText(getApplicationContext(), "Chopper arrête sa recherche !", Toast.LENGTH_SHORT).show();
        }

    }
}