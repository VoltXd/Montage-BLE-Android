package com.example.myfirstapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeviceControlActivity extends AppCompatActivity
{
    public static final String TAG = "DeviceControlActivity";
    private static final String LIST_NAME = "LIST_NAME";
    private static final String LIST_UUID = "LIST_UUID";
    private BluetoothLeService bluetoothLeService;
    private String deviceAddress;
    private TextView connectionStatus_textView;
    private Button connectButton;
    private Button discoverServicesButton;
    private ExpandableListView gattServicesList;
    private TextView data_textView;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics;
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
        gattServicesList = findViewById(R.id.gatt_services_list);
        gattServicesList.setOnChildClickListener(new ExpandableListView.OnChildClickListener()
        {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id)
            {
                if (mGattCharacteristics != null)
                {
                    final BluetoothGattCharacteristic characteristic = mGattCharacteristics.get(groupPosition).get(childPosition);
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                    {
                        if (false)
                        {
                            // TODO: IF statement to notifications clear
                        }
                        bluetoothLeService.readCharacteristic(characteristic);
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                    {
                        // TODO: IF statement to manage notifications
                    }
                    return true;
                }
                return false;
            }
        });
        data_textView = findViewById(R.id.data_value);

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

    private void clearGattList()
    {
        gattServicesList.setAdapter((SimpleExpandableListAdapter)null);
        data_textView.setText(getResources().getString(R.string.no_data));
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
            else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))
            {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayData(String data)
    {
        if (data != null)
            data_textView.setText(data);
    }

    // Demonstrates how to iterate through the supported GATT
    // Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the
    // ExpandableListView on the UI.
    private void displayGattServices(List<BluetoothGattService> supportedGattServices)
    {
        System.out.println("SERVICES DECOUVERTS");
        if (supportedGattServices == null)
            return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : supportedGattServices)
        {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics = gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
            {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }
        // Display
        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[] { LIST_NAME, LIST_UUID},
                new int[] { android.R.id.text1, android.R.id.text2}
        );
        gattServicesList.setAdapter(gattServiceAdapter);
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
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
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