package com.example.myfirstapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

public class DeviceControlActivity extends AppCompatActivity
{
    public static final String TAG = "DeviceControlActivity";
    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private TextView connectionStatus_textView;
    private Button connectButton;
    private Button discoverServicesButton;
    private ServiceConnection serviceConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            bluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (bluetoothLeService != null)
            {
                if (!bluetoothLeService.initialize())
                {
                    Log.e(TAG, "Unable to initialize Bluetooth");
                    finish();
                }
                // Perform device connection
                bluetoothLeService.connect(deviceAddress);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            bluetoothLeService = null;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        connectionStatus_textView = findViewById(R.id.connection_status);
        connectButton = findViewById(R.id.gatt_connect_button);
        discoverServicesButton = findViewById(R.id.gatt_discover_services_button);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null)
            deviceAddress = bundle.getString("DeviceAddress");

        connectButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (bluetoothLeService != null)
                    bluetoothLeService.connect(deviceAddress);
            }
        });

        discoverServicesButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (bluetoothLeService != null)
                    bluetoothLeService.discoverServices();
            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        getApplicationContext().bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean connected;

    private final BroadcastReceiver gattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                connected = true;
                updateConnectionState(R.string.connected);

                connectButton.setEnabled(false);
                discoverServicesButton.setEnabled(true);
            }
            else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action))
            {
                connected = false;
                updateConnectionState(R.string.disconnected);
                connectButton.setEnabled(true);
                discoverServicesButton.setEnabled(false);
            }
            else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                displayGattServices(bluetoothLeService.getSupportedGattServices());
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> supportedGattServices)
    {
        System.out.println("SERVICES DECOUVERTS");
        System.out.println(supportedGattServices);
    }

    private void updateConnectionState(int connectionState)
    {
        connectionStatus_textView.setText(getResources().getString(connectionState));
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter());
        if (bluetoothLeService != null)
        {
            final boolean result = bluetoothLeService.connect(deviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(gattUpdateReceiver);
    }

    private static IntentFilter makeGattUpdateIntentFilter()
    {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        return intentFilter;
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        getApplicationContext().unbindService(serviceConnection);

        Intent mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(mainActivity);
        finish();
    }
}