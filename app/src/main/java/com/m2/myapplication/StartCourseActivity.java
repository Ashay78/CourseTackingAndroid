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
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.text.SimpleDateFormat;
import java.util.UUID;
// TODO Save metre
public class StartCourseActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;

    private TextView textNbSteps;
    private TextView textNbMetre;
    private TextView textDateStart;
    private TextView textSpeed;

    private int initStep = 0;
    private int cptStep = 0;
    private int cptMetre = 0;
    private long dateStart;

    private boolean getPositionAllTime = true;

    private FusedLocationProviderClient fusedLocationClient;

    private Course currentCourse;
    private String userId;

    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_course);

        Bundle b = getIntent().getExtras();
        this.userId   = b.getString("userId");

        this.sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        this.sensorManager.registerListener(this, this.sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER), this.sensorManager.SENSOR_DELAY_GAME);

        this.textNbSteps = findViewById(R.id.text_nb_steps);
        this.textDateStart = findViewById(R.id.text_date_start);
        this.textSpeed = findViewById(R.id.text_speed);
        this.textNbMetre = findViewById(R.id.text_nb_metre);

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH'h'mm");
        this.dateStart = currentTimeMillis();
        this.textDateStart.setText("Heure de début : " + dateFormat.format(this.dateStart));
        this.saveCourse();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(2*5000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    String Lat = String.valueOf(location.getLatitude());
                    String Lon = String.valueOf(location.getLongitude());

                    Toast.makeText(getApplicationContext(), Lat + " - " + Lon, Toast.LENGTH_SHORT).show();

                    textSpeed.setText("" + location.getSpeed());
                    savePosition(location);
                }
            }
        };

        refreshPosition();

    }

    public void refreshPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        fusedLocationClient.requestLocationUpdates(locationRequest,
                locationCallback,
                Looper.getMainLooper());
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
                this.cptMetre = this.cptStep;
                this.textNbSteps.setText("Nombre de pas : " + this.cptStep);
                this.textNbMetre.setText("Nombre de metre : " + Long.parseLong("" + this.cptMetre) + " m");
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
//        this.getAndSavePosition();
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
            this.currentCourse = new Course(UUID.randomUUID().toString(), this.userId, this.cptStep, this.cptMetre, this.dateStart,this.dateStart);
            db.courseDao().insert(this.currentCourse);
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
        }).start();
    }
}
