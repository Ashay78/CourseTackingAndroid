package com.m2.myapplication;

import static java.lang.System.currentTimeMillis;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class StartCourseActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private TextView textNbSteps;
    private TextView textDateStart;
    private TextView textSpeed;

    private int initStep = 0;
    private int cptStep = 0;
    private int cptMetre = 0;
    private long dateStart;

    private boolean getPositionAllTime = true;

    private FusedLocationProviderClient fusedLocationClient;

    private Course currentCourse;


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

        this.saveCourse();

        new Thread(() -> {
            while (this.getPositionAllTime) {
                this.getAndSavePosition();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();


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
        this.initStopCourse();
        super.onDestroy();
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
        this.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    public void initStopCourse() {
        this.updateCourse();
        this.getAndSavePosition();
        this.getPositionAllTime = false;
    }

    public void getAndSavePosition() {
        this.getPosition(location -> {
            if (location != null) {
                this.textSpeed.setText("" + location.getSpeed());
                this.savePosition(location);
            }
        });
    }

    public void saveCourse() {
        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();
            this.currentCourse = new Course(UUID.randomUUID().toString(), "1", this.cptStep, this.cptMetre, this.dateStart,this.dateStart);
            db.courseDao().insert(this.currentCourse);
            Log.d("TEST", "Course save");
        }).start();
    }


    public void savePosition(Location location) {
        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();
            db.positionDao().insert(new Position(UUID.randomUUID().toString(), this.currentCourse.getIdCourse(), location.getLatitude(), location.getLongitude(), currentTimeMillis()));
            Log.d("TEST", "Position save");
        }).start();
    }

    public void updateCourse() {
        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();

            this.currentCourse.setDateEnd(currentTimeMillis());
            this.currentCourse.setNbMetre(this.cptMetre);
            this.currentCourse.setNbSteps(this.cptStep);

            db.courseDao().update(this.currentCourse);
            Log.d("TEST", "Course update");
        }).start();
    }
}
