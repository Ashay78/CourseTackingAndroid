package com.m2.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();

            List<Course> courses = db.courseDao().getAll();
            List<Position> positions = db.positionDao().getAll();
        }).start();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    public void startCourses(View view) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("courseTracking", Context.MODE_PRIVATE);

        if(sharedPreferences != null) {
            String phoneNumber = sharedPreferences.getString("phoneNumber", "");
            if (phoneNumber == null || phoneNumber.equals("")) {
                Intent intentPhoneNumber = new Intent(this, PhoneNumberActivity.class);
                startActivity(intentPhoneNumber);
            } else {
                Intent intentStartCourse = new Intent(this, StartCourseActivity.class);
                Bundle data = new Bundle();
                data.putString("userId", phoneNumber);
                intentStartCourse.putExtras(data);
                startActivity(intentStartCourse);
            }
        } else {
            Intent intentPhoneNumber = new Intent(this, PhoneNumberActivity.class);
            startActivity(intentPhoneNumber);
        }
    }

    public void historicCourses(View view) {
        Intent intentHistoricCourses = new Intent(this, HistoricCoursesActivity.class);
        startActivity(intentHistoricCourses);
    }

    public void phoneNumber(View view) {
        Intent intentPhoneNumber = new Intent(this, PhoneNumberActivity.class);
        startActivity(intentPhoneNumber);
    }
}