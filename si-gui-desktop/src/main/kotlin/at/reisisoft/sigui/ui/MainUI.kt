package at.reisisoft.sigui.ui

import at.reisisoft.sigui.settings.loadSettings
import at.reisisoft.ui.*
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Stage
import java.nio.charset.StandardCharsets
import java.util.*

class MainUI : Application() {

    private lateinit var controller: MainUIController


    override fun start(primaryStage: Stage) {
        Thread.currentThread().uncaughtExceptionHandler = UNCAUGHT_EXCEPTION_HANDLER

        val icons = primaryStage.icons
        listOf("16", "32", "48", "64", "72", "96", "144", "192", "512").forEach { it ->
            MainUI::class.java.classLoader.getResource("icons/$it.png")?.openStream()?.use {
                icons += Image(it)
            } ?: throw IllegalStateException("Missing icon of size: ${it}x$it")

        }

        val settings = loadSettings()
        Locale.setDefault(settings.uiLanguage)
        JavaFxUtils.loadFXML("mainUI.fxml").apply {
            loadEncodedRessource("uistrings.sigui-desktop", StandardCharsets.UTF_8).let {
                resources = it!!
                primaryStage.title = it.getString(ResourceKey.APPNAME)
            }
            loadAsParent().let { mainUi ->
                controller = getController()
                controller.setData(settings, primaryStage)
                primaryStage.scene = Scene(mainUi)
                primaryStage.show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            Thread.currentThread().uncaughtExceptionHandler = UNCAUGHT_EXCEPTION_HANDLER
            launch(MainUI::class.java, *args)
        }
    }

    override fun stop() {
        Objects.requireNonNull(controller).close()
        System.exit(0)
    }
}