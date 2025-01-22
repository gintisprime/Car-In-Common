package com.example.car_in_common_test2.calendar;

public class Reservation {
    private String id; // Firebase key
    private String reason;
    private String startTime;
    private String endTime;
    private String date;
    private boolean isEmergency;
    private boolean releaseTimeCertain;

    // Default constructor for Firebase
    public Reservation(String emergencyReservation, String startTime, String endTime, String selectedDate, boolean b, boolean b1) {}

    public Reservation(String id, String reason, String startTime, String endTime, String date, boolean isEmergency, boolean releaseTimeCertain) {
        this.id = id;
        this.reason = reason;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.isEmergency = isEmergency;
        this.releaseTimeCertain = releaseTimeCertain;
    }

    // Getters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReason() { return reason; }
    public String getStartTime() { return startTime; }
    public String getEndTime() { return endTime; }
    public String getDate() { return date; }
    public boolean isEmergency() { return isEmergency; }
    public boolean isReleaseTimeCertain() { return releaseTimeCertain; }
}

