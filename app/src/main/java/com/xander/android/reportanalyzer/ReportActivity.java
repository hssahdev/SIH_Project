package com.xander.android.reportanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    Gson gson;
    SharedPreferences preferences;
    List<PatientInfo> reportHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        gson = new Gson();
        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        reportHistory = getReportHistory();


        LineChart lineChart = findViewById(R.id.lineChart);
        LineChart lineChart2 = findViewById(R.id.lineChart2);
        LineChart lineChart3 = findViewById(R.id.lineChart3);
        LineChart lineChart4 = findViewById(R.id.lineChart4);

        List<Entry> entries = new ArrayList<Entry>();
        List<Entry> entries2 = new ArrayList<Entry>();
        List<Entry> entries3 = new ArrayList<Entry>();
        List<Entry> entries4 = new ArrayList<Entry>();



        for(int i=0;i<reportHistory.size();i++){
            PatientInfo info = reportHistory.get(i);
            entries.add(new Entry(i+1,info.haemo));
            entries2.add(new Entry(i+1,info.glucose));
            entries3.add(new Entry(i+1,info.bun));
            entries4.add(new Entry(i+1,info.creatine));
        }


//        final String[] quarters = new String[] { "Q1", "Report 1", "Report 2", "Report 3","Report 4" , "Report 5"};
        ValueFormatter formatter = new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return "Report "+(int)value;
            }
        };

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        xAxis = lineChart2.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        xAxis = lineChart3.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);

        xAxis = lineChart4.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);



        LineDataSet dataSet = new LineDataSet(entries,"Glycosylated Haemoglobin");
        dataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet.setCircleColor(Color.MAGENTA);
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh

        LineDataSet dataSet2 = new LineDataSet(entries2,"Average Glucose");
        dataSet2.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet2.setCircleColor(Color.GREEN);
        LineData lineData2 = new LineData(dataSet2);
        lineChart2.setData(lineData2);
        lineChart2.invalidate(); // refresh

        LineDataSet dataSet3 = new LineDataSet(entries3,"BUN");
        dataSet3.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        LineData lineData3 = new LineData(dataSet3);
        dataSet3.setCircleColor(Color.RED);
        lineChart3.setData(lineData3);
        lineChart3.invalidate(); // refresh

        LineDataSet dataSet4 = new LineDataSet(entries4,"Creatinine");
        dataSet4.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        dataSet4.setCircleColor(Color.YELLOW);
        LineData lineData4 = new LineData(dataSet4);
        lineChart4.setData(lineData4);
        lineChart4.invalidate(); // refresh
//
    }

    private List<PatientInfo> getReportHistory(){
        Type type = new TypeToken<List<PatientInfo>>() {}.getType();
        List<PatientInfo> patientInfos = gson.fromJson(preferences.getString("patient_history",""),type);
        if (patientInfos==null)
            return new ArrayList<PatientInfo>();
        return patientInfos;
    }
}
