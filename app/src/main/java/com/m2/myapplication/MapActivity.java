package com.m2.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private List<Position> positions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Bundle b = getIntent().getExtras();
        String courseId = b.getString("courseId");

        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();
            db.positionDao().insert(new Position(UUID.randomUUID().toString(), "984217af-1d8f-4a6a-bf02-2ab7c0bac9c7", 0.0,0.0,Long.parseLong("1")));
            this.positions = db.positionDao().getAllByIdCourse(courseId);
        }).start();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng pos;
        if (this.positions.get(0) != null) {
            pos = new LatLng(this.positions.get(0).getLatitude(),this.positions.get(0).getLongitude());
        } else {
            pos = new LatLng(49.4931919,0.1109486);
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 14));
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style));

        PolylineOptions options = new PolylineOptions().width(this.positions.size()).color(Color.RED).geodesic(true);
        for (int i = 0; i < this.positions.size(); i++) {
            LatLng point = new  LatLng(this.positions.get(i).getLatitude(), this.positions.get(i).getLongitude());
            options.add(point);
        }
        mMap.addPolyline(options);
    }

    @Override
    public void onBackPressed(){
        this.finish();
    }
}
