package com.example.car_in_common_test2;

public class Reservation {
    private String reason;
    private String startTime;
    private String endTime;
    private String date;
    private boolean isImportant;
    private boolean isEmergency;

    public Reservation() {
        this.reason = "";
        this.startTime = "";
        this.endTime = "";
        this.date = "";
        this.isImportant = false;
        this.isEmergency = false;
    }

    public Reservation(String reason, String startTime, String endTime, String date, boolean isImportant, boolean isEmergency) {
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.isImportant = isImportant;
        this.isEmergency = isEmergency;
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

    public boolean isImportant() {
        return isImportant;
    }

    public void setImportant(boolean important) {
        isImportant = important;
    }

    public boolean isEmergency() {
        return isEmergency;
    }

    public void setEmergency(boolean emergency) {
        isEmergency = emergency;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "reason='" + reason + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", date='" + date + '\'' +
                ", isImportant=" + isImportant +
                ", isEmergency=" + isEmergency +
                '}';
    }
}
