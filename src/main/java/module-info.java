module com.example.fleetmanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.hibernate.orm.core; // Hibernate
    requires java.sql;              // JDBC
    requires jakarta.persistence;     // JPA API (transitive from Hibernate)
    requires java.naming;             // <<< DODAJ TO

    // Otwórz pakiety dla refleksji przez Hibernate i JavaFX
    opens com.example.fleetmanagement.model to org.hibernate.orm.core, javafx.base;
    opens com.example.fleetmanagement.controller to javafx.fxml;
    opens com.example.fleetmanagement to javafx.fxml; // Jeśli MainApp jest w tym pakiecie i używa FXML

    exports com.example.fleetmanagement;
}