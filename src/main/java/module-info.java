module com.example.fleetmanagement {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.fleetmanagement to javafx.fxml;
    exports com.example.fleetmanagement;
}