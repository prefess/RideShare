package com.example.rideshare.entity;

public class Vehicle {
    private String vehicleId;
    private String vehicleRegNumber;
    private String vehicleName;
    private String vehicleType;
    private String vehicleColour;
    private String seatsOffering;
    private String vehicleImageUrl;
    private String insuranceImageUrl;

    private String status;

    public Vehicle() {
        // Default constructor required for calls to DataSnapshot.getValue(Vehicle.class)
    }

    public Vehicle(String vehicleId, String vehicleRegNumber, String vehicleName, String vehicleType, String vehicleColour, String seatsOffering, String vehicleImageUrl, String insuranceImageUrl, String status) {
        this.vehicleId = vehicleId;
        this.vehicleRegNumber = vehicleRegNumber;
        this.vehicleName = vehicleName;
        this.vehicleType = vehicleType;
        this.vehicleColour = vehicleColour;
        this.seatsOffering = seatsOffering;
        this.vehicleImageUrl = vehicleImageUrl;
        this.insuranceImageUrl = insuranceImageUrl;
        this.status = status;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleName() {
        return vehicleName;
    }

    public void setVehicleName(String vehicleName) {
        this.vehicleName = vehicleName;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleColour() {
        return vehicleColour;
    }

    public void setVehicleColour(String vehicleColour) {
        this.vehicleColour = vehicleColour;
    }

    public String getSeatsOffering() {
        return seatsOffering;
    }

    public void setSeatsOffering(String seatsOffering) {
        this.seatsOffering = seatsOffering;
    }

    public String getVehicleImageUrl() {
        return vehicleImageUrl;
    }

    public void setVehicleImageUrl(String vehicleImageUrl) {
        this.vehicleImageUrl = vehicleImageUrl;
    }

    public String getInsuranceImageUrl() {
        return insuranceImageUrl;
    }

    public void setInsuranceImageUrl(String insuranceImageUrl) {
        this.insuranceImageUrl = insuranceImageUrl;
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}