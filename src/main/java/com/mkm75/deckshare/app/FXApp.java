package com.mkm75.deckshare.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;

import java.util.Objects;

public class FXApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        try {
            SplitPane pane = FXMLLoader.load(Objects.requireNonNull(FXApp.class.getResource("main.fxml")));
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.setTitle("DeckShare v1.0 by mkm75 - Targeting for Puzzline v1.6.x");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
