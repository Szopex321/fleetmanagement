package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.dao.VehicleDao;
import com.example.fleetmanagement.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.Optional;

public class VehicleController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, Long> idColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, String> regNumberColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;

    @FXML private TextField makeField;
    @FXML private TextField modelField;
    @FXML private TextField regNumberField;
    @FXML private Spinner<Integer> yearSpinner;

    private VehicleDao vehicleDao;
    private ObservableList<Vehicle> vehicleList;

    public void initialize() {
        vehicleDao = new VehicleDao();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        makeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("productionYear"));

        loadVehicles();

        // Listener do wypełniania pól po zaznaczeniu wiersza
        vehicleTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue)
        );
    }

    private void loadVehicles() {
        vehicleList = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleTable.setItems(vehicleList);
    }

    private void populateFields(Vehicle vehicle) {
        if (vehicle != null) {
            makeField.setText(vehicle.getMake());
            modelField.setText(vehicle.getModel());
            regNumberField.setText(vehicle.getRegistrationNumber());
            yearSpinner.getValueFactory().setValue(vehicle.getProductionYear());
        } else {
            clearFields();
        }
    }

    private void clearFields() {
        makeField.clear();
        modelField.clear();
        regNumberField.clear();
        yearSpinner.getValueFactory().setValue(java.time.LocalDate.now().getYear()); // Ustaw domyślny rok
        vehicleTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddVehicle() {
        if (validateInput()) {
            Vehicle newVehicle = new Vehicle(
                    makeField.getText(),
                    modelField.getText(),
                    regNumberField.getText(),
                    yearSpinner.getValue()
            );
            try {
                vehicleDao.save(newVehicle);
                vehicleList.add(newVehicle); // Dodaj bezpośrednio do listy
                clearFields();
            } catch (Exception e) { // Łapanie naruszenia UNIQUE
                showError("Błąd dodawania pojazdu", "Nie udało się dodać pojazdu. Sprawdź czy numer rejestracyjny nie jest już zajęty.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć pojazd do edycji.");
            return;
        }
        if (validateInput()) {
            selectedVehicle.setMake(makeField.getText());
            selectedVehicle.setModel(modelField.getText());
            selectedVehicle.setRegistrationNumber(regNumberField.getText());
            selectedVehicle.setProductionYear(yearSpinner.getValue());
            try {
                vehicleDao.update(selectedVehicle);
                // Odświeżenie elementu w TableView
                vehicleTable.refresh();
                clearFields();
            } catch (Exception e) {
                showError("Błąd edycji pojazdu", "Nie udało się zaktualizować pojazdu.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć pojazd do usunięcia.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć pojazd: " + selectedVehicle.getRegistrationNumber() + "?");
        confirmation.setContentText("Tej operacji nie można cofnąć. Powiązane przypisania mogą zostać zmodyfikowane (ustawienie pojazdu na NULL).");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                vehicleDao.delete(selectedVehicle);
                vehicleList.remove(selectedVehicle);
                clearFields();
            } catch (Exception e) {
                showError("Błąd usuwania pojazdu", "Nie udało się usunąć pojazdu.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadVehicles();
        clearFields();
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (makeField.getText() == null || makeField.getText().isEmpty()) {
            errorMessage += "Marka nie może być pusta.\n";
        }
        if (modelField.getText() == null || modelField.getText().isEmpty()) {
            errorMessage += "Model nie może być pusty.\n";
        }
        if (regNumberField.getText() == null || regNumberField.getText().isEmpty()) {
            errorMessage += "Numer rejestracyjny nie może być pusty.\n";
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