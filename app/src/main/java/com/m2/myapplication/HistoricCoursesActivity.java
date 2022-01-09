package com.m2.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.util.ArrayList;
import java.util.List;

public class HistoricCoursesActivity extends AppCompatActivity {

    public List<Course> list;
    ArrayAdapter<Course> adapter;
    ListView courseListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_courses);

        list = new ArrayList<>();
        adapter = new ArrayAdapter<Course>(this, android.R.layout.simple_list_item_1, list);
        courseListView = (ListView) findViewById(R.id.list);
        courseListView.setAdapter(adapter);
        registerForContextMenu(courseListView);

        courseListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView <? > arg0, View view, int position, long id) {
                Intent intentInfo = new Intent(HistoricCoursesActivity.this, InfoCourseActivity.class);
                Bundle data = new Bundle();
                data.putString("courseId", HistoricCoursesActivity.this.list.get(position).getIdCourse());
                intentInfo.putExtras(data);
                startActivity(intentInfo);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getListCourse();
    }

    public void getListCourse() {
        this.list.clear();
        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .build();

            List<Course> exams = db.courseDao().getAll();
            for (Course exam : exams) {
                this.list.add(exam);
            }
        }).start();
    }
}
