package com.example.car_in_common_test2;

public class Reservation {
    private String id;
    private String type;
    private String date;
    private String reason;
    private String startTime;
    private String endTime;
    private String importance;
    private String releaseCertainty;

    public Reservation(String id, String type, String date, String reason, String startTime, String endTime, String importance, String releaseCertainty) {
        this.id = id;
        this.type = type;
        this.date = date;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.importance = importance;
        this.releaseCertainty = releaseCertainty;
    }

    // Getters and Setters
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
