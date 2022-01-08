package com.m2.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PositionDao {
    @Query("SELECT * FROM position")
    List<Course> getAll();

    @Query("SELECT * FROM position WHERE idPosition = (:idPosition)")
    Course getById(String idPosition);

    @Query("SELECT * FROM position WHERE idCourse = (:idCourse)")
    List<Course> getAllByIdCourse(String idCourse);

    @Insert
    void insert(Position... positions);

    @Delete
    void delete(Position position);
}
