package com.m2.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CourseDao {
    @Query("SELECT * FROM course")
    List<Course> getAll();

    @Query("SELECT * FROM course WHERE idCourse = (:idCourse)")
    Course getById(String idCourse);

    @Query("SELECT * FROM course WHERE idUser = (:idUser)")
    List<Course> getAllByIdUser(String idUser);

    @Insert
    void insert(Course... courses);

    @Delete
    void delete(Course course);

    @Update
    void update(Course course);
}
