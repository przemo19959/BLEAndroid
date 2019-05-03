package com.example.myapp1;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static com.example.myapp1.MainActivity.TAG;

public class TerminalTab extends Fragment {
    private TextView textArea;
    private TextView speedInfoLabel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.text_tab_layout,container,false);

        textArea=view.findViewById(R.id.textArea);
        textArea.setMovementMethod(new ScrollingMovementMethod()); //dodanie paska

        speedInfoLabel=view.findViewById(R.id.infoLabel);

        return view;
    }

    public void printData(String data){
        Log.d(TAG,"Fragment: "+data);
        getActivity().runOnUiThread(()->{
            Log.d(TAG,"Inside Ui Thread: "+data);
            textArea.append(data);
            Log.d(TAG,"After appending!");
        });
    }
//    public void clearTextArea(){getActivity().runOnUiThread(()->textArea.setText(""));}
    public void clearTextArea(){textArea.setText("");}

    public void infoLabelSetText(String data){getActivity().runOnUiThread(()->speedInfoLabel.setText(data));}
}
