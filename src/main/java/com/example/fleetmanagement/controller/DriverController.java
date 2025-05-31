package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.dao.DriverDao;
import com.example.fleetmanagement.model.Driver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class DriverController {

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, Long> idColumn;
    @FXML private TableColumn<Driver, String> firstNameColumn;
    @FXML private TableColumn<Driver, String> lastNameColumn;
    @FXML private TableColumn<Driver, String> licenseNumberColumn;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField licenseNumberField;

    private DriverDao driverDao;
    private ObservableList<Driver> driverList;

    public void initialize() {
        driverDao = new DriverDao();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        licenseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));

        loadDrivers();

        // Listener do wypełniania pól po zaznaczeniu wiersza
        driverTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue)
        );
    }

    private void loadDrivers() {
        driverList = FXCollections.observableArrayList(driverDao.findAll());
        driverTable.setItems(driverList);
    }

    private void populateFields(Driver driver) {
        if (driver != null) {
            firstNameField.setText(driver.getFirstName());
            lastNameField.setText(driver.getLastName());
            licenseNumberField.setText(driver.getLicenseNumber());
        } else {
            clearFields();
        }
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        licenseNumberField.clear();
        driverTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddDriver() {
        if (validateInput()) {
            Driver newDriver = new Driver(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    licenseNumberField.getText()
            );
            try {
                driverDao.save(newDriver);
                loadDrivers(); // Ponowne załadowanie odświeży listę i pokaże ID
                clearFields();
            } catch (Exception e) { // Łapanie naruszenia UNIQUE dla numeru prawa jazdy
                showError("Błąd dodawania kierowcy", "Nie udało się dodać kierowcy. Sprawdź czy numer prawa jazdy nie jest już zajęty.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditDriver() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć kierowcę do edycji.");
            return;
        }
        if (validateInput()) {
            selectedDriver.setFirstName(firstNameField.getText());
            selectedDriver.setLastName(lastNameField.getText());
            selectedDriver.setLicenseNumber(licenseNumberField.getText());
            try {
                driverDao.update(selectedDriver);
                driverTable.refresh();
                clearFields();
            } catch (Exception e) {
                showError("Błąd edycji kierowcy", "Nie udało się zaktualizować danych kierowcy.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteDriver() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć kierowcę do usunięcia.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć kierowcę: " + selectedDriver.getFirstName() + " " + selectedDriver.getLastName() + "?");
        confirmation.setContentText("Tej operacji nie można cofnąć. Powiązane przypisania mogą zostać zmodyfikowane (ustawienie kierowcy na NULL).");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                driverDao.delete(selectedDriver);
                driverList.remove(selectedDriver);
                clearFields();
            } catch (Exception e) {
                showError("Błąd usuwania kierowcy", "Nie udało się usunąć kierowcy.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadDrivers();
        clearFields();
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "Imię nie może być puste.\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "Nazwisko nie może być puste.\n";
        }
        if (licenseNumberField.getText() == null || licenseNumberField.getText().trim().isEmpty()) {
            errorMessage += "Numer prawa jazdy nie może być pusty.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showAlert("Błąd walidacji", errorMessage);
            return false;
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}