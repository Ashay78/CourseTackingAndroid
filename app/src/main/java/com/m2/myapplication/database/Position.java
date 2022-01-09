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
    public Double latitude;

    @NonNull
    public Double longitude;

    @NonNull
    public Long date;

    public Position(
            @NonNull String idPosition,
            @NonNull String IdCourse,
            @NonNull Double latitude,
            @NonNull Double longitude,
            @NonNull Long date
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
