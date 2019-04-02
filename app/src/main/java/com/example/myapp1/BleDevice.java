package com.example.myapp1;

import android.bluetooth.BluetoothDevice;

public class BleDevice {
    private String name;
    private int rssi;
    private String address;
    private BluetoothDevice deviceHandler;

    public BleDevice(String name, int rssi, String address) {
        this.name = name;
        this.rssi = rssi;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public BluetoothDevice getDeviceHandler() {
        return deviceHandler;
    }

    public void setDeviceHandler(BluetoothDevice deviceHandler) {
        this.deviceHandler = deviceHandler;
    }

    @Override
    public String toString() {
        return "BleDevice{" +
                "name='" + name + '\'' +
                ", rssi=" + rssi +
                ", address='" + address + '\'' +
                '}';
    }
}
