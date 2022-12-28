module com.example.mvc_gui {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.mvc_gui to javafx.fxml;
    exports com.example.mvc_gui;
}