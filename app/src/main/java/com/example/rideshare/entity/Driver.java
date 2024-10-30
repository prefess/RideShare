package com.example.rideshare.entity;

public class Driver {
    private String driverId;
    private String name;
    private String email;
    private String phoneNumber;
    private String profilePicture;
    private String driverLicensePicture;
    private boolean verified;
    private int totalRatings;
    private float totalRatingScore;
    private float averageRating;

    public Driver() {
        // Default constructor required for calls to DataSnapshot.getValue(Driver.class)
    }

    public Driver(String name, String email, String phoneNumber, String profilePicture, String driverLicensePicture, boolean verified, int totalRatings) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.profilePicture = profilePicture;
        this.driverLicensePicture = driverLicensePicture;
        this.verified = verified;
        this.totalRatings = totalRatings;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public String getDriverLicensePicture() { return driverLicensePicture; }
    public void setDriverLicensePicture(String driverLicensePicture) { this.driverLicensePicture = driverLicensePicture; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public int getTotalRatings() {
        return totalRatings;
    }

    public void setTotalRatings(int totalRatings) {
        this.totalRatings = totalRatings;
    }

    public float getTotalRatingScore() {
        return totalRatingScore;
    }

    public void setTotalRatingScore(float totalRatingScore) {
        this.totalRatingScore = totalRatingScore;
    }

    public float getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(float averageRating) {
        this.averageRating = averageRating;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
}
