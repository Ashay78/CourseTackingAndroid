package com.m2.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.m2.myapplication.database.Course;
import com.m2.myapplication.database.CourseDao;
import com.m2.myapplication.database.CourseTrackingDB;
import com.m2.myapplication.database.Position;

import java.text.DateFormat;
import java.util.List;

public class InfoCourseActivity extends AppCompatActivity {

    private Course course;

    private TextView textUser;
    private TextView textDateStart;
    private TextView textDateEnd;
    private TextView textNbStep;
    private TextView textNbMetre;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info_course);

        Bundle b = getIntent().getExtras();
        String courseId   = b.getString("courseId");

        this.textUser = findViewById(R.id.text_info_user);
        this.textDateStart = findViewById(R.id.text_info_date_start);
        this.textDateEnd = findViewById(R.id.text_info_date_end);
        this.textNbStep = findViewById(R.id.text_info_nb_steps);
        this.textNbMetre = findViewById(R.id.text_info_nb_meter);

        new Thread(() -> {
            CourseTrackingDB db = Room
                    .databaseBuilder(
                            getApplicationContext(),
                            CourseTrackingDB.class,
                            "courseTracking")
                    .fallbackToDestructiveMigration()
                    .build();

            course = db.courseDao().getById(courseId);

            DateFormat mediumDateFormat = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM,
                    DateFormat.MEDIUM);

            this.textDateStart.setText("Date de début :  " + mediumDateFormat.format(this.course.dateStart));
            this.textDateEnd.setText("Date de début :  " + mediumDateFormat.format(this.course.dateEnd));
            this.textUser.setText("User : " + this.course.getIdUser());
            this.textNbStep.setText("Nombre de pas : " + this.course.getNbSteps() + " pas");
            this.textNbMetre.setText("Nombre de metre : " + this.course.getNbMetre() + " m");
        }).start();
    }

    public void SeeMap(View view) {
        Intent mapIntent = new Intent(this, MapActivity.class);
        Bundle data = new Bundle();
        data.putString("courseId", this.course.getIdCourse());
        mapIntent.putExtras(data);
        startActivity(mapIntent);
    }
}
