package com.example.myapp1;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class BleScanCallback extends ScanCallback {
    private List<String> devicesAddresses=new ArrayList<>();
    private List<BleDevice> devices;
    private ArrayAdapter<BleDevice> customListAdapter;

    public BleScanCallback(List<BleDevice> devices, ArrayAdapter<BleDevice> customListAdapter) {
        this.devices = devices;
        this.customListAdapter = customListAdapter;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        String deviceAddress = result.getDevice().getAddress();
        if (!devicesAddresses.contains(deviceAddress)) {
            Log.d(MainActivity.TAG, "Dodano adres: " + deviceAddress);
            BleDevice tmp = new BleDevice(result.getDevice().getName(), result.getRssi(), result.getDevice().getAddress());
            tmp.setDeviceHandler(result.getDevice());
            devices.add(tmp);
            devicesAddresses.add(deviceAddress);
        } else {
            for (BleDevice dev : devices) {
                if (dev.getAddress().equals(deviceAddress)) { //aktualizuj jeśli obiekt jest już na liście.
                    dev.setRssi(result.getRssi());
                    Log.d(MainActivity.TAG, "Aktualizacja");
                    break;
                }
            }
        }
        customListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScanFailed(int errorCode) {
        super.onScanFailed(errorCode);
        Log.d(MainActivity.TAG, "Error: " + errorCode);
    }

    public void clearAddressList(){
        devicesAddresses.clear();
    }
}
