package com.m2.myapplication.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Position {
    @PrimaryKey
    @NonNull
    public String idPosition;

    @NonNull
    public String IdCourse;

    @NonNull
    public Integer latitude;

    @NonNull
    public Integer longitude;

    @NonNull
    public String date;

    public Position(
            @NonNull String idPosition,
            @NonNull String IdCourse,
            @NonNull Integer latitude,
            @NonNull Integer longitude,
            @NonNull String date
    ) {
        this.idPosition = idPosition;
        this.IdCourse = IdCourse;
        this.latitude = latitude;
        this.longitude = longitude;
        this.date = date;
    }

    @Override
    public String toString() {
        return "Position{" +
                "idPosition='" + idPosition + '\'' +
                ", IdCourse='" + IdCourse + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", date='" + date + '\'' +
                '}';
    }
}
