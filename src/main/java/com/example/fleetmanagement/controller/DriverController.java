package com.example.fleetmanagement.controller;

import com.example.fleetmanagement.MainApp;
import com.example.fleetmanagement.dao.DriverDao;
import com.example.fleetmanagement.model.Driver;
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
import java.util.regex.Pattern;

public class DriverController {

    @FXML private TableView<Driver> driverTable;
    @FXML private TableColumn<Driver, String> firstNameColumn;
    @FXML private TableColumn<Driver, String> lastNameColumn;
    @FXML private TableColumn<Driver, String> licenseNumberColumn;
    @FXML private TableColumn<Driver, String> statusTableColumn;
    @FXML private TableColumn<Driver, String> phoneTableColumn;

    @FXML private Button showDriverDetailsButton;
    @FXML private Button editDriverButton;
    @FXML private Button deleteDriverButton;

    private DriverDao driverDao;
    private ObservableList<Driver> driverList;

    private final ObservableList<String> driverStatuses = FXCollections.observableArrayList(
            "Aktywny", "Na urlopie", "Zwolnienie lekarskie", "Szkolenie", "Niedostępny", "Zwolniony"
    );

//            (\\+?\\d{1,3}[- ]?)?: To jest opcjonalna grupa (ze względu na ? na końcu) dla kodu kraju.
//            \\+?: Opcjonalny znak + (plus) na początku kodu kraju. \\ jest znakiem ucieczki dla +, a ? czyni go opcjonalnym.
//            \\d{1,3}: Dopasowuje od jednej do trzech cyfr (\\d to cyfra, {1,3} to kwantyfikator "od 1 do 3 razy"). To jest kod kraju.
//            [- ]?: Opcjonalny separator po kodzie kraju – myślnik LUB spacja. [] definiuje zestaw znaków, a ? czyni go opcjonalnym.
//            \\d{3}: Dopasowuje dokładnie trzy cyfry (pierwsza część numeru lokalnego).
//            [- ]?: Znowu opcjonalny separator (myślnik lub spacja).
//            \\d{3}: Dopasowuje kolejne trzy cyfry.
//            [- ]?: I znowu opcjonalny separator.
//            \\d{3}: Dopasowuje ostatnie trzy cyfry.
//            $: Dopasowuje koniec ciągu (linii). Oznacza, że po ostatniej grupie cyfr nic więcej nie może wystąpić.
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{1,3}[- ]?)?\\d{3}[- ]?\\d{3}[- ]?\\d{3}$|^\\d{9}$");

//          : Początek ciągu.
//          [a-zA-Z0-9._%+-]+: Dopasowuje część lokalną adresu email (to co przed znakiem @).
//          [a-zA-Z0-9._%+-]: Zestaw dozwolonych znaków. Obejmuje:
//          a-z: małe litery.
//          A-Z: duże litery.
//          0-9: cyfry.
//          ._%+-: kropka, podkreślenie, procent, plus, myślnik.
//          +: Kwantyfikator oznaczający "jedno lub więcej wystąpień" poprzedzającego zestawu znaków. Część lokalna nie może być pusta.
//          @: Dopasowuje dosłownie znak @ (małpa), który oddziela część lokalną od domeny.
//          [a-zA-Z0-9.-]+: Dopasowuje nazwę domeny (bez domeny najwyższego poziomu, np. example w example.com).
//          [a-zA-Z0-9.-]: Zestaw dozwolonych znaków dla nazwy domeny. Obejmuje litery, cyfry, kropkę i myślnik.
//          +: Nazwa domeny nie może być pusta.
//          \\.: Dopasowuje dosłownie znak . (kropka), który oddziela nazwę domeny od domeny najwyższego poziomu. \ jest znakiem ucieczki dla ., ponieważ . w regexie ma specjalne znaczenie (dopasowuje dowolny znak).
//          [a-zA-Z]{2,6}: Dopasowuje domenę najwyższego poziomu (TLD), np. com, org, pl, info.
//          [a-zA-Z]: Tylko litery (małe lub duże).
//          {2,6}: Kwantyfikator oznaczający "od dwóch do sześciu wystąpień" poprzedzającego zestawu znaków. Oznacza to, że TLD musi mieć od 2 do 6 liter. (Uwaga: istnieją TLD dłuższe niż 6 znaków, np. .museum, .travel, a także TLD z cyframi lub IDN, więc ten fragment nie jest w pełni uniwersalny dla wszystkich możliwych TLD, ale jest często spotykany).
//          $: Koniec ciągu.
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$");

    public void initialize() {
        driverDao = new DriverDao();

        // Konfiguracja kolumn tabeli dla kierowców.
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        licenseNumberColumn.setCellValueFactory(new PropertyValueFactory<>("licenseNumber"));
        statusTableColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        phoneTableColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));

        loadDrivers();

        // Listener aktywacji/dezaktywacji przycisków na podstawie zaznaczenia w tabeli.
        driverTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            booleanItemSelected = (newSelection != null);
            showDriverDetailsButton.setDisable(!booleanItemSelected);
            if (editDriverButton != null) editDriverButton.setDisable(!booleanItemSelected);
            if (deleteDriverButton != null) deleteDriverButton.setDisable(!booleanItemSelected);
        });
        showDriverDetailsButton.setDisable(true);
        if (editDriverButton != null) editDriverButton.setDisable(true);
        if (deleteDriverButton != null) deleteDriverButton.setDisable(true);
    }
    private boolean booleanItemSelected = false;

    private void loadDrivers() {
        driverList = FXCollections.observableArrayList(driverDao.findAll());
        driverTable.setItems(driverList);
        driverTable.getSelectionModel().clearSelection();
    }

    private void setupDialogControls(GridPane dialogPaneContent, Driver driverToEdit) {
        // Pobranie referencji do kontrolek z DriverDialog.fxml za pomocą lookup().
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

        if (driverToEdit != null) {  // Jeśli edycja/szczegóły (obiekt Driver przekazany)
            // Wypełnianie pól formularza danymi z obiektu driverToEdit.
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
        } else { // Jeśli dodawanie nowego kierowcy
            // Ustawianie wartości domyślnych.
            statusComboBox.setValue("Aktywny");
            employmentDatePicker.setValue(LocalDate.now());
            firstNameField.requestFocus();
        }
    }

    private void updateDriverFromDialog(Driver driver, GridPane dialogPaneContent) {
        // Pobieranie wartości z kontrolek dialogu i ustawianie ich w obiekcie `driver`.
        driver.setFirstName(((TextField) dialogPaneContent.lookup("#firstNameFieldDialog")).getText());
        driver.setLastName(((TextField) dialogPaneContent.lookup("#lastNameFieldDialog")).getText());
        driver.setLicenseNumber(((TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog")).getText());

        String phone = ((TextField) dialogPaneContent.lookup("#phoneNumberFieldDialog")).getText();
        driver.setPhoneNumber( (phone!=null && !phone.trim().isEmpty()) ? phone.trim() : null );

        String email = ((TextField) dialogPaneContent.lookup("#emailFieldDialog")).getText();
        driver.setEmail( (email!=null && !email.trim().isEmpty()) ? email.trim() : null );

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
        // Pobranie kontrolek do walidacji.
        TextField firstNameField = (TextField) dialogPaneContent.lookup("#firstNameFieldDialog");
        TextField lastNameField = (TextField) dialogPaneContent.lookup("#lastNameFieldDialog");
        TextField licenseNumberField = (TextField) dialogPaneContent.lookup("#licenseNumberFieldDialog");
        TextField phoneNumberField = (TextField) dialogPaneContent.lookup("#phoneNumberFieldDialog");
        TextField emailField = (TextField) dialogPaneContent.lookup("#emailFieldDialog");
        ComboBox<String> statusComboBox = (ComboBox<String>) dialogPaneContent.lookup("#statusComboBoxDialog");

        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) errorMessage.append("Imię jest wymagane.\n");
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) errorMessage.append("Nazwisko jest wymagane.\n");
        if (licenseNumberField.getText() == null || licenseNumberField.getText().trim().isEmpty()) errorMessage.append("Numer prawa jazdy jest wymagany.\n");
        if (statusComboBox.getValue() == null || statusComboBox.getValue().trim().isEmpty()) errorMessage.append("Status kierowcy jest wymagany.\n");

        String phone = phoneNumberField.getText();
        if (phone != null && !phone.trim().isEmpty() && !PHONE_PATTERN.matcher(phone.trim()).matches()) {
            errorMessage.append("Niepoprawny format numeru telefonu.\n");
        }
        String email = emailField.getText();
        if (email != null && !email.trim().isEmpty() && !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            errorMessage.append("Niepoprawny format adresu email.\n");
        }

        if (errorMessage.length() > 0) {
            showError("Błąd walidacji danych",errorMessage.toString());
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
                    Driver newDriver = new Driver();
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
    private void handleShowDriverDetails() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(MainApp.class.getResource("Dialog/DriverDialog.fxml"));
            GridPane dialogPaneContent = loader.load();
            setupDialogControls(dialogPaneContent, selectedDriver);
            setDialogControlsReadOnly(dialogPaneContent, true);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Szczegóły Kierowcy: " + selectedDriver.getFirstName() + " " + selectedDriver.getLastName());
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
    private void handleDeleteDriver() {
        Driver selectedDriver = driverTable.getSelectionModel().getSelectedItem();
        if (selectedDriver == null) {
            showAlert("Brak zaznaczenia", "Proszę zaznaczyć kierowcę do usuniecia.");
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