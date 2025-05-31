package com.example.fleetmanagement;

import com.example.fleetmanagement.util.HibernateUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Inicjalizacja Hibernate przy starcie aplikacji
        HibernateUtil.getSessionFactory();

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