package com.example.myapp1;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;
import java.util.List;

public class ChartTab extends Fragment {
    private LineChart mLineChart;
    private List<Entry> yValues;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.chart_tab_layout,container,false);

        mLineChart=view.findViewById(R.id.chart);
        mLineChart.setTouchEnabled(true);
        mLineChart.setPinchZoom(true);

        mLineChart.centerViewTo(0,0, YAxis.AxisDependency.RIGHT);
        mLineChart.getXAxis().setAxisMaximum(200);
        mLineChart.setAutoScaleMinMaxEnabled(true);

        Description description=new Description();
        description.setText("");
        mLineChart.setDescription(description);

        setupChart();

        return view;
    }

    private void setupChart(){
        yValues=new ArrayList<>();
        yValues.add(new Entry(0,0)); //początek układu współrzędnych
        LineDataSet lineDataSet=new LineDataSet(yValues,"Dane");
        lineDataSet.setColor(R.color.orange);
        List<ILineDataSet> dataSets=new ArrayList<>();
        dataSets.add(lineDataSet);
        LineData data=new LineData(dataSets);
        mLineChart.setData(data);
    }

    public void addPointToChart(int xValue,int yValue){
        getActivity().runOnUiThread(()->{
            LineDataSet lineDataSet= (LineDataSet) mLineChart.getData().getDataSetByIndex(0);
            lineDataSet.addEntry(new Entry(xValue,yValue));
            mLineChart.getData().notifyDataChanged();
            mLineChart.notifyDataSetChanged();
        });
    }
}
