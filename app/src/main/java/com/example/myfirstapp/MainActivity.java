package com.example.myfirstapp;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class MainActivity extends AppCompatActivity
{
    private static final int REQUEST_ENABLE_BT = 0;

    private Button play;
    private Button scan;
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

    private ImageView drawableChopper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        drawableChopper = new ImageView(this);
        drawableChopper.setImageResource(R.drawable.chopper);

        // Set the main activity to be this activity
        activity = this;

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
        {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!IfDef.INITIALISATION_BLE)
        {
            // Chopper nous dit comment initialiser le BLE
            AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);

            chopperPopup.setTitle("Il faut initialiser le BLE !");
            chopperPopup.setMessage("On doit récupérer un \"BluetoothAdapter\" et vérifier que le Bluetooth du téléphone est activé.\n\n" +
                                    "ATTENTION : Il faut vérifier que le \"BluetoothAdapter\" récupéré ne soit pas NULL !");
            chopperPopup.setNeutralButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            chopperPopup.setView(drawableChopper);

            chopperPopup.show();
        }


        if (IfDef.INITIALISATION_BLE)
        {
            // Step 1. get bluetooth adapter
            bluetoothManager = getSystemService(BluetoothManager.class);
            bluetoothAdapter = bluetoothManager.getAdapter();
            if (bluetoothAdapter == null)
            {
                Toast.makeText(this, R.string.ble_adapter_null, Toast.LENGTH_SHORT).show();
                finish();
            }

            // Step 2. Enable bluetooth
            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            if (!IfDef.SCAN_BLE)
            {
                // Chopper nous dit comment effectuer un scan des appareils disponibles
                AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);

                chopperPopup.setTitle("Bluetooth activé !");
                chopperPopup.setMessage("Il faut maintenant scanner les appareils disponibles ! Pour cela 2 méthodes :\n\n" +
                                        "1. Scan Bluetooth\n\n" +
                                        "2. Scan BLE");
                chopperPopup.setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                chopperPopup.setView(drawableChopper);

                chopperPopup.show();
            }

            if (IfDef.SCAN_BLE)
            {
                // On est censé pouvoir effectuer un scan
                AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);

                chopperPopup.setTitle("Scan disponible !");
                chopperPopup.setMessage("Appuie sur SCAN pour chercher des appareils !");
                chopperPopup.setNeutralButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {

                    }
                });
                chopperPopup.setView(drawableChopper);

                chopperPopup.show();
                // Step 3. Prepare scanning parameters
                bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(receiver, filter);

                setContentView(R.layout.activity_main2);

                // Get the android image
                this.play = (Button) findViewById(R.id.play);
                this.scan = (Button) findViewById(R.id.scan);

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

                scan.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        if (!bluetoothAdapter.isEnabled())
                            Toast.makeText(getApplicationContext(), "Le bluetooth n'est pas activé...", Toast.LENGTH_SHORT).show();
                        else
                        {
                            scanBluetoothDevice();
                            scanLeDevice();
                        }
                    }
                });

                this.imageChopper = (ImageView) findViewById(R.id.imageChopper);

                imageChopper.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        // SIMPLE POP-UP
                        AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);
                        chopperPopup.setTitle("BLE");
                        chopperPopup.setMessage("Rechercher des appareils BLE ?");
                        chopperPopup.setNegativeButton("Non", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Toast.makeText(getApplicationContext(), "Chopper a la flemme...", Toast.LENGTH_SHORT).show();
                            }
                        });

                        chopperPopup.show();
                    }
                });
            }
        }
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
                    if (!IfDef.GOTO_CONTROL_ACTIVITY)
                    {
                        // On est censé pouvoir effectuer un scan
                        AlertDialog.Builder chopperPopup = new AlertDialog.Builder(activity);

                        chopperPopup.setTitle("µC Trouvé !");
                        chopperPopup.setMessage("On a trouvé notre carte NUCLEO-WB55RG !\n\n" +
                                                "Il va falloir se connecter à son serveur GATT.");
                        chopperPopup.setNeutralButton("OK", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {

                            }
                        });
                        //chopperPopup.setView(drawableChopper);

                        chopperPopup.show();
                        return;
                    }

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

    // Scan stops after 10 seconds
    private final long SCAN_PERIOD = 10000;
    private void scanLeDevice()
    {
        if (bluetoothLeScanner == null)
        {
            Toast.makeText(activity, "BluetoothLeScanner is null", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!scanning)
        {
            // Scan until defined period elapsed
            handler.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    scanning = false;
                    Toast.makeText(getApplicationContext(), "Recherche BLE trop longue...", Toast.LENGTH_SHORT).show();
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
            Toast.makeText(getApplicationContext(), "Recherche d'appareils !", Toast.LENGTH_SHORT).show();
        }
        else
        {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
            Toast.makeText(getApplicationContext(), "Arrêt de la recherche !", Toast.LENGTH_SHORT).show();
        }
    }

    private ScanCallback leScanCallback = new ScanCallback()
    {
        @Override
        public void onScanResult(int callbackType, ScanResult result)
        {
            super.onScanResult(callbackType, result);

            System.out.println("\nDiscovered LE device.\nName: " + result.getDevice().getName() + "\nMAC: " + result.getDevice().getAddress());
        }
    };
}