package com.m2.myapplication;

import static java.lang.System.currentTimeMillis;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;

    private SensorManager sensorManager;

    private TextView textNbSteps;
    private TextView textNbMetre;
    private TextView textDateStart;
    private TextView textSpeed;

    private String currentSavedLocation;

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

                    currentSavedLocation = Lat + " " + Lon;

                    textSpeed.setText("" + location.getSpeed());
                    savePosition(location);
                }
            }
        };

        refreshPosition();

        // Send sms to the destination
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        }

        new Thread(() -> {
            boolean stopThread = false;
            while (!stopThread) {
                try {
                    Thread.sleep(1000);

                    Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
                    if (cursor.moveToFirst()) { // must check the result to prevent exception
                        do {
                            String date = cursor.getString(4);
                            String body = cursor.getString(12);
                            String sender = cursor.getString(2);

                            if (body.startsWith("2 IDENTIFY IN")) {
                                System.out.println("Received IN from " + sender);

                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(sender, null, "2 IDENTIFY OUT " + this.userId, null, null);

                                new Thread(() -> {
                                    while (true) {
                                        try {
                                            Thread.sleep(10000);
                                            smsManager.sendTextMessage(sender, null, "3 POS " + this.currentSavedLocation, null, null);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }).start();

                                stopThread = true;
                                break; // fuck other sms
                            }
                        } while (cursor.moveToNext());
                    }
                } catch(SecurityException ex) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_SMS},
                            MY_PERMISSIONS_REQUEST_READ_SMS);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
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

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == MY_PERMISSIONS_REQUEST_READ_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getApplicationContext(),
                        "SMS faild, please try again.", Toast.LENGTH_LONG).show();
            }
        }

    }
}
