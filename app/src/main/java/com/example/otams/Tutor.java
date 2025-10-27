package com.example.otams;

public class Tutor extends User{

    private String degree;
    private String courses;


    public Tutor(String firstName, String lastName, String email, String password, String phone, String degree, String courses, boolean isPending, boolean isDenied, boolean isAccepted){
        super(firstName, lastName, email, password, phone, "Tutor", isPending, isDenied, isAccepted, null);
        this.degree = degree;
        this.courses = courses;
    }


}
