package com.m2.myapplication.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Course.class, Position.class}, version = 2)
public abstract class CourseTrackingDB extends RoomDatabase {
    public abstract CourseDao courseDao();
    public abstract PositionDao positionDao();
}

