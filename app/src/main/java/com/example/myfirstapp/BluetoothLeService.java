package com.example.myfirstapp;

import android.Manifest;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.List;

public class BluetoothLeService extends Service
{
    public final static String ACTION_GATT_CONNECTED = "com.example.myfirstapp.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "com.example.myfirstapp.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "com.example.myfirstapp.ACTION_GATT_SERVICES_DISCOVERED";

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTED = 2;

    private int connectionState;

    public static final String TAG = "BluetoothLeService";
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothGatt bluetoothGatt;

    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback()
    {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
        {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED)
            {
                // Successfully connected to the GATT server
                connectionState = STATE_CONNECTED;
                broadcastUpdate(ACTION_GATT_CONNECTED);

                // Attemps to discover services after successful connection.
                //discoverServices();
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED)
            {
                // Disconnected from the GATT server
                connectionState = STATE_DISCONNECTED;
                broadcastUpdate(ACTION_GATT_DISCONNECTED);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status)
        {
            if (status == BluetoothGatt.GATT_SUCCESS)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            else
                Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    };

    public boolean discoverServices()
    {
        return bluetoothGatt.discoverServices();
    }

    public boolean initialize()
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null)
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    public boolean connect(final String address)
    {
        if (bluetoothAdapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        try
        {
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
            // connect to the GATT server on the device
            bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            Log.w(TAG, "Device not found with provided address.");
            return false;
        }
    }

    private void broadcastUpdate(final String action)
    {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    class LocalBinder extends Binder
    {
        public BluetoothLeService getService()
        {
            return BluetoothLeService.this;
        }
    }

    private LocalBinder binder = new LocalBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        close();
        return super.onUnbind(intent);
    }

    private void close()
    {
        if (bluetoothGatt == null)
            return;

        bluetoothGatt.close();
        bluetoothGatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (bluetoothGatt == null)
            return null;

        return bluetoothGatt.getServices();
    }
}
