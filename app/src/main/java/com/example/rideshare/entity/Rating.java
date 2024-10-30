package com.example.rideshare.entity;

public class Rating {
    private String userId;
    private float rating;
    private String comment;
    private String rideId;

    public Rating() {
        // Default constructor required for calls to DataSnapshot.getValue(Rating.class)
    }

    public Rating(String userId, float rating, String comment, String rideId) {
        this.userId = userId;
        this.rating = rating;
        this.comment = comment;
        this.rideId = rideId;
    }

    // Getters and setters

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }
}