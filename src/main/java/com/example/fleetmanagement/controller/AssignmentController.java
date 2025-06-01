package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.MainApp;
import com.example.fleetmanagement.dao.AssignmentDao;
import com.example.fleetmanagement.dao.DriverDao;
import com.example.fleetmanagement.dao.VehicleDao;
import com.example.fleetmanagement.model.Assignment;
import com.example.fleetmanagement.model.Driver;
import com.example.fleetmanagement.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;

import java.io.IOException;
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

    private AssignmentDao assignmentDao;
    private VehicleDao vehicleDao;
    private DriverDao driverDao;

    private ObservableList<Assignment> assignmentList;

    public void initialize() {
        assignmentDao = new AssignmentDao();
        vehicleDao = new VehicleDao();
        driverDao = new DriverDao();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleDisplay"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driverDisplay"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));

        loadAssignments();
    }

    private void loadAssignments() {
        assignmentList = FXCollections.observableArrayList(assignmentDao.findAll());
        assignmentTable.setItems(assignmentList);
    }

    // Metoda pomocnicza do konfiguracji kontrolek w dialogu
    private void setupDialogControls(GridPane dialogPaneContent, Assignment assignmentToEdit) {
        ComboBox<Vehicle> vehicleComboBoxDialog = (ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog");
        ComboBox<Driver> driverComboBoxDialog = (ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog");
        DatePicker startDatePicker = (DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface");
        DatePicker endDatePicker = (DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface");
        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");

        ObservableList<Vehicle> vehicles = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleComboBoxDialog.setItems(vehicles);
        ObservableList<Driver> drivers = FXCollections.observableArrayList(driverDao.findAll());
        driverComboBoxDialog.setItems(drivers);

        vehicleComboBoxDialog.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) { return v == null ? null : v.toString(); }
            @Override public Vehicle fromString(String s) { return null; }
        });
        driverComboBoxDialog.setConverter(new StringConverter<>() {
            @Override public String toString(Driver d) { return d == null ? null : d.toString(); }
            @Override public Driver fromString(String s) { return null; }
        });

        if (assignmentToEdit != null) { // Tryb edycji
            if(assignmentToEdit.getVehicle() != null) {
                for(Vehicle v : vehicles) {
                    if(v.getId().equals(assignmentToEdit.getVehicle().getId())) {
                        vehicleComboBoxDialog.setValue(v);
                        break;
                    }
                }
            }
            if(assignmentToEdit.getDriver() != null) {
                for(Driver d : drivers) {
                    if(d.getId().equals(assignmentToEdit.getDriver().getId())) {
                        driverComboBoxDialog.setValue(d);
                        break;
                    }
                }
            }

            startDatePicker.setValue(assignmentToEdit.getStartDate());
            endDatePicker.setValue(assignmentToEdit.getEndDate());
            destinationField.setText(assignmentToEdit.getDestination());
            vehicleComboBoxDialog.requestFocus();
        } else { // Tryb dodawania
            startDatePicker.setValue(LocalDate.now()); // Domyślna data rozpoczęcia
            vehicleComboBoxDialog.requestFocus();
        }
    }

    // Metoda do aktualizacji obiektu Assignment na podstawie danych z dialogu
    private void updateAssignmentFromDialog(Assignment assignment, GridPane dialogPaneContent) {
        assignment.setVehicle(((ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog")).getValue());
        assignment.setDriver(((ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog")).getValue());
        assignment.setStartDate(((DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface")).getValue());
        assignment.setEndDate(((DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface")).getValue());
        assignment.setDestination(((TextField) dialogPaneContent.lookup("#destinationFieldDialog")).getText());
    }

    private boolean validateDialogInput(GridPane dialogPaneContent) {
        String errorMessage = "";
        if (((ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog")).getValue() == null) {
            errorMessage += "Należy wybrać pojazd.\n";
        }
        if (((ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog")).getValue() == null) {
            errorMessage += "Należy wybrać kierowcę.\n";
        }
        DatePicker startDatePicker = (DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface");
        DatePicker endDatePicker = (DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface");
        if (startDatePicker.getValue() == null) {
            errorMessage += "Należy wybrać datę rozpoczęcia.\n";
        }
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null &&
                endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessage += "Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.\n";
        }
        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");
        if (destinationField.getText() == null || destinationField.getText().trim().isEmpty()) {
            errorMessage += "Cel podróży jest wymagany.\n";
        }


        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd walidacji danych");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            return false;
        }
    }

    @FXML
    private void handleAddAssignment() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/AssignmentDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            setupDialogControls(dialogPaneContent, null); // null dla nowego przypisania

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowe Przypisanie");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    Assignment newAssignment = new Assignment();
                    updateAssignmentFromDialog(newAssignment, dialogPaneContent);
                    try {
                        assignmentDao.save(newAssignment);
                        loadAssignments();
                    } catch (Exception e) {
                        showError("Błąd dodawania przypisania", "Nie udało się dodać przypisania.\nBłąd: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna dialogowego: " + e.getMessage());
        }
    }

    @FXML
    private void handleEditAssignment() {
        Assignment selectedAssignment = assignmentTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć przypisanie do edycji.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/AssignmentDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            setupDialogControls(dialogPaneContent, selectedAssignment);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edytuj Przypisanie");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz Zmiany", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    updateAssignmentFromDialog(selectedAssignment, dialogPaneContent);
                    try {
                        assignmentDao.update(selectedAssignment);
                        loadAssignments();
                    } catch (Exception e) {
                        showError("Błąd edycji przypisania", "Nie udało się zaktualizować przypisania.\nBłąd: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna dialogowego: " + e.getMessage());
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
        String vehicleInfo = selectedAssignment.getVehicle() != null ? selectedAssignment.getVehicle().toString() : "N/A";
        String driverInfo = selectedAssignment.getDriver() != null ? selectedAssignment.getDriver().toString() : "N/A";
        confirmation.setContentText("Pojazd: " + vehicleInfo + "\nKierowca: " + driverInfo + "\nCel: " + selectedAssignment.getDestination());


        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                assignmentDao.delete(selectedAssignment);
                loadAssignments();
            } catch (Exception e) {
                showError("Błąd usuwania przypisania", "Nie udało się usunąć przypisania.\nBłąd: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadAssignments();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}