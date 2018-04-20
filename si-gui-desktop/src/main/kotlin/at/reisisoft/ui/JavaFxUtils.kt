package at.reisisoft.ui

import javafx.fxml.FXMLLoader

object JavaFxUtils {
    @JvmStatic
    fun loadFXML(fileName: String): FXMLLoader =
        JavaFxUtils::class.java.classLoader.getResource(fileName).let {
            FXMLLoader().apply { location = it }
        }
}