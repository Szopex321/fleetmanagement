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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
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

    @FXML private Button showAssignmentDetailsButton;
    @FXML private Button editAssignmentButton;
    @FXML private Button deleteAssignmentButton;


    private AssignmentDao assignmentDao;
    private VehicleDao vehicleDao;
    private DriverDao driverDao;

    private ObservableList<Assignment> assignmentList; // Obserwowalna lista przypisań dla tabeli

    private final ObservableList<String> assignmentStatuses = FXCollections.observableArrayList(
            "Zaplanowane", "W trakcie", "Zakończone", "Anulowane", "Opóźnione", "Problem"
    );

    public void initialize() {
        // Tworzenie instancji wszystkich potrzebnych DAO.
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

        assignmentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            booleanItemSelected = (newSelection != null);
            showAssignmentDetailsButton.setDisable(!booleanItemSelected);
            if(editAssignmentButton != null) editAssignmentButton.setDisable(!booleanItemSelected);
            if(deleteAssignmentButton != null) deleteAssignmentButton.setDisable(!booleanItemSelected);
        });
        showAssignmentDetailsButton.setDisable(true);
        if(editAssignmentButton != null) editAssignmentButton.setDisable(true);
        if(deleteAssignmentButton != null) deleteAssignmentButton.setDisable(true);
    }

    private boolean booleanItemSelected = false;

    private void loadAssignments() {
        assignmentList = FXCollections.observableArrayList(assignmentDao.findAll());
        assignmentTable.setItems(assignmentList);
        assignmentTable.getSelectionModel().clearSelection();
    }

    private void setupDialogControls(GridPane dialogPaneContent, Assignment assignmentToEdit) {
        // Pobranie referencji do kontrolek z AssignmentDialog.fxml.
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

        // Ładowanie danych do ComboBoxów.
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

        if (assignmentToEdit != null) { // Jeśli edycja/szczegóły
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
        // Pobieranie wartości z kontrolek dialogu i ustawianie ich w obiekcie `assignment`.
        assignment.setVehicle(((ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog")).getValue());
        assignment.setDriver(((ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog")).getValue());
        assignment.setStartDate(((DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface")).getValue());
        assignment.setEndDate(((DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface")).getValue());

        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");
        String destinationText = destinationField.getText();
        assignment.setDestination( (destinationText!=null && !destinationText.trim().isEmpty()) ? destinationText.trim() : null );

        TextField purposeField = (TextField) dialogPaneContent.lookup("#purposeFieldDialog");
        String purposeText = purposeField.getText();
        assignment.setPurpose( (purposeText!=null && !purposeText.trim().isEmpty()) ? purposeText.trim() : null );

        assignment.setStatus(((ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog")).getValue());

        TextField startMileageField = (TextField) dialogPaneContent.lookup("#startMileageFieldDialog");
        String startMileageStr = startMileageField.getText();
        assignment.setStartMileage( (startMileageStr!=null && !startMileageStr.trim().isEmpty()) ? Integer.parseInt(startMileageStr.trim()) : null);

        TextField endMileageField = (TextField) dialogPaneContent.lookup("#endMileageFieldDialog");
        String endMileageStr = endMileageField.getText();
        assignment.setEndMileage( (endMileageStr!=null && !endMileageStr.trim().isEmpty()) ? Integer.parseInt(endMileageStr.trim()) : null);

        TextArea notesTextArea = (TextArea) dialogPaneContent.lookup("#notesTextAreaDialog");
        String notesContent = notesTextArea.getText();
        if (notesContent != null) {
            notesContent = notesContent.trim();
            assignment.setNotes(notesContent.isEmpty() ? null : notesContent);
        } else {
            assignment.setNotes(null);
        }
        // creationDate jest obsługiwane przez @PrePersist w encji lub przez bazę
    }

    private boolean validateDialogInput(GridPane dialogPaneContent, Assignment currentAssignment) {
        // Pobranie referencji do kontrolek.
        StringBuilder errorMessage = new StringBuilder();
        ComboBox<Vehicle> vehicleComboBox = (ComboBox<Vehicle>) dialogPaneContent.lookup("#vehicleComboBoxDialog");
        ComboBox<Driver> driverComboBox = (ComboBox<Driver>) dialogPaneContent.lookup("#driverComboBoxDialog");
        DatePicker startDatePicker = (DatePicker) dialogPaneContent.lookup("#startDatePickerDialogInterface");
        DatePicker endDatePicker = (DatePicker) dialogPaneContent.lookup("#endDatePickerDialogInterface");
        TextField destinationField = (TextField) dialogPaneContent.lookup("#destinationFieldDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        TextField startMileageField = (TextField) dialogPaneContent.lookup("#startMileageFieldDialog");
        TextField endMileageField = (TextField) dialogPaneContent.lookup("#endMileageFieldDialog");

        Vehicle selectedVehicle = vehicleComboBox.getValue();
        Driver selectedDriver = driverComboBox.getValue();
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();

        if (selectedVehicle == null) errorMessage.append("Należy wybrać pojazd.\n");
        if (selectedDriver == null) errorMessage.append("Należy wybrać kierowcę.\n");
        if (startDate == null) errorMessage.append("Należy wybrać datę rozpoczęcia.\n");
        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            errorMessage.append("Data zakończenia nie może być wcześniejsza niż data rozpoczęcia.\n");

        String destText = destinationField.getText();
        if (destText == null || destText.trim().isEmpty())
            errorMessage.append("Krótki cel podróży jest wymagany.\n");
        if(statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty())
            errorMessage.append("Status przypisania jest wymagany.\n");

        Integer startM = null, endM = null;
        String startMileageStrVal = startMileageField.getText();
        if (startMileageStrVal != null && !startMileageStrVal.trim().isEmpty()) {
            try { startM = Integer.parseInt(startMileageStrVal.trim());
                if(startM < 0) errorMessage.append("Przebieg początkowy nie może być ujemny.\n");
            } catch (NumberFormatException e) { errorMessage.append("Przebieg początkowy musi być liczbą.\n"); }
        }
        String endMileageStrVal = endMileageField.getText();
        if (endMileageStrVal != null && !endMileageStrVal.trim().isEmpty()) {
            try { endM = Integer.parseInt(endMileageStrVal.trim());
                if(endM < 0) errorMessage.append("Przebieg końcowy nie może być ujemny.\n");
            } catch (NumberFormatException e) { errorMessage.append("Przebieg końcowy musi być liczbą.\n"); }
        }
        if(startM != null && endM != null && endM < startM) {
            errorMessage.append("Przebieg końcowy nie może być mniejszy niż początkowy.\n");
        }

        // Walidacja pokrywania się terminów (biznesowa).
        Long currentAssignmentId = (currentAssignment != null) ? currentAssignment.getId() : null;
        // Sprawdzenie dla pojazdu
        if (selectedVehicle != null && startDate != null) {
            if (assignmentDao.hasOverlappingAssignmentForVehicle(selectedVehicle.getId(), startDate, endDate, currentAssignmentId)) {
                errorMessage.append("Wybrany pojazd ("+selectedVehicle.getRegistrationNumber()+") jest już przypisany w pokrywającym się terminie.\n");
            }
        }
        // Sprawdzenie dla kierowcy
        if (selectedDriver != null && startDate != null) {
            if (assignmentDao.hasOverlappingAssignmentForDriver(selectedDriver.getId(), startDate, endDate, currentAssignmentId)) {
                errorMessage.append("Wybrany kierowca ("+selectedDriver.getLastName()+") jest już przypisany w pokrywającym się terminie.\n");
            }
        }

        if (errorMessage.length() > 0) {
            showError("Błąd walidacji danych",errorMessage.toString());
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
                if (validateDialogInput(dialogPaneContent, null)) {
                    Assignment newAssignment = new Assignment();
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
                if (validateDialogInput(dialogPaneContent, selectedAssignment)) {
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
    private void handleShowAssignmentDetails() {
        Assignment selectedAssignment = assignmentTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/AssignmentDialog.fxml"));
            GridPane dialogPaneContent = loader.load();
            setupDialogControls(dialogPaneContent, selectedAssignment);
            setDialogControlsReadOnly(dialogPaneContent, true);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Szczegóły Przypisania (ID: " + selectedAssignment.getId() +")");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().add(okButtonType);
            dialog.setResizable(true);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna szczegółów: " + e.getMessage());
        }
    }

    private void setDialogControlsReadOnly(Pane parentPane, boolean readOnly) {
        for (Node node : parentPane.getChildren()) {
            if (node instanceof TextField) ((TextField) node).setEditable(!readOnly);
            else if (node instanceof TextArea) ((TextArea) node).setEditable(!readOnly);
            else if (node instanceof ComboBox) ((ComboBox<?>) node).setDisable(readOnly);
            else if (node instanceof DatePicker) ((DatePicker) node).setDisable(readOnly);
            else if (node instanceof Spinner) ((Spinner<?>) node).setDisable(readOnly);
            if (node instanceof Pane) setDialogControlsReadOnly((Pane) node, readOnly);
        }
    }

    @FXML
    private void handleDeleteAssignment() {
        Assignment selectedAssignment = assignmentTable.getSelectionModel().getSelectedItem();
        if (selectedAssignment == null) {
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