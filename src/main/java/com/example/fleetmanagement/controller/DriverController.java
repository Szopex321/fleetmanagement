package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.MainApp;
import com.example.fleetmanagement.dao.DriverDao;
import com.example.fleetmanagement.model.Driver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Pattern;

public class DriverController {

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> firstNameColumn;
    @FXML private TableColumn<Driver, String> lastNameColumn;
    @FXML private TableColumn<Driver, String> licenseNumberColumn;
    @FXML private TableColumn<Driver, String> statusTableColumn;
    @FXML private TableColumn<Driver, String> phoneTableColumn;


    private DriverDao driverDao;
    private ObservableList<Driver> driverList;

    private final ObservableList<String> driverStatuses = FXCollections.observableArrayList("Aktywny", "Na urlopie", "Zwolnienie lekarskie", "Szkolenie", "Niedostępny", "Zwolniony");


    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{1,3}[- ]?)?\\d{3}[- ]?\\d{3}[- ]?\\d{3}$|^\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");


    public void initialize() {
        driverDao = new DriverDao();

        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        licenseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        phoneTableColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        loadDrivers();
    }

    private void loadDrivers() {
        driverList = FXCollections.observableArrayList(driverDao.findAll());
        driverTable.setItems(driverList);
    }

    private void setupDialogControls(GridPane dialogPaneContent, Driver driverToEdit) {
        TextField firstNameField = (TextField) dialogPaneContent.lookup("#firstNameFieldDialog");
        TextField lastNameField = (TextField) dialogPaneContent.lookup("#lastNameFieldDialog");
        TextField licenseNumberField = (TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog");
        TextField phoneNumberField = (TextField) dialogPaneContent.lookup("#phoneNumberFieldDialog");
        TextField emailField = (TextField) dialogPaneContent.lookup("#emailFieldDialog");
        DatePicker employmentDatePicker = (DatePicker) dialogPaneContent.lookup("#employmentDatePickerDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");
        DatePicker licenseExpiryDatePicker = (DatePicker) dialogPaneContent.lookup("#licenseExpiryDatePickerDialog");
        DatePicker medicalCheckDatePicker = (DatePicker) dialogPaneContent.lookup("#medicalCheckDatePickerDialog");
        TextArea addressTextArea = (TextArea) dialogPaneContent.lookup("#addressTextAreaDialog");

        statusComboBox.setItems(driverStatuses);

        if (driverToEdit != null) { // Edycja
            firstNameField.setText(driverToEdit.getFirstName());
            lastNameField.setText(driverToEdit.getLastName());
            licenseNumberField.setText(driverToEdit.getLicenseNumber());
            phoneNumberField.setText(driverToEdit.getPhoneNumber());
            emailField.setText(driverToEdit.getEmail());
            employmentDatePicker.setValue(driverToEdit.getEmploymentDate());
            statusComboBox.setValue(driverToEdit.getStatus());
            licenseExpiryDatePicker.setValue(driverToEdit.getLicenseExpiryDate());
            medicalCheckDatePicker.setValue(driverToEdit.getMedicalCheckExpiryDate());
            addressTextArea.setText(driverToEdit.getAddress());
            firstNameField.requestFocus();
        } else { // Dodawanie
            statusComboBox.setValue("Aktywny");
            employmentDatePicker.setValue(LocalDate.now()); // Domyślna data zatrudnienia
            firstNameField.requestFocus();
        }
    }

    private void updateDriverFromDialog(Driver driver, GridPane dialogPaneContent) {
        driver.setFirstName(((TextField) dialogPaneContent.lookup("#firstNameFieldDialog")).getText());
        driver.setLastName(((TextField) dialogPaneContent.lookup("#lastNameFieldDialog")).getText());
        driver.setLicenseNumber(((TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog")).getText());

        String phone = ((TextField) dialogPaneContent.lookup("#phoneNumberFieldDialog")).getText().trim();
        driver.setPhoneNumber(phone.isEmpty() ? null : phone);

        String email = ((TextField) dialogPaneContent.lookup("#emailFieldDialog")).getText().trim();
        driver.setEmail(email.isEmpty() ? null : email);

        driver.setEmploymentDate(((DatePicker) dialogPaneContent.lookup("#employmentDatePickerDialog")).getValue());
        driver.setStatus(((ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog")).getValue());
        driver.setLicenseExpiryDate(((DatePicker) dialogPaneContent.lookup("#licenseExpiryDatePickerDialog")).getValue());
        driver.setMedicalCheckExpiryDate(((DatePicker) dialogPaneContent.lookup("#medicalCheckDatePickerDialog")).getValue());

        TextArea addressTextArea = (TextArea) dialogPaneContent.lookup("#addressTextAreaDialog");
        String addressContent = addressTextArea.getText();
        if (addressContent != null) {
            addressContent = addressContent.trim();
            driver.setAddress(addressContent.isEmpty() ? null : addressContent);
        } else {
            driver.setAddress(null);
        }


    }

    private boolean validateDialogInput(GridPane dialogPaneContent) {
        StringBuilder errorMessage = new StringBuilder();
        TextField firstNameField = (TextField) dialogPaneContent.lookup("#firstNameFieldDialog");
        TextField lastNameField = (TextField) dialogPaneContent.lookup("#lastNameFieldDialog");
        TextField licenseNumberField = (TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog");
        TextField phoneNumberField = (TextField) dialogPaneContent.lookup("#phoneNumberFieldDialog");
        TextField emailField = (TextField) dialogPaneContent.lookup("#emailFieldDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");

        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage.append("Imię jest wymagane.\n");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage.append("Nazwisko jest wymagane.\n");
        }
        if (licenseNumberField.getText() == null || licenseNumberField.getText().trim().isEmpty()) {
            errorMessage.append("Numer prawa jazdy jest wymagany.\n");
        }
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) {
            errorMessage.append("Status kierowcy jest wymagany.\n");
        }
        String phone = phoneNumberField.getText().trim();
        if (!phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            errorMessage.append("Niepoprawny format numeru telefonu.\n");
        }
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            errorMessage.append("Niepoprawny format adresu email.\n");
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
    private void handleAddDriver() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/DriverDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            setupDialogControls(dialogPaneContent, null);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowego Kierowcę");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    Driver newDriver = new Driver(); // Używa domyślnego statusu z konstruktora
                    updateDriverFromDialog(newDriver, dialogPaneContent);
                    try {
                        driverDao.save(newDriver);
                        loadDrivers();
                    } catch (Exception e) {
                        showError("Błąd dodawania kierowcy", "Nie udało się dodać kierowcy. Sprawdź unikalność nr prawa jazdy i email.\nBłąd: " + e.getMessage());
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
    private void handleEditDriver() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć kierowcę do edycji.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/DriverDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            setupDialogControls(dialogPaneContent, selectedDriver);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edytuj Dane Kierowcy");
            dialog.getDialogPane().setContent(dialogPaneContent);
            ButtonType okButtonType = new ButtonType("Zapisz Zmiany", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(dialogPaneContent)) {
                    updateDriverFromDialog(selectedDriver, dialogPaneContent);
                    try {
                        driverDao.update(selectedDriver);
                        loadDrivers();
                    } catch (Exception e) {
                        showError("Błąd edycji kierowcy", "Nie udało się zaktualizować danych. Sprawdź unikalność nr prawa jazdy i email.\nBłąd: " + e.getMessage());
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
    private void handleDeleteDriver() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć kierowcę do usunięcia.");
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Potwierdzenie usunięcia");
        confirmation.setHeaderText("Czy na pewno chcesz usunąć kierowcę: " + selectedDriver.getFirstName() + " " + selectedDriver.getLastName() + "?");
        confirmation.setContentText("Tej operacji nie można cofnąć. Powiązane przypisania mogą zostać zmodyfikowane.");
        confirmation.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                driverDao.delete(selectedDriver);
                loadDrivers();
            } catch (Exception e) {
                showError("Błąd usuwania kierowcy", "Nie udało się usunąć kierowcy.\nBłąd: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadDrivers();
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