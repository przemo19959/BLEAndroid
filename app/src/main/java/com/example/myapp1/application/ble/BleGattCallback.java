package com.example.myapp1.application.ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import android.util.SparseArray;

import com.example.myapp1.MainActivity;
import com.example.myapp1.TerminalTab;
import com.example.myapp1.application.commons.Commons;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * Ta klasa implementuje funkcje zwrotne profilu GATT BLE.
 */
public class BleGattCallback extends BluetoothGattCallback {
    private SparseArray<String> props=new SparseArray<>();
    private BluetoothGattCharacteristic RxChar;
//    private MainActivity mainActivity;
//    private SpeedTester speedTester;
    private TerminalTab tab;

    private Commons commons;

    private static final String TxUUID = "569a2000-b87f-490c-92cb-11ba5ea5167c";
    private static final String RxUUID = "569a2001-b87f-490c-92cb-11ba5ea5167c";

    public BleGattCallback(TerminalTab terminalTab,Commons commons) {
        initMap();
//        this.mainActivity=mainActivity;
        this.tab=terminalTab;
        this.commons=commons;
//        speedTester=new SpeedTester();
    }

    private void initMap(){
        props.put(0, "Broadcast");
        props.put(1, "Read");
        props.put(2, "WriteNoResponse");
        props.put(3, "Write");
        props.put(4, "Notify");
        props.put(5, "Indicate");
    }

    public BluetoothGattCharacteristic getRxChar() {
        return RxChar;
    }
//    public SpeedTester getSpeedTester(){return speedTester;}

    public void sendDataToServer(BluetoothGatt gatt,String data){
        if (RxChar != null && RxChar.setValue(data))
            gatt.writeCharacteristic(RxChar);
    }

    /**
     * Funkcja jest wywoływana, gdy stan połączenia klienta(telefon) a serwera uległ zmianie.
     * funkcja ta na bierząco informuje o aktualnym stanie połączenia urządzeń BLE. Jeśli urządzenia
     * poprawnie się połączą, wysłane zostaje żądanie odkrycia serwisów i charakterystyk serwera BLE.
     * @param gatt - serwer GATT
     * @param status - status
     * @param newState - nowy stan
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(MainActivity.TAG, "Status: " + status);
        Log.d(MainActivity.TAG, "Stan połączenia: " + newState);
        switch (status) {
            case BluetoothGatt.GATT_SUCCESS:
                tab.printData("Z sukcesem ");
                break;
            case BluetoothGatt.GATT_FAILURE:
                tab.printData("Błąd operacji!\n");
                break;
        }
        switch (newState) {
            case BluetoothGatt.STATE_CONNECTED: {
                tab.printData("połączono się z urządzeniem: " + gatt.getDevice().getName() + "\n\n");
                if (!gatt.discoverServices())
                    Log.d(MainActivity.TAG, "Error: service discovery!!");
                break;
            }
            case BluetoothGatt.STATE_DISCONNECTED:
                tab.printData("rozłączono się!\n\n");
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
                        gatt.setCharacteristicNotification(characteristic, true);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                        Log.d(MainActivity.TAG, "Włączono powiadomienia");
                    }
                }
            }
            tab.printData("\n");
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String data=new String(characteristic.getValue(), StandardCharsets.UTF_8);
        commons.addDataToBuffer(data); //dodaj do bufora dane.


//        mainActivity.printSpeedInfo(speedTester.getSpeedOneFrame(data));
//        mainActivity.printSpeedInfo(data);

//        Log.d(MainActivity.TAG, "Dane dodane do bufora");
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(MainActivity.TAG, "Dane wysłane do serwera");
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
        tab.printData(tmp + "UUID: " + service.getUuid() + "\n");
    }

    private void printCharacteristicInfo(BluetoothGattCharacteristic charVar) {
        Log.d(MainActivity.TAG, "char printing...");
        int propTmp = charVar.getProperties();
        int permTmp = charVar.getPermissions();
        final String characteristicInfoString = "Charakterystyka:\n\tUUID: " + charVar.getUuid() +
                "\n\tProp: " + propTmp + " [" + decodeProperties(propTmp) + "]\n\tPerm: " +
                permTmp + " [" + decodePermissions(permTmp) + "]\n";
        tab.printData(characteristicInfoString);
    }

    private void printDescriptorInfo(BluetoothGattDescriptor descVar) {
        Log.d(MainActivity.TAG, "desc printing...");
        int permTmp = descVar.getPermissions();
        final String descriptorInfoString = "\t\tDeskryptor: " + permTmp + " [" + decodePermissions(permTmp) + "]\n\t\t" +
                "Values: " + Arrays.toString(descVar.getValue()) + "\n";
        tab.printData(descriptorInfoString);
    }

    private String decodeProperties(int prop) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            if ((prop & (1 << i)) == (1 << i))
                result.append(props.get(i));
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
