package com.project.nearby.models;

public class History {
    String userId;
    String name;
    long timeSpent;
    double distance;
    double ble_distance;
    String timeStamp;

    public History() {
    }

    public History(String userId, String name, long timeSpent, double distance, double ble_distance, String timeStamp) {
        this.userId = userId;
        this.name = name;
        this.timeSpent = timeSpent;
        this.distance = distance;
        this.ble_distance=ble_distance;
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(long timeSpent) {
        this.timeSpent = timeSpent;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getBle_distance() {
        return ble_distance;
    }

    public void setBle_distance(double distance) {
        this.ble_distance = distance;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String  timeStamp) {
        this.timeStamp = timeStamp;
    }
}
