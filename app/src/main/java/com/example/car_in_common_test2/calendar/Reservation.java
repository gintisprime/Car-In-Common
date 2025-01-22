package com.example.car_in_common_test2.calendar;

public class Reservation {
    private String id; // Firebase key
    private String reason;
    private String startTime;
    private String endTime;
    private String date;
    private boolean isEmergency;
    private boolean releaseTimeCertain;

    // Default constructor required by Firebase
    public Reservation() {
    }

    public Reservation(String id, String reason, String startTime, String endTime, String date, boolean isEmergency, boolean releaseTimeCertain) {
        this.id = id;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.isEmergency = isEmergency;
        this.releaseTimeCertain = releaseTimeCertain;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }

    public boolean isReleaseTimeCertain() {
        return releaseTimeCertain;
    }

    public void setReleaseTimeCertain(boolean releaseTimeCertain) {
        this.releaseTimeCertain = releaseTimeCertain;
    }
}
