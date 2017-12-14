package com.max_baker.simpleruntracker;
/**
 * Written By: Max Baker
 * Last Modified; 12/7/17
 * Basically a struct
 */

public class Run {
    int minutes;
    int seconds;
    float distance;

    public Run(int minutes, int seconds, float distance) {
        this.minutes = minutes;
        this.seconds = seconds;
        this.distance = distance;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
