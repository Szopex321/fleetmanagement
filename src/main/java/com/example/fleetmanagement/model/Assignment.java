package com.example.fleetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(length = 255)
    private String destination;

    @Column(length = 255)
    private String purpose;

    @Column(name = "start_mileage")
    private Integer startMileage;

    @Column(name = "end_mileage")
    private Integer endMileage;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 50)
    private String status;

    @Column(name = "creation_date", updatable = false, columnDefinition = "TIMESTAMP WITH TIME ZONE")
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime creationDate;


    public Assignment() {
        this.status = "Zaplanowane";
    }

    public Assignment(Vehicle vehicle, Driver driver, LocalDate startDate, LocalDate endDate, String destination,
                      String purpose, Integer startMileage, Integer endMileage, String notes, String status) {
        this.vehicle = vehicle;
        this.driver = driver;
        this.startDate = startDate;
        this.endDate = endDate;
        this.destination = destination;
        this.purpose = purpose;
        this.startMileage = startMileage;
        this.endMileage = endMileage;
        this.notes = notes;
        this.status = (status != null && !status.isEmpty()) ? status : "Zaplanowane";
        // creationDate jest ustawiane przez bazę danych lub @PrePersist
    }

    // Adnotacja @PrePersist: Metoda oznaczona tą adnotacją zostanie automatycznie wywołana
    // przez Hibernate tuż przed zapisaniem nowego obiektu do bazy danych (przed operacją persist).
    @PrePersist // Ustawienie daty utworzenia przed zapisem do bazy
    protected void onCreate() {
        creationDate = LocalDateTime.now();
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

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }
    public Integer getStartMileage() { return startMileage; }
    public void setStartMileage(Integer startMileage) { this.startMileage = startMileage; }
    public Integer getEndMileage() { return endMileage; }
    public void setEndMileage(Integer endMileage) { this.endMileage = endMileage; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }


    // Metody pomocnicze dla TableView (wyświetlanie obiektów Vehicle i Driver)
    public String getVehicleDisplay() {
        return vehicle != null ? vehicle.toString() : "BRAK";// Zwraca tekstową reprezentację pojazdu lub "BRAK", jeśli pojazd nie jest przypisany.
    }
    public String getDriverDisplay() {
        return driver != null ? driver.toString() : "BRAK";
    }
}