package com.example.moharu.sandev;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Locale;
import java.util.UUID;

public class ProximityActivity extends Activity {

    ArrayAdapter<String> deviceList;
    ListView deviceListContainer;
    BluetoothAdapter bluetoothAdapter;
    private Boolean isInfiniteScanning = false;
    Button ToggleScanButton;
    TextToSpeech tts;
    private Locale pt_BR;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proximity);
        // Bluetooth Adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // DeviceList
        deviceList = new ArrayAdapter<String>(ProximityActivity.this, android.R.layout.simple_list_item_1);
        deviceListContainer = (ListView) findViewById(R.id.device_list);
        deviceListContainer.setAdapter(deviceList);
        // receivers
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
        // Turns on device BT
        enableBluetooth();
        // TextToSpeech
        tts = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int status) {
                        tts.setLanguage(pt_BR);
                    }
                });
        // Toggle scan button
        ToggleScanButton = (Button) findViewById(R.id.toggle_scan);
        ToggleScanButton.setOnClickListener(toggleScanListener);
    }

    private Button.OnClickListener toggleScanListener = new Button.OnClickListener(){
        @Override
        public void onClick(View v){
            toggleInfiniteScan();
        }
    };

    private void toggleInfiniteScan (){
        isInfiniteScanning = !isInfiniteScanning;
        toggleScanView();
        if(!isInfiniteScanning && bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
        }
        if(isInfiniteScanning){
            bluetoothAdapter.startDiscovery();
        }
    }

    private void toggleScanView(){
        if(isInfiniteScanning){
            deviceList.clear();
            ToggleScanButton.setText(R.string.end_scan);
        } else {
            ToggleScanButton.setText(R.string.start_scan);
        }
    }

    private void enableBluetooth(){
        if(bluetoothAdapter == null){
            Toast.makeText(ProximityActivity.this, R.string.bluetooth_not_available, Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()){
                bluetoothAdapter.cancelDiscovery();
            } else {
                bluetoothAdapter.enable();
            }
        }
    }

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                short signal = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                String deviceName = device.getName();
                if(deviceName.startsWith("BSA")){
                    int signalThreshold;
                    switch (deviceName.substring(deviceName.indexOf("0"))){
                        case "00": signalThreshold = -63; break;
                        case "01": signalThreshold = -67; break;
                        case "02": signalThreshold = -72; break;
                        case "03": signalThreshold = -75; break;
                        case "04": signalThreshold = -79; break;
                        case "05": signalThreshold = -83; break;
                        case "06": signalThreshold = -87; break;
                        case "07": signalThreshold = -91; break;
                        case "08": signalThreshold = -95; break;
                    }
                }
                deviceList.add(device.getName()+" "+signal+" dBm.");
                deviceList.notifyDataSetChanged();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                Toast.makeText(ProximityActivity.this, R.string.stopped_searching_for_devices, Toast.LENGTH_SHORT).show();
                if(isInfiniteScanning)
                    bluetoothAdapter.startDiscovery();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){
                Toast.makeText(ProximityActivity.this, R.string.searching_for_devices, Toast.LENGTH_SHORT).show();
            }
        }
    };

}
