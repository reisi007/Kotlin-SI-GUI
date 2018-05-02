package at.reisisoft.sigui.ui;

import at.reisisoft.sigui.settings.SettingsKt;
import at.reisisoft.sigui.settings.SiGuiSetting;
import at.reisisoft.ui.JavaFXKt;
import at.reisisoft.ui.JavaFxUtils;
import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import kotlin.collections.CollectionsKt;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class MainUI extends Application {

    private MainUIController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Thread.currentThread().setUncaughtExceptionHandler(JavaFXKt.getUNCAUGHT_EXCEPTION_HANDLER());

        ObservableList<Image> icons = primaryStage.getIcons();
        CollectionsKt.listOf("48", "72", "96", "144", "192").forEach(it -> {
            try (InputStream icon = MainUI.class.getClassLoader().getResource("icons/" + it + ".png").openStream()) {
                icons.add(new Image(icon));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });

        SiGuiSetting settings = SettingsKt.loadSettings();
        Locale.setDefault(settings.getUiLanguage());
        FXMLLoader loader = JavaFxUtils.loadFXML("mainUI.fxml");
        ResourceBundle languageSupport = JavaFXKt.loadEncodedRessource("uistrings.sigui-desktop", StandardCharsets.UTF_8);
        loader.setResources(languageSupport);
        primaryStage.setTitle(languageSupport.getString(ResourceKey.APPNAME.toString()));
        Parent mainUi = loader.load();
        controller = loader.getController();
        controller.internalInitialize(settings, primaryStage);
        primaryStage.setScene(new Scene(mainUi));
        primaryStage.show();
    }

    public static void main(String[] args) {
        Thread.currentThread().setUncaughtExceptionHandler(JavaFXKt.getUNCAUGHT_EXCEPTION_HANDLER());
        launch(args);
    }

    @Override
    public void stop() {
        Objects.requireNonNull(controller).close();
        System.exit(0);
    }
}