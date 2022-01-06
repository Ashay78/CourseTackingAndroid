package com.m2.myapplication;

import static java.lang.System.currentTimeMillis;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;

public class StartCourseActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private TextView textNbSteps;
    private TextView textDateStart;

    private int initStep = 0;
    private int cptStep = 0;
    private long dateStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_course);

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.sensorManager.registerListener( this , this.sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER ) , this.sensorManager.SENSOR_DELAY_GAME);

        this.textNbSteps = findViewById(R.id.text_nb_steps);
        this.textDateStart = findViewById(R.id.text_date_start);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm");
        this.dateStart = currentTimeMillis();
        this.textDateStart.setText("Heure de début : " + dateFormat.format(this.dateStart));
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.sensorManager.registerListener( this , this.sensorManager.getDefaultSensor( Sensor.TYPE_STEP_COUNTER ) , this.sensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_STEP_COUNTER:
                if (this.initStep == 0) {
                    this.initStep = (int) sensorEvent.values[0];
                }
                this.cptStep = (int) sensorEvent.values[0] - this.initStep;
                this.textNbSteps.setText("Nombre de pas : " + this.cptStep);
                break;
            default:
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void stopCourse(View view) {
        this.saveCourse();
        this.finish();
    }

    @Override
    public void onBackPressed() {
        this.saveCourse();
        super.onBackPressed();
    }

    public void saveCourse() {
        SharedPreferences sharedPreferences= this.getSharedPreferences("courses", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong("dateStart", this.dateStart);
        editor.putLong("dateFinish", currentTimeMillis());
        editor.putInt("nbSteps", this.cptStep);
        editor.apply();

        Toast.makeText(this,"save",Toast.LENGTH_LONG).show();
    }
}
