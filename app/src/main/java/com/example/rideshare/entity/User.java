package com.example.rideshare.entity;

public class User {
    public String name;
    public String email;
    public String phoneNumber;
    public String password;
    public String profileImageUrl;  // New property

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String name, String email, String phoneNumber, String password, String profileImageUrl) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
    }
}
