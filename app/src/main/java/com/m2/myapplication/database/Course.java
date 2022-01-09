package com.m2.myapplication.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.text.SimpleDateFormat;

@Entity
public class Course {
    @PrimaryKey
    @NonNull
    public String idCourse;

    @NonNull
    public String idUser;

    @NonNull
    public Integer nbSteps;

    @NonNull
    public Integer nbMetre;

    @NonNull
    public Long dateStart;

    @NonNull
    public Long dateEnd;

    public Course(
            @NonNull String idCourse,
            @NonNull String idUser,
            @NonNull Integer nbSteps,
            @NonNull Integer nbMetre,
            @NonNull Long dateStart,
            @NonNull Long dateEnd
    ) {
        this.idCourse = idCourse;
        this.idUser = idUser;
        this.nbSteps = nbSteps;
        this.nbMetre = nbMetre;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public void setDateEnd(@NonNull Long dateEnd) {
        this.dateEnd = dateEnd;
    }

    public void setNbMetre(@NonNull Integer nbMetre) {
        this.nbMetre = nbMetre;
    }

    public void setNbSteps(@NonNull Integer nbSteps) {
        this.nbSteps = nbSteps;
    }

    @NonNull
    public String getIdCourse() {
        return idCourse;
    }

    @NonNull
    public Long getDateStart() {
        return dateStart;
    }

    @NonNull
    public Long getDateEnd() {
        return dateEnd;
    }

    @NonNull
    public String getIdUser() {
        return idUser;
    }

    @NonNull
    public Integer getNbMetre() {
        return nbMetre;
    }

    @NonNull
    public Integer getNbSteps() {
        return nbSteps;
    }

    @Override
    public String toString() {
        SimpleDateFormat formater = null;
        formater = new SimpleDateFormat("hh:mm");

        return "User : " + this.idUser + " | (" + formater.format(this.dateStart) + " - " + formater.format(this.dateEnd) + ") | " + this.nbSteps + " pas | " + this.nbMetre + " m";
    }
}
