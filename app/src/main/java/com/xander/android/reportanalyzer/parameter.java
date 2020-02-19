package com.xander.android.reportanalyzer;

import java.util.ArrayList;

public class parameter {
    ArrayList<Double> actual=new ArrayList<Double> (  );
    double healthy;
    double max;
    double min;

    public parameter(ArrayList<Double> actual,double healthy,double max,double min)
    {
        this.actual= (ArrayList<Double>) actual.clone();
        this.healthy=healthy;
        this.max=max;
        this.min=min;
    }
}
