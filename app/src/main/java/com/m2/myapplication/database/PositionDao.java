package com.m2.myapplication.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PositionDao {
    @Query("SELECT * FROM position")
    List<Position> getAll();

    @Query("SELECT * FROM position WHERE idPosition = (:idPosition)")
    Position getById(String idPosition);

    @Query("SELECT * FROM position WHERE idCourse = (:idCourse)")
    List<Position> getAllByIdCourse(String idCourse);

    @Insert
    void insert(Position... positions);

    @Delete
    void delete(Position position);
}
