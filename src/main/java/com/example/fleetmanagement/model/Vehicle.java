package com.example.fleetmanagement.model;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String make;

    @Column(nullable = false)
    private String model;

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(name = "production_year")
    private Integer productionYear;

    @OneToMany(mappedBy = "vehicle", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>();

    public Vehicle() {}

    public Vehicle(String make, String model, String registrationNumber, Integer productionYear) {
        this.make = make;
        this.model = model;
        this.registrationNumber = registrationNumber;
        this.productionYear = productionYear;
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
    public Set<Assignment> getAssignments() { return assignments; }
    public void setAssignments(Set<Assignment> assignments) { this.assignments = assignments; }

    @Override
    public String toString() { // Przydatne dla ComboBox
        return make + " " + model + " (" + registrationNumber + ")";
    }
}