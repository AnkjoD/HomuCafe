package com.homucafe.app;

import java.io.IOException;
import java.util.Objects;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class HomuCafeApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        var view = Objects.requireNonNull(
                getClass().getResource("/com/homucafe/views/welcome.fxml"),
                "Missing welcome.fxml");
        var scene = new Scene(new FXMLLoader(view).load(), 860, 560);
        scene.getStylesheets().add(Objects.requireNonNull(
                getClass().getResource("/com/homucafe/styles/app.css"),
                "Missing app.css").toExternalForm());
        stage.setTitle("HomuCafe");
        stage.setMinWidth(680);
        stage.setMinHeight(440);
        stage.setScene(scene);
        stage.show();
    }
}
