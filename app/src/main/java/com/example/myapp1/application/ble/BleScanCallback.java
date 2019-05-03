package com.example.myapp1.application.ble;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.example.myapp1.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Klasa implementująca funkcję wołającą (ang.callback) dla procesu skanowania BLE.
 */
public class BleScanCallback extends ScanCallback {
    private List<String> devicesAddresses=new ArrayList<>();
    private List<BleDevice> devices;
    private ArrayAdapter<BleDevice> customListAdapter;

    public BleScanCallback(List<BleDevice> devices, ArrayAdapter<BleDevice> customListAdapter) {
        this.devices = devices;
        this.customListAdapter = customListAdapter;
    }

    /**
     * Funkcja wywoływana, gdy otrzymano rozgłoszenia BLE. Początkowo pobierany jest adres urządzenia
     * jakie wysłało rozgłoszenie. Jeśli dany adres już się znajduje na liście to jego dane są aktualizowane
     * w przeciwnym razie, takie urządzenie jest dodawane jako nowy wpis do widoku listy.
     * @param callbackType
     * @param result - wynik skanowania, zwierający rozgłoszenie BLE.
     */
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
