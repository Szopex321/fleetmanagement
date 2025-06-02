package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.MainApp;
import com.example.fleetmanagement.dao.VehicleDao;
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

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;

public class VehicleController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, String> regNumberColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;
    @FXML private TableColumn<Vehicle, String> statusTableColumn;
    @FXML private TableColumn<Vehicle, String> vinTableColumn;

    @FXML private Button showVehicleDetailsButton;
    @FXML private Button editVehicleButton;
    @FXML private Button deleteVehicleButton;


    private VehicleDao vehicleDao;
    private ObservableList<Vehicle> vehicleList;

    private final ObservableList<String> fuelTypes = FXCollections.observableArrayList(
            "Benzyna", "Diesel", "LPG", "Elektryczny", "Hybryda", "Hybryda Plug-in", "Wodór", "Benzyna+CNG", "Inny"
    );
    private final ObservableList<String> vehicleStatuses = FXCollections.observableArrayList(
            "Dostępny", "W użyciu", "W serwisie", "Planowany serwis", "Awaria", "Sprzedany", "Wycofany", "Rezerwacja", "Wynajęty"
    );

    public void initialize() {
        vehicleDao = new VehicleDao();

        makeColumn.setCellValueFactory(new PropertyValueFactory<>("make"));
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        regNumberColumn.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        yearColumn.setCellValueFactory(new PropertyValueFactory<>("productionYear"));
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        vinTableColumn.setCellValueFactory(new PropertyValueFactory<>("vin"));

        loadVehicles();

        // Listener do aktywacji/dezaktywacji przycisków
        vehicleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            booleanItemSelected = (newSelection != null);
            showVehicleDetailsButton.setDisable(!booleanItemSelected);
            if (editVehicleButton != null) editVehicleButton.setDisable(!booleanItemSelected);
            if (deleteVehicleButton != null) deleteVehicleButton.setDisable(!booleanItemSelected);
        });
        // Początkowa dezaktywacja przycisków
        showVehicleDetailsButton.setDisable(true);
        if (editVehicleButton != null) editVehicleButton.setDisable(true);
        if (deleteVehicleButton != null) deleteVehicleButton.setDisable(true);
    }

    private boolean booleanItemSelected = false; // Flaga pomocnicza

    private void loadVehicles() {
        vehicleList = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleTable.setItems(vehicleList);
        vehicleTable.getSelectionModel().clearSelection(); // Wyczyść zaznaczenie po załadowaniu
    }

    private void setupDialogControls(GridPane dialogPaneContent, Vehicle vehicleToEdit) {
        TextField makeField = (TextField) dialogPaneContent.lookup("#makeFieldDialog");
        TextField modelField = (TextField) dialogPaneContent.lookup("#modelFieldDialog");
        TextField regNumberField = (TextField) dialogPaneContent.lookup("#regNumberFieldDialog");
        Spinner<Integer> yearSpinner = (Spinner<Integer>) dialogPaneContent.lookup("#yearSpinnerDialog");
        TextField vinField = (TextField) dialogPaneContent.lookup("#vinFieldDialog");
        ComboBox<String> fuelTypeComboBox = (ComboBox<String>) dialogPaneContent.lookup("#fuelTypeComboBoxDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        TextField mileageField = (TextField) dialogPaneContent.lookup("#mileageFieldDialog");
        DatePicker purchaseDatePicker = (DatePicker) dialogPaneContent.lookup("#purchaseDatePickerDialog");
        DatePicker lastServiceDatePicker = (DatePicker) dialogPaneContent.lookup("#lastServiceDatePickerDialog");
        DatePicker insuranceExpiryDatePicker = (DatePicker) dialogPaneContent.lookup("#insuranceExpiryDatePickerDialog");
        TextArea notesTextArea = (TextArea) dialogPaneContent.lookup("#notesTextAreaDialog");

        fuelTypeComboBox.setItems(fuelTypes);
        statusComboBox.setItems(vehicleStatuses);

        mileageField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("\\d*")) {
                return change;
            }
            return null;
        }));

        if (vehicleToEdit != null) {
            makeField.setText(vehicleToEdit.getMake());
            modelField.setText(vehicleToEdit.getModel());
            regNumberField.setText(vehicleToEdit.getRegistrationNumber());
            yearSpinner.getValueFactory().setValue(vehicleToEdit.getProductionYear() != null ? vehicleToEdit.getProductionYear() : java.time.LocalDate.now().getYear());
            vinField.setText(vehicleToEdit.getVin());
            fuelTypeComboBox.setValue(vehicleToEdit.getFuelType());
            statusComboBox.setValue(vehicleToEdit.getStatus());
            mileageField.setText(vehicleToEdit.getMileage() != null ? vehicleToEdit.getMileage().toString() : "");
            purchaseDatePicker.setValue(vehicleToEdit.getPurchaseDate());
            lastServiceDatePicker.setValue(vehicleToEdit.getLastServiceDate());
            insuranceExpiryDatePicker.setValue(vehicleToEdit.getInsuranceExpiryDate());
            notesTextArea.setText(vehicleToEdit.getNotes());
            makeField.requestFocus();
        } else {
            yearSpinner.getValueFactory().setValue(java.time.LocalDate.now().getYear());
            statusComboBox.setValue("Dostępny");
            makeField.requestFocus();
        }
    }

    private void updateVehicleFromDialog(Vehicle vehicle, GridPane dialogPaneContent) {
        vehicle.setMake(((TextField) dialogPaneContent.lookup("#makeFieldDialog")).getText());
        vehicle.setModel(((TextField) dialogPaneContent.lookup("#modelFieldDialog")).getText());
        vehicle.setRegistrationNumber(((TextField) dialogPaneContent.lookup("#regNumberFieldDialog")).getText());
        vehicle.setProductionYear(((Spinner<Integer>) dialogPaneContent.lookup("#yearSpinnerDialog")).getValue());

        String vinText = ((TextField) dialogPaneContent.lookup("#vinFieldDialog")).getText();
        if(vinText != null) {
            vinText = vinText.trim();
            vehicle.setVin(vinText.isEmpty() ? null : vinText);
        } else {
            vehicle.setVin(null);
        }

        vehicle.setFuelType(((ComboBox<String>) dialogPaneContent.lookup("#fuelTypeComboBoxDialog")).getValue());
        vehicle.setStatus(((ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog")).getValue());

        String mileageText = ((TextField) dialogPaneContent.lookup("#mileageFieldDialog")).getText();
        if (mileageText != null) {
            mileageText = mileageText.trim();
            vehicle.setMileage(mileageText.isEmpty() ? null : Integer.parseInt(mileageText));
        } else {
            vehicle.setMileage(null);
        }

        vehicle.setPurchaseDate(((DatePicker) dialogPaneContent.lookup("#purchaseDatePickerDialog")).getValue());
        vehicle.setLastServiceDate(((DatePicker) dialogPaneContent.lookup("#lastServiceDatePickerDialog")).getValue());
        vehicle.setInsuranceExpiryDate(((DatePicker) dialogPaneContent.lookup("#insuranceExpiryDatePickerDialog")).getValue());

        TextArea notesTextArea = (TextArea) dialogPaneContent.lookup("#notesTextAreaDialog");
        String notesContent = notesTextArea.getText();
        if (notesContent != null) {
            notesContent = notesContent.trim();
            vehicle.setNotes(notesContent.isEmpty() ? null : notesContent);
        } else {
            vehicle.setNotes(null);
        }
    }

    private boolean validateDialogInput(GridPane dialogPaneContent) {
        StringBuilder errorMessage = new StringBuilder();
        TextField makeField = (TextField) dialogPaneContent.lookup("#makeFieldDialog");
        TextField modelField = (TextField) dialogPaneContent.lookup("#modelFieldDialog");
        TextField regNumberField = (TextField) dialogPaneContent.lookup("#regNumberFieldDialog");
        TextField vinField = (TextField) dialogPaneContent.lookup("#vinFieldDialog");
        TextField mileageField = (TextField) dialogPaneContent.lookup("#mileageFieldDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        Spinner<Integer> yearSpinner = (Spinner<Integer>) dialogPaneContent.lookup("#yearSpinnerDialog");
        DatePicker purchaseDatePicker = (DatePicker) dialogPaneContent.lookup("#purchaseDatePickerDialog");
        DatePicker lastServiceDatePicker = (DatePicker) dialogPaneContent.lookup("#lastServiceDatePickerDialog");
        DatePicker insuranceExpiryDatePicker = (DatePicker) dialogPaneContent.lookup("#insuranceExpiryDatePickerDialog");

        if (makeField.getText() == null || makeField.getText().trim().isEmpty()) errorMessage.append("Marka jest wymagana.\n");
        if (modelField.getText() == null || modelField.getText().trim().isEmpty()) errorMessage.append("Model jest wymagany.\n");
        if (regNumberField.getText() == null || regNumberField.getText().trim().isEmpty()) errorMessage.append("Numer rejestracyjny jest wymagany.\n");
        String vinTextVal = vinField.getText();
        if (vinTextVal != null && !vinTextVal.trim().isEmpty() && vinTextVal.trim().length() != 17)
            errorMessage.append("Numer VIN musi mieć 17 znaków (lub pozostać pusty).\n");
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty())
            errorMessage.append("Status pojazdu jest wymagany.\n");
        String mileageTextVal = mileageField.getText();
        if (mileageTextVal != null && !mileageTextVal.trim().isEmpty()) {
            try {
                int mileage = Integer.parseInt(mileageTextVal.trim());
                if (mileage < 0) errorMessage.append("Przebieg nie może być ujemny.\n");
            } catch (NumberFormatException e) { errorMessage.append("Przebieg musi być poprawną liczbą całkowitą.\n"); }
        }

        LocalDate purchaseDate = purchaseDatePicker.getValue();
        LocalDate lastServiceDate = lastServiceDatePicker.getValue();
        LocalDate insuranceExpiryDateVal = insuranceExpiryDatePicker.getValue();
        Integer productionYearVal = yearSpinner.getValue();

        if (productionYearVal != null) {
            if (purchaseDate != null && purchaseDate.getYear() < productionYearVal) {
                errorMessage.append("Rok daty zakupu nie może być wcześniejszy niż rok produkcji.\n");
            }
            if (lastServiceDate != null && lastServiceDate.isBefore(LocalDate.of(productionYearVal, 1, 1))) {
                errorMessage.append("Data ostatniego serwisu nie może być wcześniejsza niż rok produkcji.\n");
            }
            if (insuranceExpiryDateVal != null && insuranceExpiryDateVal.isBefore(LocalDate.of(productionYearVal, 1, 1).plusDays(1))) {
                errorMessage.append("Data ważności ubezpieczenia musi być późniejsza niż rok produkcji.\n");
            }
        }
        if (lastServiceDate != null && purchaseDate != null && lastServiceDate.isBefore(purchaseDate)) {
            errorMessage.append("Data ostatniego serwisu nie może być wcześniejsza niż data zakupu.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Błąd walidacji danych", errorMessage.toString());
            return false;
        }
        return true;
    }

    @FXML
    private void handleAddVehicle() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/VehicleDialog.fxml"));
            GridPane dialogPaneContent = loader.load();
            setupDialogControls(dialogPaneContent, null);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowy Pojazd");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    Vehicle newVehicle = new Vehicle();
                    updateVehicleFromDialog(newVehicle, dialogPaneContent);
                    try {
                        vehicleDao.save(newVehicle);
                        loadVehicles();
                    } catch (Exception e) {
                        showError("Błąd dodawania pojazdu", "Nie udało się dodać pojazdu. Sprawdź unikalność Nr Rej. i VIN.\nBłąd: " + e.getMessage());
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
    private void handleEditVehicle() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć pojazd do edycji.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/VehicleDialog.fxml"));
            GridPane dialogPaneContent = loader.load();
            setupDialogControls(dialogPaneContent, selectedVehicle);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edytuj Pojazd");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz Zmiany", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    updateVehicleFromDialog(selectedVehicle, dialogPaneContent);
                    try {
                        vehicleDao.update(selectedVehicle);
                        loadVehicles();
                    } catch (Exception e) {
                        showError("Błąd edycji pojazdu", "Nie udało się zaktualizować pojazdu. Sprawdź unikalność Nr Rej. i VIN.\nBłąd: " + e.getMessage());
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
    private void handleShowVehicleDetails() {
        Vehicle selectedVehicle = vehicleTable.getSelectionModel().getSelectedItem();
        if (selectedVehicle == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć pojazd, aby zobaczyć szczegóły.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/VehicleDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            setupDialogControls(dialogPaneContent, selectedVehicle);
            setDialogControlsReadOnly(dialogPaneContent, true);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Szczegóły Pojazdu: " + selectedVehicle.getRegistrationNumber());
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
            if (node instanceof TextField) {
                ((TextField) node).setEditable(!readOnly);
            } else if (node instanceof TextArea) {
                ((TextArea) node).setEditable(!readOnly);
            } else if (node instanceof ComboBox) {
                ((ComboBox<?>) node).setDisable(readOnly);
            } else if (node instanceof DatePicker) {
                ((DatePicker) node).setDisable(readOnly);
            } else if (node instanceof Spinner) {
                ((Spinner<?>) node).setDisable(readOnly);
            }
            if (node instanceof Pane) {
                setDialogControlsReadOnly((Pane) node, readOnly);
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
        confirmation.setContentText("Tej operacji nie można cofnąć. Powiązane przypisania mogą zostać zmodyfikowane.");
        confirmation.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                vehicleDao.delete(selectedVehicle);
                loadVehicles();
            } catch (Exception e) {
                showError("Błąd usuwania pojazdu", "Nie udało się usunąć pojazdu.\n" + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadVehicles();
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