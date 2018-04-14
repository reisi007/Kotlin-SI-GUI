package at.reisisoft.sigui.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        URL uiDescription = MainUI.class.getClassLoader().getResource("mainUI.fxml");
        loader.setLocation(uiDescription);
        Parent mainUi = loader.load();
        primaryStage.setScene(new Scene(mainUi));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}