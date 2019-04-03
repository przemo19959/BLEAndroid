package com.example.myapp1;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "moje";
    private BluetoothAdapter bleAdapter;
    private BluetoothGatt bleGATT;

    private final List<String> devicesAddresses = new ArrayList<>();

    private final List<BleDevice> devices = new ArrayList<>();
    private ArrayAdapter<BleDevice> customListAdapter;

    private Button enableDisableBLEButton;
    private TextView textArea;
    private EditText sendField;
    private Map<Integer, String> propsMap = new HashMap<>();

    private final String TxUUID = "569a2000-b87f-490c-92cb-11ba5ea5167c";
    private final String RxUUID = "569a2001-b87f-490c-92cb-11ba5ea5167c";

    private BluetoothGattCharacteristic RxChar;
    private BluetoothDevice bleDevice;

    private ScanCallback leScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            String deviceAddress = result.getDevice().getAddress();
            if (!devicesAddresses.contains(deviceAddress)) {
                Log.d(TAG, "Dodano adres: " + deviceAddress);
                BleDevice tmp = new BleDevice(result.getDevice().getName(), result.getRssi(), result.getDevice().getAddress());
                tmp.setDeviceHandler(result.getDevice());
                devices.add(tmp);
                devicesAddresses.add(deviceAddress);
            } else {
                for (BleDevice dev : devices) {
                    if (dev.getAddress().equals(deviceAddress)) { //aktualizuj jeśli obiekt jest już na liście.
                        dev.setRssi(result.getRssi());
                        Log.d(TAG, "Aktualizacja");
                        break;
                    }
                }
            }
            customListAdapter.notifyDataSetChanged();
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Error: " + errorCode);
        }
    };

    private void printServiceInfo(BluetoothGattService service) {
        String serviceInfoString = "";
        switch (service.getType()) {
            case BluetoothGattService.SERVICE_TYPE_PRIMARY: {
                serviceInfoString = "Serwis typu: PRIMARY\n";
                Log.d(TAG, "Serwis typu: PRIMARY");
                break;
            }
            case BluetoothGattService.SERVICE_TYPE_SECONDARY: {
                serviceInfoString = "Serwis typu: SECONDARY\n";
                Log.d(TAG, "Serwis typu: SECONDARY");
                break;
            }
        }
        final String tmp = serviceInfoString;
        runOnUiThread(() -> textArea.append(tmp + "UUID: " + service.getUuid() + "\n"));
    }

    private BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "Status: " + status);
            Log.d(TAG, "Stan połączenia: " + newState);
            switch (status) {
                case BluetoothGatt.GATT_SUCCESS:
                    runOnUiThread(() -> textArea.append("Z sukcesem "));
                    break;
                case BluetoothGatt.GATT_FAILURE:
                    runOnUiThread(() -> textArea.append("Błąd operacji!\n"));
                    break;
            }
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED: {
                    runOnUiThread(() -> textArea.append("połączono się z urządzeniem: " + gatt.getDevice().getName() + "\n\n"));
                    if (!gatt.discoverServices())
                        Log.d(TAG, "Error: service discovery!!");
                    break;
                }
                case BluetoothGatt.STATE_DISCONNECTED:
                    runOnUiThread(() -> textArea.append("rozłączono się!\n\n"));
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.d(TAG, "serwisy odszukane!");
            for (BluetoothGattService service : bleGATT.getServices()) {
                printServiceInfo(service);
                Log.d(TAG, "No. char: " + service.getCharacteristics().size());
                for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                    printCharacteristicInfo(characteristic);

                    if (characteristic.getUuid().equals(UUID.fromString(RxUUID))) {
                        RxChar = characteristic;
                    }
                    Log.d(TAG, "No. desc: " + characteristic.getDescriptors().size());
                    for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                        printDescriptorInfo(descriptor);

                        if (characteristic.getUuid().equals(UUID.fromString(TxUUID))) {
                            Log.d(TAG, "Włączono powiadomienia");
                            gatt.setCharacteristicNotification(characteristic, true);
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                }
                runOnUiThread(() -> textArea.append("\n"));
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "Charakterystka zmieniona");
            runOnUiThread(() -> textArea.append(new String(characteristic.getValue(), StandardCharsets.UTF_8)));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "Charakterystka zapisana");
//            runOnUiThread(()->textArea.append(characteristic.getValue()+"\n"));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            printCharacteristicInfo(characteristic);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            printDescriptorInfo(descriptor);
        }
    };

    private void printCharacteristicInfo(BluetoothGattCharacteristic charVar) {
        Log.d(TAG, "char printing...");
        int propTmp = charVar.getProperties();
        int permTmp = charVar.getPermissions();
        final String characteristicInfoString = "Charakterystyka:\n\tUUID: " + charVar.getUuid() +
                "\n\tProp: " + propTmp + " [" + decodeProperties(propTmp) + "]\n\tPerm: " +
                permTmp + " [" + decodePermissions(permTmp) + "]\n";
        runOnUiThread(() -> textArea.append(characteristicInfoString));
    }

    private void printDescriptorInfo(BluetoothGattDescriptor descVar) {
        Log.d(TAG, "desc printing...");
        int permTmp = descVar.getPermissions();
        final String descriptorInfoString = "\t\tDeskryptor: " + permTmp + " [" + decodePermissions(permTmp) + "]\n\t\t" +
                "Values: " + Arrays.toString(descVar.getValue()) + "\n";
        runOnUiThread(() -> textArea.append(descriptorInfoString));
    }


    private String decodeProperties(int prop) {
        String result = "";
        for (int i = 0; i < 6; i++) {
            if ((prop & (1 << i)) == (1 << i))
                result += propsMap.get(i);
        }
        return result;
    }

    private String decodePermissions(int perm) {
        String result = "";
        switch (perm) {
            case BluetoothGattCharacteristic.PERMISSION_READ:
                result = "READ";
                break;
            case BluetoothGattCharacteristic.PERMISSION_WRITE:
                result = "WRITE";
                break;
            default:
                result = "Unsupported";
        }
        return result;
    }

    private void startScanning() {
        if (bleAdapter == null)
            return;
        Log.d(TAG, "Scanning started");
        bleAdapter.getBluetoothLeScanner().startScan(leScanCallback);
    }

    private void stopScanning() {
        if (bleAdapter == null)
            return;
        bleAdapter.getBluetoothLeScanner().stopScan(leScanCallback);
        Log.d(TAG, "Scanning stopped");
    }

    void initBLEAdapter() {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
    }

    void enableDisableBLE() {
        if (bleAdapter == null || !bleAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
//            enableDisableBLEButton.setText(R.string.turnOffBLE);
        } else if (bleAdapter != null && bleAdapter.isEnabled()) {
            gattServerDisconnet();
            bleAdapter.disable();
//            enableDisableBLEButton.setText(R.string.turnOnBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        if(menu instanceof MenuBuilder){
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enableBle:
                initBLEAdapter();
                enableDisableBLE();
                return true;

            case R.id.scanBle:
                if (bleAdapter != null) {
                    showDialog();
                } else
                    Toast.makeText(this, "Włącz BLE!!!", Toast.LENGTH_SHORT).show();
                return true;

            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        propsMap.put(0, "Broadcast");
        propsMap.put(1, "Read");
        propsMap.put(2, "WriteNoResponse");
        propsMap.put(3, "Write");
        propsMap.put(4, "Notify");
        propsMap.put(5, "Indicate");

        textArea = findViewById(R.id.textArea);
        textArea.setMovementMethod(new ScrollingMovementMethod()); //dodanie paska

        sendField = findViewById(R.id.sendField);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener((v) -> {
            if (!sendField.getText().toString().equals("") && bleDevice != null && bleGATT != null) {
                if (RxChar != null && RxChar.setValue(sendField.getText().toString()))
                    bleGATT.writeCharacteristic(RxChar);
            }
        });

//        Button scanButton = findViewById(R.id.scanBLE);
//        scanButton.setOnClickListener((v) -> {
//            if (bleAdapter != null) {
//                showDialog();
//            } else
//                Toast.makeText(this, "Włącz BLE!!!", Toast.LENGTH_SHORT).show();
//        });

//        enableDisableBLEButton = findViewById(R.id.enableBLEButton);
//        enableDisableBLEButton.setOnClickListener((v) -> {
//            initBLEAdapter();
//            enableDisableBLE();
//        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gattServerDisconnet();
        turnOffBLE();
    }

    private void showDialog() {
        devices.clear();
        devicesAddresses.clear();
        startScanning();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Wybierz urządzenie:");
        dialog.setCancelable(false);
        customListAdapter = new BleDeviceArrayAdapter(this, 0, devices);

        //przycisk zamykający okno dialogowe i zatrzymujący skanowanie BLE
        dialog.setNegativeButton("Zamknij", (dialog2, which) -> {
            stopScanning();
            dialog2.dismiss();
        });

        dialog.setAdapter(customListAdapter, (dialog2, which) -> {
            stopScanning();
            bleDevice = customListAdapter.getItem(which).getDeviceHandler();
            connectToServer(bleDevice); //podłącz się do
        });
        dialog.show();
    }

    private void connectToServer(BluetoothDevice bluetoothDevice) {
        bleGATT = bluetoothDevice.connectGatt(this, false, bleGattCallback);
        bleGATT.discoverServices();
        if (bleGATT == null)
            Toast.makeText(this, "Błąd: nie można się połączyć z urządzeniem!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Poprawnie połączono!", Toast.LENGTH_SHORT).show();
    }

    private void gattServerDisconnet() {
        if (bleGATT != null)
            bleGATT.disconnect();
    }

    private void turnOffBLE() {
        if (bleAdapter != null && bleAdapter.isEnabled())
            bleAdapter.disable();
    }
}
