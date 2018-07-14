package com.sample.kontaktio;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class BeaconActivity extends AppCompatActivity {


    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 300000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;

    AVLoadingIndicatorView scanning;

    ImageView locationHere , locationLeft;

    TextView rangeValue;

    String TAG = "MyBeaconApp";

    int flag = -1;

    int txPower = 0;

    long distance = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanning = (AVLoadingIndicatorView) findViewById(R.id.scanning);

        locationHere = (ImageView) findViewById(R.id.location_here);
        locationLeft = (ImageView) findViewById(R.id.location_left);
        rangeValue = (TextView) findViewById(R.id.range_value);

        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                        .build();
                filters = new ArrayList<ScanFilter>();

                scanLeDevice(true);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {

        Log.d(TAG,"scanLeDevice "+enable);

        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        Log.d(TAG,"stopScan <<<>>> ");
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        Log.d(TAG,"stopScan >>> ");
                        mLEScanner.stopScan(mScanCallback);

                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                Log.d(TAG,"startScan >>> ");
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                Log.d(TAG,"startScan  << ");
                mLEScanner.startScan(filters, settings, mScanCallback);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                Log.d(TAG,"stopScan---- ");
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                Log.d(TAG,"stopScan---->> ");
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }


    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            Log.d(TAG,"ScanResult "+result);

            if(result!=null) {

                BluetoothDevice device = result.getDevice();

                String currentLocation = "";

                int rssi = result.getRssi();

                String address = device.getAddress();

                if(txPower==0) {
                    List<ADStructure> structures =
                            ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
                    txPower = calculateTxpower(structures);
                }

                if(txPower!=0) {
                    distance = Math.round(calculateAccuracy(txPower, rssi));
                }

                if(address.equals(BeaconConstant.address)){


                          scanning.setVisibility(View.GONE);

                          if (distance <= BeaconConstant.range) {

                              currentLocation = "You are here";

                              locationHere.setVisibility(View.VISIBLE);
                              locationLeft.setVisibility(View.GONE);
                          } else {

                              currentLocation = "You left";


                              locationLeft.setVisibility(View.VISIBLE);
                              locationHere.setVisibility(View.GONE);

                          }

                          rangeValue.setText("" + distance);



                }

                Log.d(TAG, " Data : " + address + " ==> " + distance + " = "+rssi+" , "+txPower+" == " + currentLocation);

            }

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.i(TAG, "onBatchScanResults "+results);
            for (ScanResult sr : results) {
                Log.i(TAG, "Batch "+sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Error Code: " + errorCode);
        }
    };

    private int calculateTxpower(List<ADStructure> structures) {

        int txPower = 0;

        for (ADStructure structure : structures) {

            if (structure instanceof EddystoneUID) {

                EddystoneUID es = (EddystoneUID) structure;

                txPower = es.getTxPower();

            } else if(structure instanceof EddystoneURL){


                EddystoneURL es = (EddystoneURL) structure;

                txPower = es.getTxPower();


            }
        }

        return txPower;

    }


    protected static double calculateAccuracy(int txPower, int rssi) {
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

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

                }
            };

}
