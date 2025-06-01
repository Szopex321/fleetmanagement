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
import java.util.Optional;

public class DriverController {

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, Long> idColumn;
    @FXML private TableColumn<Driver, String> firstNameColumn;
    @FXML private TableColumn<Driver, String> lastNameColumn;
    @FXML private TableColumn<Driver, String> licenseNumberColumn;

    private DriverDao driverDao;
    private ObservableList<Driver> driverList;

    public void initialize() {
        driverDao = new DriverDao();

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        licenseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));

        loadDrivers();
    }

    private void loadDrivers() {
        driverList = FXCollections.observableArrayList(driverDao.findAll());
        driverTable.setItems(driverList);
    }

    @FXML
    private void handleAddDriver() {
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("dialog/DriverDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            TextField firstNameFieldDialog = (TextField) dialogPaneContent.lookup("#firstNameFieldDialog");
            TextField lastNameFieldDialog = (TextField) dialogPaneContent.lookup("#lastNameFieldDialog");
            TextField licenseNumberFieldDialog = (TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog");
            firstNameFieldDialog.requestFocus();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowego Kierowcę");
            dialog.getDialogPane().setContent(dialogPaneContent);

            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(firstNameFieldDialog, lastNameFieldDialog, licenseNumberFieldDialog)) {
                    Driver newDriver = new Driver(
                            firstNameFieldDialog.getText(),
                            lastNameFieldDialog.getText(),
                            licenseNumberFieldDialog.getText()
                    );
                    try {
                        driverDao.save(newDriver);
                        loadDrivers();
                    } catch (Exception e) {
                        showError("Błąd dodawania kierowcy", "Nie udało się dodać kierowcy. Sprawdź czy numer prawa jazdy nie jest już zajęty.\nBłąd: " + e.getMessage());
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
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("dialog/DriverDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            TextField firstNameFieldDialog = (TextField) dialogPaneContent.lookup("#firstNameFieldDialog");
            TextField lastNameFieldDialog = (TextField) dialogPaneContent.lookup("#lastNameFieldDialog");
            TextField licenseNumberFieldDialog = (TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog");

            firstNameFieldDialog.setText(selectedDriver.getFirstName());
            lastNameFieldDialog.setText(selectedDriver.getLastName());
            licenseNumberFieldDialog.setText(selectedDriver.getLicenseNumber());
            firstNameFieldDialog.requestFocus();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edytuj Dane Kierowcy");
            dialog.getDialogPane().setContent(dialogPaneContent);

            ButtonType okButtonType = new ButtonType("Zapisz Zmiany", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
            dialog.setResizable(true);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(firstNameFieldDialog, lastNameFieldDialog, licenseNumberFieldDialog)) {
                    selectedDriver.setFirstName(firstNameFieldDialog.getText());
                    selectedDriver.setLastName(lastNameFieldDialog.getText());
                    selectedDriver.setLicenseNumber(licenseNumberFieldDialog.getText());
                    try {
                        driverDao.update(selectedDriver);
                        loadDrivers();
                    } catch (Exception e) {
                        showError("Błąd edycji kierowcy", "Nie udało się zaktualizować danych kierowcy.\nBłąd: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna dialogowego: " + e.getMessage());
        }
    }

    private boolean validateDialogInput(TextField firstNameField, TextField lastNameField, TextField licenseNumberField) {
        String errorMessage = "";
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMessage += "Imię jest wymagane.\n";
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMessage += "Nazwisko jest wymagane.\n";
        }
        if (licenseNumberField.getText() == null || licenseNumberField.getText().trim().isEmpty()) {
            errorMessage += "Numer prawa jazdy jest wymagany.\n";
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