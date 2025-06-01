package com.example.fleetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String make;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "registration_number", unique = true, nullable = false, length = 20)
    private String registrationNumber;

    @Column(name = "production_year")
    private Integer productionYear;

    @Column(unique = true, length = 17)
    private String vin;

    @Column(name = "fuel_type", length = 50)
    private String fuelType;

    @Column(length = 50)
    private String status;

    private Integer mileage;

    @Column(name = "last_service_date")
    private LocalDate lastServiceDate;

    @Column(name = "insurance_expiry_date")
    private LocalDate insuranceExpiryDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();

    public Vehicle() {
        this.status = "Dostępny"; // Domyślny status
    }

    public Vehicle(String make, String model, String registrationNumber, Integer productionYear, String vin, String fuelType, String status, Integer mileage, LocalDate purchaseDate, LocalDate lastServiceDate, LocalDate insuranceExpiryDate, String notes) {
        this.make = make;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.productionYear = productionYear;
        this.vin = vin;
        this.fuelType = fuelType;
        this.status = (status != null && !status.isEmpty()) ? status : "Dostępny";
        this.mileage = mileage;
        this.purchaseDate = purchaseDate;
        this.lastServiceDate = lastServiceDate;
        this.insuranceExpiryDate = insuranceExpiryDate;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMake() { return make; }
    public void setMake(String make) { this.make = make; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.registrationNumber = registrationNumber; }
    public Integer getProductionYear() { return productionYear; }
    public void setProductionYear(Integer productionYear) { this.productionYear = productionYear; }
    public String getVin() { return vin; }
    public void setVin(String vin) { this.vin = vin; }
    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getMileage() { return mileage; }
    public void setMileage(Integer mileage) { this.mileage = mileage; }
    public LocalDate getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(LocalDate lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    public LocalDate getInsuranceExpiryDate() { return insuranceExpiryDate; }
    public void setInsuranceExpiryDate(LocalDate insuranceExpiryDate) { this.insuranceExpiryDate = insuranceExpiryDate; }
    public LocalDate getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(LocalDate purchaseDate) { this.purchaseDate = purchaseDate; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Set<Assignment> getAssignments() { return assignments; }
    public void setAssignments(Set<Assignment> assignments) { this.assignments = assignments; }

    @Override
    public String toString() {
        return make + " " + model + " (" + registrationNumber + ")";
    }
}