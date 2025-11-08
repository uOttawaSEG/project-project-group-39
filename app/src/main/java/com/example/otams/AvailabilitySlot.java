package com.example.otams;

import com.google.firebase.firestore.DocumentId;
public class AvailabilitySlot {
    private String slotID;
    private String tutorUID;
    private String date;
    private long startTime;
    private long endTime;
    private boolean manualApproval;
    private String bookingStatus;

    public AvailabilitySlot(){}

    public AvailabilitySlot(String tutorUID, long startTime, long endTime, boolean manualApproval){
        this.tutorUID = tutorUID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.manualApproval = manualApproval;
        this.bookingStatus = "available";

    }


    public String getTutorUID() {
        return tutorUID;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public boolean manualApproval() {
        return manualApproval;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setTutorUid(String tutorUid) {
        this.tutorUID = tutorUID;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public void setManualApproval(boolean manualApproval) {
        this.manualApproval = manualApproval;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }



}
