package com.example.car_in_common_test2.calendar;

public class Reservation {
    private String reason;
    private String startTime;
    private String endTime;
    private String date;
    private boolean isEmergency;
    private boolean releaseTimeCertain;

    // Required default constructor for Firebase
    public Reservation() {}

    public Reservation(String reason, String startTime, String endTime, String date, boolean isEmergency, boolean releaseTimeCertain) {
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.isEmergency = isEmergency;
        this.releaseTimeCertain = releaseTimeCertain;
    }

    public String getReason() {
        return reason;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getDate() {
        return date;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public boolean isReleaseTimeCertain() {
        return releaseTimeCertain;
    }
}
