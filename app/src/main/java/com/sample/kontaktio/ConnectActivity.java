package com.sample.kontaktio;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.kontakt.sdk.android.ble.connection.IKontaktDeviceConnection;
import com.kontakt.sdk.android.ble.connection.KontaktDeviceConnection;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

public class ConnectActivity extends AppCompatActivity {

    private static final String TAG = "MyBeaconApp";

    private KontaktDeviceConnection kontaktDeviceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "ConnectActivity");

        //we are passing remoteBluetoothDevice and password through intent
        RemoteBluetoothDevice remoteBluetoothDevice = getIntent().getExtras().getParcelable("beacon");

        String password = getIntent().getExtras().getString("password");
        remoteBluetoothDevice.setPassword(password.getBytes());
        connect(remoteBluetoothDevice);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void connect(RemoteBluetoothDevice remoteBluetoothDevice) {
        kontaktDeviceConnection = new KontaktDeviceConnection(this, remoteBluetoothDevice, connectionListener);
        kontaktDeviceConnection.connect();
    }

    private void disconnect() {
        if (kontaktDeviceConnection != null) {
            kontaktDeviceConnection.close();
            kontaktDeviceConnection = null;
        }
    }

    private void printCharacteristic(RemoteBluetoothDevice.Characteristics characteristics) {
        StringBuilder stringBuilder = new StringBuilder();
        String description = stringBuilder.append("proximity=").append(characteristics.getProximityUUID())
                .append("major=").append(characteristics.getMajor())
                .append("minor=").append(characteristics.getMinor())
                .append("power_level=").append(characteristics.getPowerLevel())
                .append("advertising_interval=").append(characteristics.getAdvertisingInterval())
                .append("active_profile=").append(characteristics.getActiveProfile())
                .append("model_name=").append(characteristics.getModelName())
                .append("namespace=").append(characteristics.getNamespaceId())
                .append("instanceId=").append(characteristics.getInstanceId())
                .append("url=").append(characteristics.getUrl())
                .append("manufacturer_name=").append(characteristics.getManufacturerName())
                .append("battery_level=").append(characteristics.getBatteryLevel())
                .append("firmware_version=").append(characteristics.getFirmwareRevision())
                .append("hardware_version=").append(characteristics.getHardwareRevision())
                .append("secure=").append(characteristics.isSecure())
                .toString();

        Log.d(TAG, "beacon characteristic= " + description);
    }

    private IKontaktDeviceConnection.ConnectionListener connectionListener = new IKontaktDeviceConnection.ConnectionListener() {
        @Override
        public void onConnected() {
            Log.d(TAG, "onConnected");
        }

        @Override
        public void onAuthenticationSuccess(RemoteBluetoothDevice.Characteristics characteristics) {
            Log.d(TAG, "onAuthenticationSuccess");
            //here you can read characteristic from device

            //do your actual work here
            printCharacteristic(characteristics);
            disconnect();
        }

        @Override
        public void onAuthenticationFailure(int failureCode) {
            switch (failureCode) {
                case KontaktDeviceConnection.FAILURE_WRONG_PASSWORD:
                    Log.d(TAG, "wrong password");
                    break;
                case KontaktDeviceConnection.FAILURE_UNKNOWN_BEACON:
                    Log.d(TAG, "unknow beacon");
                    break;
            }

            disconnect();
        }

        @Override
        public void onCharacteristicsUpdated(RemoteBluetoothDevice.Characteristics characteristics) {
            Log.d(TAG, "onCharacteristicsUpdated");
        }

        @Override
        public void onErrorOccured(int errorCode) {
            if (KontaktDeviceConnection.isGattError(errorCode)) {
                //low level bluetooth stack error. Most often 133
                int gattError = KontaktDeviceConnection.getGattError(errorCode);
                Log.d(TAG, "onErrorOccured gattError=" + gattError);
            } else {
                //sdk error
                Log.d(TAG, "onErrorOccured=" + errorCode);
            }

            disconnect();
        }

        @Override
        public void onDisconnected() {
            Log.d(TAG, "onDisconnected");
        }
    };
}