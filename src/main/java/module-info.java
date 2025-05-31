module com.example.fleetmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core;
    requires java.sql;
    requires jakarta.persistence;
    requires java.naming;

    opens com.example.fleetmanagement.model to org.hibernate.orm.core, javafx.base;
    opens com.example.fleetmanagement.controller to javafx.fxml;
    opens com.example.fleetmanagement to javafx.fxml;

    exports com.example.fleetmanagement;
}