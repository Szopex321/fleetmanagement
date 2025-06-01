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
    @FXML private TableColumn<Assignment, String> vehicleColumn;
    @FXML private TableColumn<Assignment, String> driverColumn;
    @FXML private TableColumn<Assignment, LocalDate> startDateColumn;
    @FXML private TableColumn<Assignment, LocalDate> endDateColumn;
    @FXML private TableColumn<Assignment, String> destinationColumn;
    @FXML private TableColumn<Assignment, String> statusTableColumn;
    @FXML private TableColumn<Assignment, String> purposeTableColumn;


    private AssignmentDao assignmentDao;
    private VehicleDao vehicleDao;
    private DriverDao driverDao;

    private ObservableList<Assignment> assignmentList;

    private final ObservableList<String> assignmentStatuses = FXCollections.observableArrayList("Zaplanowane", "W trakcie", "Zakończone", "Anulowane", "Opóźnione", "Problem");

    public void initialize() {
        assignmentDao = new AssignmentDao();
        vehicleDao = new VehicleDao();
        driverDao = new DriverDao();

        vehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicleDisplay"));
        driverColumn.setCellValueFactory(new PropertyValueFactory<>("driverDisplay"));
        startDateColumn.setCellValueFactory(new PropertyValueFactory<>("startDate"));
        endDateColumn.setCellValueFactory(new PropertyValueFactory<>("endDate"));
        destinationColumn.setCellValueFactory(new PropertyValueFactory<>("destination"));
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        purposeTableColumn.setCellValueFactory(new PropertyValueFactory<>("purpose"));

        loadAssignments();
    }

    private void loadAssignments() {
        assignmentList = FXCollections.observableArrayList(assignmentDao.findAll());
        assignmentTable.setItems(assignmentList);
    }

    private void setupDialogControls(GridPane dialogPaneContent, Assignment assignmentToEdit) {
        ComboBox<Vehicle> vehicleComboBox = (ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog");
        ComboBox<Driver> driverComboBox = (ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog");
        DatePicker startDatePicker = (DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface");
        DatePicker endDatePicker = (DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface");
        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");
        TextField purposeField = (TextField) dialogPaneContent.lookup("#purposeFieldDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        TextField startMileageField = (TextField) dialogPaneContent.lookup("#startMileageFieldDialog");
        TextField endMileageField = (TextField) dialogPaneContent.lookup("#endMileageFieldDialog");
        TextArea notesTextArea = (TextArea) dialogPaneContent.lookup("#notesTextAreaDialog");

        ObservableList<Vehicle> vehicles = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleComboBox.setItems(vehicles);
        ObservableList<Driver> drivers = FXCollections.observableArrayList(driverDao.findAll());
        driverComboBox.setItems(drivers);
        statusComboBox.setItems(assignmentStatuses);

        vehicleComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Vehicle v) { return v == null ? null : v.toString(); }
            @Override public Vehicle fromString(String s) { return null; }
        });
        driverComboBox.setConverter(new StringConverter<>() {
            @Override public String toString(Driver d) { return d == null ? null : d.toString(); }
            @Override public Driver fromString(String s) { return null; }
        });

        TextFormatter<String> mileageFormatter1 = new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null);
        TextFormatter<String> mileageFormatter2 = new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*") ? change : null);
        startMileageField.setTextFormatter(mileageFormatter1);
        endMileageField.setTextFormatter(mileageFormatter2);


        if (assignmentToEdit != null) {
            if(assignmentToEdit.getVehicle() != null) {
                for(Vehicle v : vehicles) { if(v.getId().equals(assignmentToEdit.getVehicle().getId())) { vehicleComboBox.setValue(v); break; } }
            }
            if(assignmentToEdit.getDriver() != null) {
                for(Driver d : drivers) { if(d.getId().equals(assignmentToEdit.getDriver().getId())) { driverComboBox.setValue(d); break; } }
            }
            startDatePicker.setValue(assignmentToEdit.getStartDate());
            endDatePicker.setValue(assignmentToEdit.getEndDate());
            destinationField.setText(assignmentToEdit.getDestination());
            purposeField.setText(assignmentToEdit.getPurpose());
            statusComboBox.setValue(assignmentToEdit.getStatus());
            startMileageField.setText(assignmentToEdit.getStartMileage() != null ? assignmentToEdit.getStartMileage().toString() : "");
            endMileageField.setText(assignmentToEdit.getEndMileage() != null ? assignmentToEdit.getEndMileage().toString() : "");
            notesTextArea.setText(assignmentToEdit.getNotes());
            vehicleComboBox.requestFocus();
        } else { // Dodawanie
            startDatePicker.setValue(LocalDate.now());
            statusComboBox.setValue("Zaplanowane");
            vehicleComboBox.requestFocus();
        }
    }

    private void updateAssignmentFromDialog(Assignment assignment, GridPane dialogPaneContent) {
        assignment.setVehicle(((ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog")).getValue());
        assignment.setDriver(((ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog")).getValue());
        assignment.setStartDate(((DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface")).getValue());
        assignment.setEndDate(((DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface")).getValue());
        assignment.setDestination(((TextField) dialogPaneContent.lookup("#destinationFieldDialog")).getText().trim());

        String purpose = ((TextField) dialogPaneContent.lookup("#purposeFieldDialog")).getText().trim();
        assignment.setPurpose(purpose.isEmpty() ? null : purpose);

        assignment.setStatus(((ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog")).getValue());

        String startMileageStr = ((TextField) dialogPaneContent.lookup("#startMileageFieldDialog")).getText().trim();
        assignment.setStartMileage(startMileageStr.isEmpty() ? null : Integer.parseInt(startMileageStr));

        String endMileageStr = ((TextField) dialogPaneContent.lookup("#endMileageFieldDialog")).getText().trim();
        assignment.setEndMileage(endMileageStr.isEmpty() ? null : Integer.parseInt(endMileageStr));

        String notes = ((TextArea) dialogPaneContent.lookup("#notesTextAreaDialog")).getText().trim();
        assignment.setNotes(notes.isEmpty() ? null : notes);
    }

    private boolean validateDialogInput(GridPane dialogPaneContent) {
        StringBuilder errorMessage = new StringBuilder();
        if (((ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog")).getValue() == null) {
            errorMessage.append("Należy wybrać pojazd.\n");
        }
        if (((ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog")).getValue() == null) {
            errorMessage.append("Należy wybrać kierowcę.\n");
        }
        DatePicker startDatePicker = (DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface");
        if (startDatePicker.getValue() == null) {
            errorMessage.append("Należy wybrać datę rozpoczęcia.\n");
        }
        DatePicker endDatePicker = (DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface");
        if (startDatePicker.getValue() != null && endDatePicker.getValue() != null &&
                endDatePicker.getValue().isBefore(startDatePicker.getValue())) {
            errorMessage.append("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.\n");
        }
        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");
        if (destinationField.getText() == null || destinationField.getText().trim().isEmpty()) {
            errorMessage.append("Krótki cel podróży jest wymagany.\n");
        }
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        if(statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("Status przypisania jest wymagany.\n");
        }

        TextField startMileageField = (TextField) dialogPaneContent.lookup("#startMileageFieldDialog");
        TextField endMileageField = (TextField) dialogPaneContent.lookup("#endMileageFieldDialog");
        Integer startM = null, endM = null;

        try {
            if(!startMileageField.getText().trim().isEmpty()) startM = Integer.parseInt(startMileageField.getText().trim());
            if(startM != null && startM < 0) errorMessage.append("Przebieg początkowy nie może być ujemny.\n");
        } catch (NumberFormatException e) { errorMessage.append("Przebieg początkowy musi być liczbą.\n"); }

        try {
            if(!endMileageField.getText().trim().isEmpty()) endM = Integer.parseInt(endMileageField.getText().trim());
            if(endM != null && endM < 0) errorMessage.append("Przebieg końcowy nie może być ujemny.\n");
        } catch (NumberFormatException e) { errorMessage.append("Przebieg końcowy musi być liczbą.\n"); }

        if(startM != null && endM != null && endM < startM) {
            errorMessage.append("Przebieg końcowy nie może być mniejszy niż początkowy.\n");
        }


        if (errorMessage.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd walidacji danych");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage.toString());
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.showAndWait();
            return false;
        }
        return true;
    }

    @FXML
    private void handleAddAssignment() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/AssignmentDialog.fxml"));
            GridPane dialogPaneContent = loader.load();
            setupDialogControls(dialogPaneContent, null);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowe Przypisanie");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    Assignment newAssignment = new Assignment(); // Domyślny status i creationDate z @PrePersist
                    updateAssignmentFromDialog(newAssignment, dialogPaneContent);
                    try {
                        assignmentDao.save(newAssignment);
                        loadAssignments();
                    } catch (Exception e) {
                        showError("Błąd dodawania przypisania", "Nie udało się dodać przypisania.\nBłąd: " + e.getMessage());
                        e.printStackTrace();
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
                        e.printStackTrace();
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
        String vehicleInfo = selectedAssignment.getVehicle() != null ? selectedAssignment.getVehicle().toString() : "BRAK";
        String driverInfo = selectedAssignment.getDriver() != null ? selectedAssignment.getDriver().toString() : "BRAK";
        confirmation.setContentText("Pojazd: " + vehicleInfo + "\nKierowca: " + driverInfo + "\nCel: " + selectedAssignment.getDestination());
        confirmation.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
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