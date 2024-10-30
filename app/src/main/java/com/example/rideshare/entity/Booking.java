package com.example.rideshare.entity;

import java.io.Serializable;

public class Booking implements Serializable {
    private String bookingId;   // Unique identifier for the booking
    private String rideId;      // The ride associated with the booking
    private String customerId;  // The customer who made the booking
    private String status;      // Status of the booking (e.g., Pending, Confirmed)
    private int bookedSeats;    // Number of seats booked by the customer

    // Default constructor (required for Firebase)
    public Booking() {
    }

    // Parameterized constructor
    public Booking(String bookingId, String rideId, String customerId, String status, int bookedSeats) {
        this.bookingId = bookingId;
        this.rideId = rideId;
        this.customerId = customerId;
        this.status = status;
        this.bookedSeats = bookedSeats;
    }

    // Getter and Setter methods

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
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
