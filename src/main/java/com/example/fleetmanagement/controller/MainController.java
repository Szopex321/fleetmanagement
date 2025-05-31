package com.example.fleetmanagement.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    @FXML
    private TabPane tabPane;

    @FXML
    public void initialize() {
        loadTab("Pojazdy", "/com/example/fleetmanagement/VehicleView.fxml");
        loadTab("Kierowcy", "/com/example/fleetmanagement/DriverView.fxml");
        loadTab("Przypisania", "/com/example/fleetmanagement/AssignmentView.fxml");
    }

    private void loadTab(String title, String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource(fxmlPath)));
            Pane content = loader.load();
            Tab tab = new Tab(title);
            tab.setContent(content);
            tabPane.getTabs().add(tab);
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Błąd ładowania widoku");
            errorAlert.setHeaderText("Nie udało się załadować zakładki: " + title);
            errorAlert.setContentText("Ścieżka FXML: " + fxmlPath + "\nBłąd: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }
}