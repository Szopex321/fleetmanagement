package com.example.fleetmanagement.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "drivers")
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "license_number", unique = true, nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(unique = true, length = 100)
    private String email;

    @Column(name = "employment_date")
    private LocalDate employmentDate;

    @Column(length = 50)
    private String status;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    @Column(name = "medical_check_expiry_date")
    private LocalDate medicalCheckExpiryDate;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String address;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Assignment> assignments = new HashSet<>(); // Kolekcja przypisań dla tego kierowcy.

    public Driver() {
        this.status = "Aktywny";
    }

    public Driver(String firstName, String lastName, String licenseNumber) {
        this(); // Wywołuje domyślny konstruktor
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
    }

    public Driver(String firstName, String lastName, String licenseNumber, String phoneNumber, String email,
                  LocalDate employmentDate, String status, LocalDate licenseExpiryDate,
                  LocalDate medicalCheckExpiryDate, String address) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.employmentDate = employmentDate;
        this.status = (status != null && !status.isEmpty()) ? status : "Aktywny";
        this.licenseExpiryDate = licenseExpiryDate;
        this.medicalCheckExpiryDate = medicalCheckExpiryDate;
        this.address = address;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getEmploymentDate() { return employmentDate; }
    public void setEmploymentDate(LocalDate employmentDate) { this.employmentDate = employmentDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getLicenseExpiryDate() { return licenseExpiryDate; }
    public void setLicenseExpiryDate(LocalDate licenseExpiryDate) { this.licenseExpiryDate = licenseExpiryDate; }
    public LocalDate getMedicalCheckExpiryDate() { return medicalCheckExpiryDate; }
    public void setMedicalCheckExpiryDate(LocalDate medicalCheckExpiryDate) { this.medicalCheckExpiryDate = medicalCheckExpiryDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Set<Assignment> getAssignments() { return assignments; }
    public void setAssignments(Set<Assignment> assignments) { this.assignments = assignments; }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + licenseNumber + ")";
    }
}