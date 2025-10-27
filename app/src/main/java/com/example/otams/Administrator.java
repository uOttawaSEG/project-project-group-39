package com.example.otams;

public class Administrator extends User {

    public Administrator(String firstName, String lastName, String email, String password, String phone) {
        super(firstName, lastName, email, password, phone, "Administrator", false, false, true, null);
    }

}

