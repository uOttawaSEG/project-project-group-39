package com.example.otams;

public class User {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNum;
    private String userType;

    public User(String firstName, String lastName, String email, String password, String phoneNum, String userType) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNum = phoneNum;
        this.userType = userType;
    }
}
