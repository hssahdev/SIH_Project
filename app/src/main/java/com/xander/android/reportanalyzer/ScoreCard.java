package com.xander.android.reportanalyzer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

public class ScoreCard extends AppCompatActivity {

    Gson gson;
    SharedPreferences preferences;
    List<PatientInfo> reportHistory;
//    ArrayList<Double> values_haem= {5.2, 5.6, 5.4, 6.0, 6.4};
//    ArrayList<Double> values_gluc[] = {102.5, 104.6, 106.8, 105, 105.8};
//    ArrayList<Double> values_bun[] = {6.4, 6.6, 6.7, 6.9 ,7.0};
//    ArrayList<Double> values_creat[] = {.71, .75, .72, .79, .82};
    ArrayList<Double> values_haem= new ArrayList<Double> (  );
    ArrayList<Double> values_gluc = new ArrayList<Double> (  );
    ArrayList<Double> values_bun = new ArrayList<Double> (  );
    ArrayList<Double> values_creat= new ArrayList<Double> (  );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate ( savedInstanceState );
        setContentView ( R.layout.activity_score_card );

        gson = new Gson();
        preferences = getSharedPreferences("pref", Context.MODE_PRIVATE);
        reportHistory = getReportHistory();

        //TODO: reportHistory mai purri list hai reports ki, dynamic bnade accordingly

        for(int i=0;i<reportHistory.size();i++)
        {
            values_haem.add((double)reportHistory.get(i).haemo);
            values_gluc.add((double)reportHistory.get(i).glucose);
            values_bun.add ((double) reportHistory.get(i).bun );
            values_creat.add((double)reportHistory.get(i).creatine);
        }

        parameter haemoglobin = new parameter ( values_haem,5.5, 6.4, 4);
        parameter glucose = new parameter ( values_gluc, 103, 126, 70 );
        parameter BUN = new parameter (values_bun, 6.5, 10, 3 );
        parameter Creatinine = new parameter ( values_creat, 0.9, 1.2, 0.6);

        int size=haemoglobin.actual.size();
        double total = 100*size;
        Log.e("this is healthy",haemoglobin.actual.size()+"");
        for(int i=0;i<haemoglobin.actual.size();i++)
        {
            total -= ((abs(haemoglobin.actual.get(i) - haemoglobin.healthy) *(100/4))/(haemoglobin.max-haemoglobin.min));
            Log.e("this is total 1 ",total+"");
        }
        for(int i=0;i<glucose.actual.size();i++)
        {
            total -= ((abs(glucose.actual.get(i) - glucose.healthy) *(100/4))/(glucose.max-glucose.min));
            Log.e("this is total 2 ",total+"");
        }
        for(int i=0;i<BUN.actual.size();i++)
        {
            total -= ((abs(BUN.actual.get(i) - BUN.healthy) *(100/4))/(BUN.max-BUN.min));
            Log.e("this is total 3 ",total+"");
        }
        for(int i=0;i<Creatinine.actual.size();i++)
        {
            total -= ((abs(Creatinine.actual.get(i) - Creatinine.healthy) *(100/4))/(Creatinine.max-Creatinine.min));
            Log.e("this is total 4 ",total+"");
        }

        int score= (int) (total / size);
        Log.e("score",score+"");
        TextView Score=(TextView) findViewById (R.id.score123);
        TextView Comment=(TextView) findViewById ( R.id.comment);
        ConstraintLayout layout=(ConstraintLayout)findViewById ( R.id.mylayout );
        Score.setText ( score+"" );
        if(score<=35)
        {
            Comment.setText ( "Poor!!!" );
            layout.setBackgroundColor ( Color.parseColor("#FF0000") );

        }
        else if(score<=75)
        {
            Comment.setText ( "Average!!!" );
            layout.setBackgroundColor ( Color.parseColor("#FFD319") );

        }
        else {
            Comment.setText ("Good");
            layout.setBackgroundColor ( Color.parseColor("#90F500") );

        }




    }

    private List<PatientInfo> getReportHistory(){
        Type type = new TypeToken<List<PatientInfo>>() {}.getType();
        List<PatientInfo> patientInfos = gson.fromJson(preferences.getString("patient_history",""),type);
        if (patientInfos==null)
            return new ArrayList<PatientInfo>();
        return patientInfos;
    }
}
