package com.m2.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCourses(View view) {
        Intent intentStartCourse = new Intent(this, StartCourseActivity.class);
        startActivity(intentStartCourse);
    }

    public void historicCourses(View view) {
        Intent intentHistoricCourses = new Intent(this, HistoricCoursesActivity.class);
        startActivity(intentHistoricCourses);
    }
}