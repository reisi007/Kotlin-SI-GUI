package at.reisisoft.sigui.ui;

import at.reisisoft.sigui.settings.SettingsKt;
import at.reisisoft.sigui.settings.SiGuiSetting;
import at.reisisoft.ui.JavaFxUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainUI extends Application {

    private MainUIController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setWidth(690);
        primaryStage.setHeight(330);
        primaryStage.setResizable(false);
        SiGuiSetting settings = SettingsKt.loadSettings();
        Locale.setDefault(settings.getUiLanguage());
        FXMLLoader loader = JavaFxUtils.loadFXML("mainUi.fxml");
        ResourceBundle languageSupport = ResourceBundle.getBundle("uistrings.sigui-desktop");
        loader.setResources(languageSupport);
        primaryStage.setTitle(languageSupport.getString(ResourceBundleUtils.INSTANCE.getAPPNAME()));
        Parent mainUi = loader.load();
        controller = loader.getController();
        controller.internalInitialize(settings);
        primaryStage.setScene(new Scene(mainUi));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void stop() {
        Objects.requireNonNull(controller).close();
        System.exit(0);
    }
}