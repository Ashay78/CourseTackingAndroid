package com.m2.myapplication;

import static java.lang.System.currentTimeMillis;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DemandeSuiviActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_SMS = 1;

    private String courseId = UUID.randomUUID().toString();
    private String phoneNo = "";
    private boolean accepted = false;
    List<Position> positions = new ArrayList<>();

    private double lastLat;
    private double lastLon;
    private int last = 0;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demande_suivi);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @SuppressLint("SetTextI18n")
    public void SendSMS(View view){
        EditText mEdit = (EditText)findViewById(R.id.edit_phone_number);
        mEdit.setEnabled(false);
        this.phoneNo = mEdit.getText().toString();

        Button mButton = (Button)findViewById(R.id.button);
        mButton.setEnabled(false);

        // Send sms to the destination
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.SEND_SMS) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_SMS)) {
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, "2 IDENTIFY IN", null, null);

                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SEND_SMS, Manifest.permission.READ_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);
            }
        } else {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, "2 IDENTIFY IN", null, null);

            Toast.makeText(getApplicationContext(), "SMS sent.",
                    Toast.LENGTH_LONG).show();
        }

        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);

                    Cursor cursor = getContentResolver().query(Uri.parse("content://sms"), null, null, null, null);
                    if (cursor.moveToFirst()) { // must check the result to prevent exception
                        do {
                            String date = cursor.getString(4);
                            String body = cursor.getString(12);
                            String sender = cursor.getString(2);

                            if (!sender.equals(phoneNo)) {
                                continue; // not the right sms
                            }

                            CourseTrackingDB db = Room
                                    .databaseBuilder(
                                            getApplicationContext(),
                                            CourseTrackingDB.class,
                                            "courseTracking")
                                    .fallbackToDestructiveMigration()
                                    .build();

                            if (body.startsWith("2 IDENTIFY OUT ") && !accepted) {
                                String userId = body.substring(15);
                                long start = System.currentTimeMillis();
                                courseId = UUID.randomUUID().toString();
                                db.courseDao().insert(new Course(courseId, userId, 0, 0, start, start));

                                System.out.println("New user: " + userId);
                                accepted = true;
                                break; // fuck other sms
                            } else if (body.startsWith("3 POS ")) {
                                String things = body.substring(6);
                                double lat = Double.parseDouble(things.split(" ")[0]);
                                double lon = Double.parseDouble(things.split(" ")[1]);

                                if (lat == lastLat && lon == lastLon) {
                                    break; // already inserted
                                }

                                lastLat = lat;
                                lastLon = lon;

                                long currDate = System.currentTimeMillis();
                                Position curr = new Position(UUID.randomUUID().toString(), courseId, lat, lon, currDate);
                                db.positionDao().insert(curr);
                                positions.add(curr);
                                System.out.println("Received pos: " + lat + " " + lon);

                                Handler mainHandler = new Handler(this.getMainLooper());

                                Runnable myRunnable = new Runnable() {
                                    @Override
                                    public void run() {
                                        LatLng pos = new LatLng(lastLat, lastLon);
                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
                                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(DemandeSuiviActivity.this, R.raw.map_style));

                                        PolylineOptions options = new PolylineOptions().width(10).color(Color.RED).geodesic(true);
                                        for (int i = last; i < positions.size(); i++) {
                                            LatLng point = new  LatLng(positions.get(i).getLatitude(), positions.get(i).getLongitude());
                                            options.add(point);
                                        }
                                        last = positions.size() - 1;
                                        if (last <= 0) {
                                            last = 0;
                                        }
                                        mMap.addPolyline(options);
                                    } // This is your code
                                };
                                mainHandler.post(myRunnable);
                                break; // fuck other sms
                            }


                        } while (cursor.moveToNext());
                    }
                } catch(SecurityException ex) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.READ_SMS},
                            MY_PERMISSIONS_REQUEST_READ_SMS);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        // Wait for response
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNo, null, "2 IDENTIFY IN", null, null);

                Toast.makeText(getApplicationContext(), "SMS sent.",
                        Toast.LENGTH_LONG).show();
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

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }
}