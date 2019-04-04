package com.example.myapp1;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BleGattCallback extends BluetoothGattCallback {
    private Map<Integer, String> propsMap = new HashMap<>();
    private BluetoothGattCharacteristic RxChar;
    private MainActivity mainActivity;

    private final String TxUUID = "569a2000-b87f-490c-92cb-11ba5ea5167c";
    private final String RxUUID = "569a2001-b87f-490c-92cb-11ba5ea5167c";

    public BleGattCallback(MainActivity mainActivity) {
        initMap();
        this.mainActivity=mainActivity;
    }

    private void initMap(){
        propsMap.put(0, "Broadcast");
        propsMap.put(1, "Read");
        propsMap.put(2, "WriteNoResponse");
        propsMap.put(3, "Write");
        propsMap.put(4, "Notify");
        propsMap.put(5, "Indicate");
    }

    public BluetoothGattCharacteristic getRxChar() {
        return RxChar;
    }

    public void sendDataToServer(BluetoothGatt gatt,String data){
        if (RxChar != null && RxChar.setValue(data))
            gatt.writeCharacteristic(RxChar);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(MainActivity.TAG, "Status: " + status);
        Log.d(MainActivity.TAG, "Stan połączenia: " + newState);
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                mainActivity.printData("Z sukcesem ");
//                runOnUiThread(() -> textArea.append("Z sukcesem "));
                break;
            case BluetoothGatt.GATT_FAILURE:
                mainActivity.printData("Błąd operacji!\n");
//                runOnUiThread(() -> textArea.append("Błąd operacji!\n"));
                break;
        }
        switch (newState) {
            case BluetoothGatt.STATE_CONNECTED: {
                mainActivity.printData("połączono się z urządzeniem: " + gatt.getDevice().getName() + "\n\n");
//                runOnUiThread(() -> textArea.append("połączono się z urządzeniem: " + gatt.getDevice().getName() + "\n\n"));
                if (!gatt.discoverServices())
                    Log.d(MainActivity.TAG, "Error: service discovery!!");
                break;
            }
            case BluetoothGatt.STATE_DISCONNECTED:
                mainActivity.printData("rozłączono się!\n\n");
//                runOnUiThread(() -> textArea.append("rozłączono się!\n\n"));
                break;
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(MainActivity.TAG, "serwisy odszukane!");
        for (BluetoothGattService service : gatt.getServices()) {
            printServiceInfo(service);
            Log.d(MainActivity.TAG, "No. char: " + service.getCharacteristics().size());
            for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                printCharacteristicInfo(characteristic);

                if (characteristic.getUuid().equals(UUID.fromString(RxUUID))) {
                    RxChar = characteristic;
                }
                Log.d(MainActivity.TAG, "No. desc: " + characteristic.getDescriptors().size());
                for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                    printDescriptorInfo(descriptor);

                    if (characteristic.getUuid().equals(UUID.fromString(TxUUID))) {
                        Log.d(MainActivity.TAG, "Włączono powiadomienia");
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
            mainActivity.printData("\n");
//            runOnUiThread(() -> textArea.append("\n"));
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(MainActivity.TAG, "Charakterystka zmieniona");
        mainActivity.printData(new String(characteristic.getValue(), StandardCharsets.UTF_8));
//        runOnUiThread(() -> textArea.append(new String(characteristic.getValue(), StandardCharsets.UTF_8)));
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(MainActivity.TAG, "Charakterystka zapisana");
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        printCharacteristicInfo(characteristic);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        printDescriptorInfo(descriptor);
    }

    private void printServiceInfo(BluetoothGattService service) {
        String serviceInfoString = "";
        switch (service.getType()) {
            case BluetoothGattService.SERVICE_TYPE_PRIMARY: {
                serviceInfoString = "Serwis typu: PRIMARY\n";
                Log.d(MainActivity.TAG, "Serwis typu: PRIMARY");
                break;
            }
            case BluetoothGattService.SERVICE_TYPE_SECONDARY: {
                serviceInfoString = "Serwis typu: SECONDARY\n";
                Log.d(MainActivity.TAG, "Serwis typu: SECONDARY");
                break;
            }
        }
        final String tmp = serviceInfoString;
        mainActivity.printData(tmp + "UUID: " + service.getUuid() + "\n");
//        runOnUiThread(() -> textArea.append(tmp + "UUID: " + service.getUuid() + "\n"));
    }

    private void printCharacteristicInfo(BluetoothGattCharacteristic charVar) {
        Log.d(MainActivity.TAG, "char printing...");
        int propTmp = charVar.getProperties();
        int permTmp = charVar.getPermissions();
        final String characteristicInfoString = "Charakterystyka:\n\tUUID: " + charVar.getUuid() +
                "\n\tProp: " + propTmp + " [" + decodeProperties(propTmp) + "]\n\tPerm: " +
                permTmp + " [" + decodePermissions(permTmp) + "]\n";
        mainActivity.printData(characteristicInfoString);
//        runOnUiThread(() -> textArea.append(characteristicInfoString));
    }

    private void printDescriptorInfo(BluetoothGattDescriptor descVar) {
        Log.d(MainActivity.TAG, "desc printing...");
        int permTmp = descVar.getPermissions();
        final String descriptorInfoString = "\t\tDeskryptor: " + permTmp + " [" + decodePermissions(permTmp) + "]\n\t\t" +
                "Values: " + Arrays.toString(descVar.getValue()) + "\n";
        mainActivity.printData(descriptorInfoString);
//        runOnUiThread(() -> textArea.append(descriptorInfoString));
    }

    private String decodeProperties(int prop) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if ((prop & (1 << i)) == (1 << i))
                result.append(propsMap.get(i));
        }
        return result.toString();
    }

    private String decodePermissions(int perm) {
        switch (perm) {
            case BluetoothGattCharacteristic.PERMISSION_READ:
                return "READ";
            case BluetoothGattCharacteristic.PERMISSION_WRITE:
                return "WRITE";
            default:
                return  "Unsupported";
        }
    }
}
