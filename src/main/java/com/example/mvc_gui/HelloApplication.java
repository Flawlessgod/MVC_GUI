package com.example.mvc_gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        MainUI uiRoot = new MainUI();
        Scene scene = new Scene(uiRoot);
        stage.setTitle("MVC_GUI");
        stage.setScene(scene);
        stage.show();
        uiRoot.requestFocus();
    }

    public static void main(String[] args) {
        launch();
    }
}