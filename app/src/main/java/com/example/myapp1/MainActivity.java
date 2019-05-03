package com.example.myapp1;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapp1.application.ble.BleDevice;
import com.example.myapp1.application.ble.BleDeviceArrayAdapter;
import com.example.myapp1.application.ble.BleGattCallback;
import com.example.myapp1.application.ble.BleScanCallback;
import com.example.myapp1.application.ble.SpeedTester;
import com.example.myapp1.application.commons.Commons;
import com.example.myapp1.application.commons.DrawingThread;
import com.github.mikephil.charting.charts.LineChart;

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
    private boolean bleOnFlag = false;

    private EditText sendField;

    private Switch isTestOn;
    private EditText frameField;
    private View speedTestLayout;
    private AlertDialog.Builder speedTextDialog;
    private TextView currentFramePattern;

    private SpeedTester speedTester;

    private DrawingThread t1;
    private Commons commons;

    private SectionsPageAdapter sectionsPageAdapter;
    private ViewPager viewPager;
    private TerminalTab fragment1;
    private ChartTab fragment2;


    /**
     * Ta metoda rozpoczyna skanowanie BLE w poszukiwaniu rozgłoszeń BLE.
     */
    private void startScanning() {
        if (bleAdapter == null || bleScanCallback==null)
            return;
        Log.d(TAG, "Scanning started");
        bleAdapter.getBluetoothLeScanner().startScan(bleScanCallback);
    }

    /**
     * Ta metoda zatrzymuje skanowanie BLE.
     */
    private void stopScanning() {
        if (bleAdapter == null || bleScanCallback==null)
            return;
        bleAdapter.getBluetoothLeScanner().stopScan(bleScanCallback);
        Log.d(TAG, "Scanning stopped");
    }

    /**
     * Ta metoda włącza moduł BLE urządzenia mobilnego. Dodatkowo jeśli operacja się powiedzie
     * zmianie ulegają ikony listy menu aplikacji.
     */
    private void initBLEAdapter() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bleAdapter = bluetoothManager.getAdapter();
        if (bleAdapter.isEnabled()) {
            mainMenu.findItem(R.id.enableBle).setTitle(R.string.turnOffBLE);
            mainMenu.findItem(R.id.enableBle).setIcon(R.drawable.ble_icon_off);
            bleOnFlag = true;
        }
//      requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
    }

    private void enableBLE() {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBtIntent);
        bleOnFlag = true;
        mainMenu.findItem(R.id.enableBle).setTitle(R.string.turnOffBLE);
        mainMenu.findItem(R.id.enableBle).setIcon(R.drawable.ble_icon_off);
    }

    private void disableBLE() {
        gattServerDisconnet();
        bleAdapter.disable();
        bleOnFlag = false;
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
        mainMenu = menu;
        if (menu instanceof MenuBuilder) {
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
                if (bleAdapter != null && bleOnFlag)
                    showDialog();
                else
                    Toast.makeText(this, "Włącz BLE!!!", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.clearArea:
                fragment1.clearTextArea();
                return true;
            case R.id.speedTest:
                showSpeedTestDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    /**
     * Ta metoda inicjuje okno dialogowe dla testu przepływnosci.
     */
    private void initSpeedTestDialog() {
        speedTextDialog = new AlertDialog.Builder(this);
        speedTextDialog.setTitle("Ustaw parametry testu:");
        speedTextDialog.setCancelable(false);

        speedTestLayout = getLayoutInflater().inflate(R.layout.speed_test_layout, null);
        currentFramePattern = speedTestLayout.findViewById(R.id.currentFramePattern);
        currentFramePattern.setText(speedTester.getFramePattern());
        isTestOn = speedTestLayout.findViewById(R.id.isTestOn);
        frameField = speedTestLayout.findViewById(R.id.framePattern);
        isTestOn.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                frameField.setEnabled(true);
            else
                frameField.setEnabled(false);
        });

        speedTextDialog.setView(speedTestLayout);

        speedTextDialog.setNegativeButton("Zamknij", (dialog, which) -> dialog.dismiss());

        speedTextDialog.setPositiveButton("Zatwierdź", (dialog, which) -> {
            if (isTestOn.isChecked()) {
                speedTester.setTestOn(true);
                if (!frameField.getText().toString().equals("")) {
                    speedTester.setFramePattern(frameField.getText().toString());
                    currentFramePattern.setText(frameField.getText().toString());
                }
            } else {
                speedTester.setTestOn(false);
            }
            frameField.setText("");
        });
    }

    private void showSpeedTestDialog() {
        if (speedTestLayout.getParent() != null)
            ((ViewGroup) speedTestLayout.getParent()).removeView(speedTestLayout);
        speedTextDialog.show();
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter=new SectionsPageAdapter(getSupportFragmentManager());
        fragment1=new TerminalTab();
        fragment2=new ChartTab();

        adapter.addFragment(fragment1,"TERMINAL");
        adapter.addFragment(fragment2,"WYKRES");


//        adapter.addFragment(new TerminalTab(),"TERMINAL");
//        adapter.addFragment(new ChartTab(),"WYKRES");

//        fragment1= (TerminalTab) adapter.getItem(0);
//        fragment2= (ChartTab) adapter.getItem(1);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionsPageAdapter=new SectionsPageAdapter(getSupportFragmentManager());
        viewPager=findViewById(R.id.container);
        setupViewPager(viewPager);

        TabLayout tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        commons=new Commons();
        t1=new DrawingThread(commons,fragment1,fragment2); //wątek rysujący

        customListAdapter = new BleDeviceArrayAdapter(this, 0, devices);
        bleScanCallback = new BleScanCallback(devices, customListAdapter);
        bleGattCallback = new BleGattCallback(fragment1,commons);

        sendField = findViewById(R.id.sendField);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener((v) -> {
            if (!sendField.getText().toString().equals("") && bleDevice != null && bleGATT != null) {
                bleGattCallback.sendDataToServer(bleGATT, sendField.getText().toString());
            }
        });

        speedTester = new SpeedTester();
        initSpeedTestDialog();
    }

    /**
     * Ta metoda oblicza przepływność a następnie ją wyświetla w oknie ekranu urządzenia
     * mobilnego. Oczywiście jeśli tester jest włączony.
     * @param speedData - dane wymagane go policzenia przepływności.
     */
    public void printSpeedInfo(String speedData) {
        if (speedTester != null && speedTester.isTestOn()) {
            String speed=speedTester.getSpeedOneFrame(speedData);
            Log.d(TAG, "Speed: " + speed);
            fragment1.infoLabelSetText(speed);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gattServerDisconnet();
        turnOffBLE();

        t1.terminate();
        Log.d(TAG,"Clean-up done!");
    }

    /**
     * Ta metoda pokazuje okno dialogowe z widokiem list aktywnie rozgłaszajacych się urządzeń BLE.
     * Początkowo lista jest czyszczona(tak aby rekordy z poprzedniej sesji zniknęły). Następnie
     * rozpoczynany jest proces skanowania. Listę można zamknąć, lub klikając jeden z elementów
     * zainicjować łączenie z nim. Skanowanie w obu przypadkach jest kończone automatycznie.
     */
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
            connectToServer(bleDevice); //połącz się z serwerem
            t1.resume(); //wznów wątek rysujący
        });
        dialog.show();
    }

    /**
     * Ta metoda łączy się ze wskazanym w argumencie urządzeniem BLE. Następnie inicjuje odnajdywanie
     * serwisów i charakterystyk serwera GATT.
     * @param bluetoothDevice - urządzenie z którym nastąpi nawiązanie połączenia
     */
    private void connectToServer(BluetoothDevice bluetoothDevice) {
        bleGATT = bluetoothDevice.connectGatt(this, false, bleGattCallback);
        if (bleGATT == null)
            Toast.makeText(this, "Błąd: nie można się połączyć z urządzeniem!", Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Poprawnie połączono!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Ta metoda rozłącza połączenie z serwerem BLE, jeśli takie istnieje.
     */
    private void gattServerDisconnet() {
        if (bleGATT != null)
            bleGATT.disconnect();
    }

    /**
     * Ta metoda wyłącza moduł BLE urządzenia mobilnego, jeśli został on włączony.
     */
    private void turnOffBLE() {
        if (bleAdapter != null && bleAdapter.isEnabled())
            bleAdapter.disable();
    }
}
