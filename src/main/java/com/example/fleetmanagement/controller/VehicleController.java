package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.MainApp; // Potrzebne do ładowania FXML dialogu
import com.example.fleetmanagement.dao.VehicleDao;
import com.example.fleetmanagement.model.Vehicle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.io.IOException;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class VehicleController {

    @FXML private TableView<Vehicle> vehicleTable;
    @FXML private TableColumn<Vehicle, Long> idColumn;
    @FXML private TableColumn<Vehicle, String> makeColumn;
    @FXML private TableColumn<Vehicle, String> modelColumn;
    @FXML private TableColumn<Vehicle, String> regNumberColumn;
    @FXML private TableColumn<Vehicle, Integer> yearColumn;

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
    }

    private void loadVehicles() {
        vehicleList = FXCollections.observableArrayList(vehicleDao.findAll());
        vehicleTable.setItems(vehicleList);
    }

    @FXML
    private void handleAddVehicle() {
        try {
            // Załaduj FXML dialogu
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("dialog/VehicleDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            // Pobierz referencje do kontrolek z załadowanego FXML
            TextField makeFieldDialog = (TextField) dialogPaneContent.lookup("#makeFieldDialog");
            TextField modelFieldDialog = (TextField) dialogPaneContent.lookup("#modelFieldDialog");
            TextField regNumberFieldDialog = (TextField) dialogPaneContent.lookup("#regNumberFieldDialog");
            Spinner<Integer> yearSpinnerDialog = (Spinner<Integer>) dialogPaneContent.lookup("#yearSpinnerDialog");

            // Utwórz dialog
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Dodaj Nowy Pojazd");
            dialog.getDialogPane().setContent(dialogPaneContent);

            // Dodaj przyciski OK i Anuluj
            ButtonType okButtonType = new ButtonType("Zapisz", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // Pokaż dialog i poczekaj na odpowiedź
            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(makeFieldDialog, modelFieldDialog, regNumberFieldDialog)) {
                    Vehicle newVehicle = new Vehicle(
                            makeFieldDialog.getText(),
                            modelFieldDialog.getText(),
                            regNumberFieldDialog.getText(),
                            yearSpinnerDialog.getValue()
                    );
                    try {
                        vehicleDao.save(newVehicle);
                        loadVehicles(); // Odśwież listę
                    } catch (Exception e) {
                        showError("Błąd dodawania pojazdu", "Nie udało się dodać pojazdu. Sprawdź czy numer rejestracyjny nie jest już zajęty.\n" + e.getMessage());
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna dialogowego.");
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
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("dialog/VehicleDialog.fxml"));
            GridPane dialogPaneContent = loader.load();

            TextField makeFieldDialog = (TextField) dialogPaneContent.lookup("#makeFieldDialog");
            TextField modelFieldDialog = (TextField) dialogPaneContent.lookup("#modelFieldDialog");
            TextField regNumberFieldDialog = (TextField) dialogPaneContent.lookup("#regNumberFieldDialog");
            Spinner<Integer> yearSpinnerDialog = (Spinner<Integer>) dialogPaneContent.lookup("#yearSpinnerDialog");

            // Ustaw aktualne wartości w dialogu
            makeFieldDialog.setText(selectedVehicle.getMake());
            modelFieldDialog.setText(selectedVehicle.getModel());
            regNumberFieldDialog.setText(selectedVehicle.getRegistrationNumber());
            if (selectedVehicle.getProductionYear() != null) {
                yearSpinnerDialog.getValueFactory().setValue(selectedVehicle.getProductionYear());
            } else {
                yearSpinnerDialog.getValueFactory().setValue(java.time.LocalDate.now().getYear()); // Domyślna wartość, jeśli null
            }


            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Edytuj Pojazd");
            dialog.getDialogPane().setContent(dialogPaneContent);

            ButtonType okButtonType = new ButtonType("Zapisz Zmiany", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            Optional<ButtonType> result = dialog.showAndWait();

            if (result.isPresent() && result.get() == okButtonType) {
                if (validateDialogInput(makeFieldDialog, modelFieldDialog, regNumberFieldDialog)) {
                    selectedVehicle.setMake(makeFieldDialog.getText());
                    selectedVehicle.setModel(modelFieldDialog.getText());
                    selectedVehicle.setRegistrationNumber(regNumberFieldDialog.getText());
                    selectedVehicle.setProductionYear(yearSpinnerDialog.getValue());
                    try {
                        vehicleDao.update(selectedVehicle);
                        loadVehicles(); // Odśwież listę
                    } catch (Exception e) {
                        showError("Błąd edycji pojazdu", "Nie udało się zaktualizować pojazdu.\n" + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Błąd aplikacji", "Nie udało się otworzyć okna dialogowego.");
        }
    }

    private boolean validateDialogInput(TextField makeField, TextField modelField, TextField regNumberField) {
        String errorMessage = "";
        if (makeField.getText() == null || makeField.getText().trim().isEmpty()) {
            errorMessage += "Marka nie może być pusta.\n";
        }
        if (modelField.getText() == null || modelField.getText().trim().isEmpty()) {
            errorMessage += "Model nie może być pusty.\n";
        }
        if (regNumberField.getText() == null || regNumberField.getText().trim().isEmpty()) {
            errorMessage += "Numer rejestracyjny nie może być pusty.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Błąd walidacji danych");
            alert.setHeaderText(null);
            alert.setContentText(errorMessage);
            alert.showAndWait();
            return false;
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
                loadVehicles(); // Odśwież listę
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