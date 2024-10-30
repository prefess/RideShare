package com.example.rideshare.entity;

public class Request
{
    private String requestId;
    private String rideId;
    private String customerId;
    private String status;
    private int bookedSeats; // Number of seats booked

    public Request() {
    }

    public Request(String requestId, String customerId, String rideId, String status, int bookedSeats) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.rideId = rideId;
        this.status = status;
        this.bookedSeats = bookedSeats;
    }


    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }
}