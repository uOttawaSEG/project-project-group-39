package com.example.otams;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNum;
    private String userType;
    private boolean isPending;
    private boolean isDenied;
    private boolean isAccepted;
    private String fcmToken;

    public User(String firstName, String lastName, String email, String password, String phoneNum, String userType, boolean isPending, boolean isDenied, boolean isAccepted, String fcmToken) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNum = phoneNum;
        this.userType = userType;
        this.isPending = isPending;
        this.isDenied = isDenied;
        this.isAccepted = isAccepted;
        this.fcmToken = null;
    }
    public String getFcmToken(){
        return fcmToken;
    }
    public void setFcmToken(String fcmToken){
        this.fcmToken = fcmToken;
    }
}
