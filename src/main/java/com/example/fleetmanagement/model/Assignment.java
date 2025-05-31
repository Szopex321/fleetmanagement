package com.example.fleetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER) // Eager, aby łatwo wyświetlić w tabeli
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    private String destination;

    public Assignment() {}

    public Assignment(Vehicle vehicle, Driver driver, LocalDate startDate, LocalDate endDate, String destination) {
        this.vehicle = vehicle;
        this.driver = driver;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destination = destination;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getVehicleDisplay() {
        return vehicle != null ? vehicle.toString() : "N/A";
    }
    public String getDriverDisplay() {
        return driver != null ? driver.toString() : "N/A";
    }
}