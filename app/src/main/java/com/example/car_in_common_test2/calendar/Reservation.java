package com.example.car_in_common_test2.calendar;

public class Reservation {
    private String reason;
    private String startTime;
    private String endTime;

    private String getDate;

    public Reservation(String reason, String startTime, String endTime) {
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.getDate = getDate;
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
        return getDate;
    }
}