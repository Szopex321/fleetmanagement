package com.example.fleetmanagement;

import com.example.fleetmanagement.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Inicjalizacja Hibernate przy starcie aplikacji
        HibernateUtil.getSessionFactory();

        Alert toDoAlert = new Alert(Alert.AlertType.INFORMATION);
        toDoAlert.setTitle("Informacja Rozwojowa");
        toDoAlert.setHeaderText("Lista rzeczy do zrobienia / Uwagi:");
        toDoAlert.setContentText(
                "- Dokończyć walidację danych we wszystkich formularzach.\n" +
                        "- wyswietlac wieksza ilosc rzeczy w widokach.\n" +
                        "- przeniec wyjatki do osobnych klas.\n" +
                        "- Przygotować kompletną dokumentację techniczną."
        );

        toDoAlert.showAndWait();

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("MainView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        stage.setTitle("System Zarządzania Flotą");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            HibernateUtil.shutdown(); // Zamknięcie SessionFactory przy zamykaniu aplikacji
            System.out.println("Aplikacja zamknięta, zasoby Hibernate zwolnione.");
        });
    }

    public static void main(String[] args) {
        launch();
    }
}