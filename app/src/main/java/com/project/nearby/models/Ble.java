package com.project.nearby.models;

public class Ble {

    int rssi;
    String timestamp;
    String func;

    public Ble(){}

    public Ble(int distance, String timestamp, String func) {
        this.rssi = distance;
        this.timestamp = timestamp;
        this.func = func;
    }

    public int getRssi() {
        return rssi;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getFunc() {
        return func;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setFunc(String func) {
        this.func = func;
    }
}
