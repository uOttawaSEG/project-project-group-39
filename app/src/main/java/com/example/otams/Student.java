package com.example.otams;

public class Student extends User{

    private String program;

    public Student(String firstName, String lastName, String email, String password, String phone, String program, boolean isPending, boolean isDenied, boolean isAccepted){
        super(firstName, lastName, email, password, phone, "Student", isPending, isDenied, isAccepted, null);
        this.program = program;
    }
}
