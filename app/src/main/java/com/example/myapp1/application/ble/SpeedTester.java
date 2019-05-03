package com.example.myapp1.application.ble;

import android.util.Log;

import com.example.myapp1.MainActivity;
import com.example.myapp1.R;

/**
 * Ta klasa zapewnia funkcjonalność testowania przepływności transmisji danych BLE.
 * Pozwala na ustalenie wzorca ramek, na jakich będzie liczona przepływność. Test można włączyć
 * lub wyłączyć.
 */
public class SpeedTester {
    private String framePattern = "abcdefghij";
    private boolean testOn = false;
    private long time;
    private StringBuilder buffer = new StringBuilder("");

    public String getFramePattern() {
        return framePattern;
    }

    public void setFramePattern(String framePattern) {
        this.framePattern = framePattern;
    }

    public boolean isTestOn() {
        return testOn;
    }

    public void setTestOn(boolean testOn) {
        this.testOn = testOn;
        Log.d(MainActivity.TAG, "Stan testowania: " + testOn);
    }

    public String getSpeedOneFrame(String data) {
        if (testOn) {
            buffer.append(data); //dodaj znaki do bufora
            String currentFrame = buffer.toString();
            Log.d(MainActivity.TAG,"Frame: ["+currentFrame+"]");
            if (currentFrame.contains(framePattern)) { //czy wykryto ramkę
                if (time == 0) {
                    time = System.currentTimeMillis();
                    buffer = buffer.delete(0, currentFrame.indexOf(framePattern)+framePattern.length());
                } else {
                    double dataSpeed = ((framePattern.length() * 8e3) / (System.currentTimeMillis() - time));
                    time = System.currentTimeMillis(); //nadpisz czas to
                    buffer = buffer.delete(0, currentFrame.indexOf(framePattern)+framePattern.length());
                    return String.format("Przepływność: %07.1f [bit/s], Buffer size: %04d", dataSpeed,buffer.length());
                }
            } else
                return "";
        }
        return R.string.speedTestIsOff + "";
    }
}
