package com.example.myapp1.application.ble;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapp1.R;

import java.util.List;

/**
 * Ta klasa stanowi adapter, dzięki któremu możliwe jest wyświetlenie listy urządzeń (klasy BleDevice)
 * w widoku listowym ListView.
 */
public class BleDeviceArrayAdapter extends ArrayAdapter<BleDevice> {

    private Context context;
    private List<BleDevice> devices;

    public BleDeviceArrayAdapter(Context context, int resource, List<BleDevice> devices) {
        super(context, resource, devices);

        this.context = context;
        this.devices = devices;
    }

    @Override
    public BleDevice getItem(int position) {
        return devices.get(position);
    }

    /**
     * Ta metoda jest wywoływana za każdym razem, gdy rysowana jest lista. Pobierany jest obiekt
     * klasy BleDevice. Dla pól w widoku dodawane są odpowiednie pola z tego obiektu, tak, że
     * użytkownik widzi dane urządzenia na ekranie.
     * @param position - pozycja widoku listy
     * @param convertView
     * @param parent
     * @return widok z danymi urządzenia
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        BleDevice device = devices.get(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.ble_device_layout, null);

        TextView name = (TextView)view.findViewById(R.id.deviceName);
        TextView rssi = (TextView)view.findViewById(R.id.rssi);
        TextView address = (TextView)view.findViewById(R.id.address);

        if(device.getName()==null)
            name.setText("Nazwa: Nieznana");
        else
            name.setText("Nazwa: "+device.getName());

        rssi.setText("RSSI: "+device.getRssi()+"[dB]");
        address.setText("Adres: "+device.getAddress());
        return view;
    }
}
