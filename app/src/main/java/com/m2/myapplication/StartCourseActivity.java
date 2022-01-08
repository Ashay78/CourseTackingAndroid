package com.m2.myapplication;

import static java.lang.System.currentTimeMillis;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;

public class StartCourseActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private TextView textNbSteps;
    private TextView textDateStart;
    private TextView textSpeed;

    private int initStep = 0;
    private int cptStep = 0;
    private long dateStart;

    private boolean getPositionAllTime = true;

    private FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_course);

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), this.sensorManager.SENSOR_DELAY_GAME);

        this.textNbSteps = findViewById(R.id.text_nb_steps);
        this.textDateStart = findViewById(R.id.text_date_start);
        this.textSpeed = findViewById(R.id.text_speed);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm");
        this.dateStart = currentTimeMillis();
        this.textDateStart.setText("Heure de début : " + dateFormat.format(this.dateStart));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        new Thread(() -> {
            while (this.getPositionAllTime) {
                this.getPosition(location -> {
                    if (location != null) {
                        this.textSpeed.setText("" + location.getSpeed());
                        Log.d("TEST", "" + location.getLatitude());
                        Log.d("TEST", "" + location.getLongitude());
                        Log.d("TEST", "" + location.getSpeed());
                    }
                });
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public void getPosition(OnSuccessListener<Location> locationOnSuccessListener) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, locationOnSuccessListener);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getPositionAllTime = false;
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
