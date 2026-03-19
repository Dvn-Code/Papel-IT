module com.example.papelit {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;
    requires javafx.web;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires org.slf4j;
    
    opens com.example.papelit to javafx.fxml;
    opens com.example.papelit.ui to javafx.fxml;
    
    exports com.example.papelit;
    exports com.example.papelit.ui;
    exports com.example.papelit.model;
}
