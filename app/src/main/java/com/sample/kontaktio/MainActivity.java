package com.sample.kontaktio;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.configuration.scan.EddystoneScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.IBeaconScanContext;
import com.kontakt.sdk.android.ble.configuration.scan.ScanContext;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.discovery.BluetoothDeviceEvent;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerContract;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;
import com.kontakt.sdk.android.manager.KontaktProximityManager;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements ProximityManager.ProximityListener {

    private static final String TAG = "MyBeaconApp";
    String currentLocation = "Scanning Location";

    private ProximityManagerContract proximityManager;
    private ScanContext scanContext;
    List<RemoteBluetoothDevice> deviceListResult;
    ListView deviceListView;
    //TextView location , deviceLog;

    AVLoadingIndicatorView scanning;

    ImageView locationHere , locationLeft;

    TextView rangeValue;

    BeaconAdapter beaconAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceListView = (ListView) findViewById(R.id.device_list);

        scanning = (AVLoadingIndicatorView) findViewById(R.id.scanning);

        locationHere = (ImageView) findViewById(R.id.location_here);
        locationLeft = (ImageView) findViewById(R.id.location_left);
        rangeValue = (TextView) findViewById(R.id.range_value);
        deviceListResult = new ArrayList<RemoteBluetoothDevice>();

        beaconAdapter = new BeaconAdapter(this,deviceListResult);
        //deviceListView.setAdapter(beaconAdapter);


        KontaktSDK.initialize("your kontakt.io API Key");
        proximityManager = new KontaktProximityManager(this);

        

    }

    @Override
    protected void onStart() {
        super.onStart();
        proximityManager.initializeScan(getScanContext(), new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.attachListener(MainActivity.this);
            }

            @Override
            public void onConnectionFailure() {

            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        proximityManager.detachListener(this);
        proximityManager.disconnect();
    }

    private ScanContext getScanContext() {
        if (scanContext == null) {


            scanContext = new ScanContext.Builder()
                    .setScanPeriod(ScanPeriod.RANGING)
                    .setScanMode(ProximityManager.SCAN_MODE_LOW_LATENCY)
                    .setActivityCheckConfiguration(ActivityCheckConfiguration.MINIMAL)
                    .setForceScanConfiguration(ForceScanConfiguration.MINIMAL)
                    .setIBeaconScanContext(new IBeaconScanContext.Builder().build())
                    .setEddystoneScanContext(new EddystoneScanContext.Builder().setDevicesUpdateCallbackInterval(1).build())
                    .build();
        }
        return scanContext;
    }

    @Override
    public void onEvent(BluetoothDeviceEvent bluetoothDeviceEvent) {

      final List<? extends RemoteBluetoothDevice> deviceList = bluetoothDeviceEvent.getDeviceList();

        Timer timer = new Timer();

        TimerTask timerTask = new TimerTask(){

            @Override
            public void run() {

               runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                       continueScan(deviceList);

                   }
               });
            }
        };

       // timer.schedule(timerTask, 2000, 10000); //
        continueScan(deviceList);
    }

    @Override
    public void onScanStart() {
        Log.d(TAG, "scan started");
        scanning.setVisibility(View.VISIBLE);
    }

    @Override
    public void onScanStop() {
        Log.d(TAG, "scan stopped");
    }


    private void continueScan(List<? extends RemoteBluetoothDevice> deviceList){

        for(int i=0;i<deviceList.size();i++){

            int txPower =  deviceList.get(i).getTxPower();

            double rssi =  deviceList.get(i).getRssi();

            double result = calculateAccuracy(txPower, rssi);


            if(deviceList.get(i).getAddress().equals(BeaconConstant.address)) {

                if (Math.round(result) <= BeaconConstant.range) {

                    currentLocation = "You are here";

                    locationHere.setVisibility(View.VISIBLE);
                    locationLeft.setVisibility(View.GONE);
                } else {

                    currentLocation = "You left";


                    locationLeft.setVisibility(View.VISIBLE);
                    locationHere.setVisibility(View.GONE);
                    scanning.setVisibility(View.GONE);
                }


                rangeValue.setText("" + Math.round(result));
            }

            Log.d(TAG, " Data : " + deviceList.get(i).getAddress() + " ==> " + Math.round(result) + " == " + currentLocation);

        }
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }

        double ratio = rssi*1.0/txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio,10);
        }
        else {
            double accuracy =  (0.89976)*Math.pow(ratio,7.7095) + 0.111;
            return accuracy;
        }
    }

}