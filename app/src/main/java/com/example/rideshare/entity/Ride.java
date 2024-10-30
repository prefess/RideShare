package com.example.rideshare.entity;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Ride implements Serializable {
    private String rideId;
    private String date;
    private String destination;
    private double destinationLatitude;
    private double destinationLongitude;
    private String driverId;
    private String driverName;

    private String origin;
    private double originLatitude;
    private double originLongitude;
    private long price;
    private Map<String, Booking> bookings; // Map of requestId to Request object
    private int seatsAvailable;
    private String vehicleId;
    private List<Stop> stops;
    private boolean isComplete;
    private String status; // New field to track the status of the ride

    // Empty constructor for Firebase
    public Ride() {}

    public Ride(String origin, double originLatitude, double originLongitude,
                String destination, double destinationLatitude, double destinationLongitude,
                String driverId, long price, int seatsAvailable, List<Stop> stops,
                String vehicleId, String date, String status) {
        this.date = date;
        this.destination = destination;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.driverId = driverId;
        this.origin = origin;
        this.originLatitude = originLatitude;
        this.originLongitude = originLongitude;
        this.price = price;
        this.seatsAvailable = seatsAvailable;
        this.stops = stops;
        this.vehicleId = vehicleId;
        this.status = status;
    }

    public Ride(String origin, String destination, String driverId, long price, int seatsAvailable, List<Stop> stops, String vehicleId, String date) {
        this.date = date;
        this.destination = destination;
        this.driverId = driverId;
        this.origin = origin;
        this.price = price;
        this.seatsAvailable = seatsAvailable;
        this.stops = stops;
        this.vehicleId = vehicleId;
    }

    // Constructor without requests map
    public Ride(String rideId, String origin, String destination, String driverId, long price, int seatsAvailable, List<Stop> stops, String vehicleId, String date) {
        this.rideId = rideId;
        this.date = date;
        this.destination = destination;
        this.driverId = driverId;
        this.origin = origin;
        this.price = price;
        this.seatsAvailable = seatsAvailable;
        this.stops = stops;
        this.vehicleId = vehicleId;
    }

    // Full constructor
    public Ride(String origin, String destination, String driverId, long price, Map<String, Booking> bookings, int seatsAvailable, List<Stop> stops, String vehicleId) {
        this.destination = destination;
        this.driverId = driverId;
        this.origin = origin;
        this.price = price;
        this.bookings = bookings;
        this.seatsAvailable = seatsAvailable;
        this.stops = stops;
        this.vehicleId = vehicleId;
    }

    // Getters and Setters

    public String getRideId() {
        return rideId;
    }

    public void setRideId(String rideId) {
        this.rideId = rideId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDestination() {
        return destination;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDriverId() {
        return driverId;
    }

    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public double getOriginLatitude() {
        return originLatitude;
    }

    public void setOriginLatitude(double originLatitude) {
        this.originLatitude = originLatitude;
    }

    public double getOriginLongitude() {
        return originLongitude;
    }

    public void setOriginLongitude(double originLongitude) {
        this.originLongitude = originLongitude;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public Map<String, Booking> getRequests() {
        return bookings;
    }

    public void setRequests(Map<String, Booking> bookings) {
        this.bookings = bookings;
    }

    public int getSeatsAvailable() {
        return seatsAvailable;
    }

    public void setSeatsAvailable(int seatsAvailable) {
        this.seatsAvailable = seatsAvailable;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    // Getters and setters
    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Nested Stop class to store each stop's address and coordinates
    public static class Stop implements Serializable {
        private String address;
        private double latitude;
        private double longitude;

        public Stop() {}

        public Stop(String address, double latitude, double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        // Getters and Setters

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
    }

}
