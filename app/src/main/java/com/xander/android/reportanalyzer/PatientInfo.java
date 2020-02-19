package com.xander.android.reportanalyzer;

public class PatientInfo {
    float haemo;
    float glucose;
    float bun;
    float creatine;

    public PatientInfo(float []vals) {
        this.haemo = vals[0];
        this.glucose = vals[1];
        this.bun = vals[2];
        this.creatine = vals[3];
    }
}
