package com.example.otams;

import com.google.firebase.firestore.DocumentId;

import java.util.Date;

public class SessionRequest {

    private String requestID;
    private String slotID;
    private String tutorUID;
    private String studentUID;
    private String studentName;
    private String bookingStatus;
    private Long requestTimeStamp;

    public SessionRequest(){}

    public SessionRequest(String slotId, String tutorId, String studentId, String studentName) {
        this.slotID = slotID;
        this.tutorUID = tutorUID;
        this.studentUID = studentUID;
        this.studentName = studentName;
        this.bookingStatus = "pending";
        this.requestTimeStamp = new Date().getTime();

    }


    public String getRequestID() {
        return requestID;
    }

    public String getSlotID() {
        return slotID;
    }

    public String getTutorUID() {
        return tutorUID;
    }

    public String getStudentUID() {
        return studentUID;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public Long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public void setSlotID(String slotID) {
        this.slotID = slotID;
    }

    public void setTutorUID(String tutorUID) {
        this.tutorUID = tutorUID;
    }

    public void setStudentUID(String studentUID) {
        this.studentUID = studentUID;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public void setRequestTimeStamp(Long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }


}
