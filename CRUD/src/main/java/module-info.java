module org.meta1.crud {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens org.meta1.crud to javafx.fxml;
    exports org.meta1.crud;
}