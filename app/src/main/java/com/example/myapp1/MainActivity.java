package com.example.myapp1;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "moje";

    private BluetoothAdapter bleAdapter;
    private final List<BleDevice> devices = new ArrayList<>();
    private ArrayAdapter<BleDevice> customListAdapter;
    private BleScanCallback bleScanCallback;
    private BluetoothDevice bleDevice;
    private BleGattCallback bleGattCallback;
    private BluetoothGatt bleGATT;

    private Menu mainMenu;
    private boolean bleOnFlag=false;

    private TextView textArea;
    private EditText sendField;

    public void printData(String data){
        runOnUiThread(() -> textArea.append(data));
    }

    private void startScanning() {
        if (bleAdapter == null)
            return;
        Log.d(TAG, "Scanning started");
        bleAdapter.getBluetoothLeScanner().startScan(bleScanCallback);
    }

    private void stopScanning() {
        if (bleAdapter == null)
            return;
        bleAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        Log.d(TAG, "Scanning stopped");
    }

    private void initBLEAdapter() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        if(bleAdapter.isEnabled()) {
            mainMenu.findItem(R.id.enableBle).setTitle(R.string.turnOffBLE);
            mainMenu.findItem(R.id.enableBle).setIcon(R.drawable.ble_icon_off);
            bleOnFlag=true;
        }
//      requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
    }

    private void enableBLE(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
        bleOnFlag=true;
        mainMenu.findItem(R.id.enableBle).setTitle(R.string.turnOffBLE);
        mainMenu.findItem(R.id.enableBle).setIcon(R.drawable.ble_icon_off);
    }

    private void disableBLE(){
        gattServerDisconnet();
        bleAdapter.disable();
        bleOnFlag=false;
        mainMenu.findItem(R.id.enableBle).setTitle(R.string.turnOnBLE);
        mainMenu.findItem(R.id.enableBle).setIcon(R.drawable.ble_icon);
    }

    private void enableDisableBLE() {
        if (bleAdapter == null || !bleAdapter.isEnabled()) //jeśli BLE nie jest włączone
            enableBLE();
        else if (bleOnFlag && bleAdapter != null && bleAdapter.isEnabled()) //jeśli BLE jest włączone
            disableBLE();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mainMenu=menu;
        if(menu instanceof MenuBuilder){
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }
        initBLEAdapter();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.enableBle:
                enableDisableBLE();
                return true;
            case R.id.scanBle:
                if (bleAdapter != null) {
                    showDialog();
                } else
                    Toast.makeText(this, "Włącz BLE!!!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.clearArea:
                textArea.setText("");
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

        customListAdapter = new BleDeviceArrayAdapter(this, 0, devices);
        bleScanCallback=new BleScanCallback(devices,customListAdapter);
        bleGattCallback=new BleGattCallback(this);

        textArea = findViewById(R.id.textArea);
        textArea.setMovementMethod(new ScrollingMovementMethod()); //dodanie paska

        sendField = findViewById(R.id.sendField);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener((v) -> {
            if (!sendField.getText().toString().equals("") && bleDevice != null && bleGATT != null) {
                bleGattCallback.sendDataToServer(bleGATT,sendField.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gattServerDisconnet();
        turnOffBLE();
    }

    private void showDialog() {
        devices.clear();
        bleScanCallback.clearAddressList();
        startScanning();

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Wybierz urządzenie:");
        dialog.setCancelable(false);


        //przycisk zamykający okno dialogowe i zatrzymujący skanowanie BLE
        dialog.setNegativeButton("Zamknij", (dialog2, which) -> {
            stopScanning();
            dialog2.dismiss();
        });

        dialog.setAdapter(customListAdapter, (dialog2, which) -> {
            stopScanning();
            bleDevice = customListAdapter.getItem(which).getDeviceHandler();
            connectToServer(bleDevice); //podłącz się do serwera
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
