package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.dao.AssignmentDao;
import com.example.fleetmanagement.dao.DriverDao;
import com.example.fleetmanagement.dao.VehicleDao;
import com.example.fleetmanagement.model.Assignment;
import com.example.fleetmanagement.model.Driver;
import com.example.fleetmanagement.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.Optional;

public class AssignmentController {

    @FXML private TableView<Assignment> assignmentTable;
    @FXML private TableColumn<Assignment, Long> idColumn;
    @FXML private TableColumn<Assignment, String> vehicleColumn;
    @FXML private TableColumn<Assignment, String> driverColumn;
    @FXML private TableColumn<Assignment, LocalDate> startDateColumn;
    @FXML private TableColumn<Assignment, LocalDate> endDateColumn;
    @FXML private TableColumn<Assignment, String> destinationColumn;

    @FXML private ComboBox<Vehicle> vehicleComboBox;
    @FXML private ComboBox<Driver> driverComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TextField destinationField;

    private AssignmentDao assignmentDao;
    private VehicleDao vehicleDao;
    private DriverDao driverDao;

    private ObservableList<Assignment> assignmentList;
    private ObservableList<Vehicle> vehicleObservableList;
    private ObservableList<Driver> driverObservableList;

    public void initialize() {
        assignmentDao = new AssignmentDao();
        vehicleDao = new VehicleDao();
        driverDao = new DriverDao();

        // Konfiguracja kolumn tabeli
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleDisplay"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driverDisplay"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));

        loadVehiclesToComboBox();
        loadDriversToComboBox();

        vehicleComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Vehicle vehicle) {
                return vehicle == null ? null : vehicle.toString();
            }
            @Override
            public Vehicle fromString(String string) { return null;}
        });

        driverComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Driver driver) {
                return driver == null ? null : driver.toString();
            }
            @Override
            public Driver fromString(String string) { return null;}
        });


        loadAssignments();

        // Listener do wypełniania pól po zaznaczeniu wiersza
        assignmentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue)
        );
    }

    private void loadVehiclesToComboBox() {
        vehicleObservableList = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleComboBox.setItems(vehicleObservableList);
    }

    private void loadDriversToComboBox() {
        driverObservableList = FXCollections.observableArrayList(driverDao.findAll());
        driverComboBox.setItems(driverObservableList);
    }

    private void loadAssignments() {
        assignmentList = FXCollections.observableArrayList(assignmentDao.findAll());
        assignmentTable.setItems(assignmentList);
    }

    private void populateFields(Assignment assignment) {
        if (assignment != null) {
            vehicleComboBox.setValue(assignment.getVehicle());
            driverComboBox.setValue(assignment.getDriver());
            startDatePicker.setValue(assignment.getStartDate());
            endDatePicker.setValue(assignment.getEndDate());
            destinationField.setText(assignment.getDestination());
        } else {
            clearFields();
        }
    }

    @FXML
    private void handleClearForm() {
        clearFields();
    }

    private void clearFields() {
        vehicleComboBox.getSelectionModel().clearSelection();
        vehicleComboBox.setValue(null); // Ważne, aby usunąć wyświetlaną wartość
        driverComboBox.getSelectionModel().clearSelection();
        driverComboBox.setValue(null);
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        destinationField.clear();
        assignmentTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleAddAssignment() {
        if (validateInput()) {
            Assignment newAssignment = new Assignment(
                    vehicleComboBox.getValue(),
                    driverComboBox.getValue(),
                    startDatePicker.getValue(),
                    endDatePicker.getValue(),
                    destinationField.getText()
            );
            try {
                assignmentDao.save(newAssignment);
                loadAssignments(); // Ponowne załadowanie, aby zobaczyć ID i wszelkie zmiany
                clearFields();
            } catch (Exception e) {
                showError("Błąd dodawania przypisania", "Nie udało się dodać przypisania.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleEditAssignment() {
        Assignment selectedAssignment = assignmentTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć przypisanie do edycji.");
            return;
        }
        if (validateInput()) {
            selectedAssignment.setVehicle(vehicleComboBox.getValue());
            selectedAssignment.setDriver(driverComboBox.getValue());
            selectedAssignment.setStartDate(startDatePicker.getValue());
            selectedAssignment.setEndDate(endDatePicker.getValue());
            selectedAssignment.setDestination(destinationField.getText());
            try {
                assignmentDao.update(selectedAssignment);
                assignmentTable.refresh(); // Odświeżenie wiersza w tabeli
                clearFields();
            } catch (Exception e) {
                showError("Błąd edycji przypisania", "Nie udało się zaktualizować przypisania.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDeleteAssignment() {
        Assignment selectedAssignment = assignmentTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć przypisanie do usunięcia.");
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć to przypisanie?");
        confirmation.setContentText("Pojazd: " + (selectedAssignment.getVehicle() != null ? selectedAssignment.getVehicle().getRegistrationNumber() : "N/A") +
                "\nKierowca: " + (selectedAssignment.getDriver() != null ? selectedAssignment.getDriver().getLastName() : "N/A") +
                "\nCel: " + selectedAssignment.getDestination());

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                assignmentDao.delete(selectedAssignment);
                assignmentList.remove(selectedAssignment);
                clearFields();
            } catch (Exception e) {
                showError("Błąd usuwania przypisania", "Nie udało się usunąć przypisania.\n" + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadVehiclesToComboBox(); // Odśwież listę pojazdów, na wypadek zmian
        loadDriversToComboBox();  // Odśwież listę kierowców
        loadAssignments();
        clearFields();
    }

    private boolean validateInput() {
        String errorMessage = "";
        if (vehicleComboBox.getValue() == null) {
            errorMessage += "Należy wybrać pojazd.\n";
        }
        if (driverComboBox.getValue() == null) {
            errorMessage += "Należy wybrać kierowcę.\n";
        }
        if (startDatePicker.getValue() == null) {
            errorMessage += "Należy wybrać datę rozpoczęcia.\n";
        }
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null &&
                endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessage += "Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.\n";
        }
        if (destinationField.getText() == null || destinationField.getText().trim().isEmpty()) {
            errorMessage += "Cel podróży nie może być pusty.\n";
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