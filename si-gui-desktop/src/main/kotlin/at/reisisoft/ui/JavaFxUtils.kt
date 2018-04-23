package at.reisisoft.ui

import at.reisisoft.ui.JavaFxUtils.showAlert
import javafx.fxml.FXMLLoader
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea

object JavaFxUtils {
    @JvmStatic
    fun loadFXML(fileName: String): FXMLLoader =
        JavaFxUtils::class.java.classLoader.getResource(fileName).let {
            FXMLLoader().apply { location = it }
        }

    @JvmStatic
    fun showError(exception: Throwable): Unit = exception.stackTraceAsString().let(::showError)

    internal fun showError(errorMessage: String) = showAlert(errorMessage)

    internal fun showAlert(errorMessage: String, alertType: Alert.AlertType = Alert.AlertType.ERROR): Unit =
        runOnUiThread {
            Alert(alertType, errorMessage, ButtonType.OK).apply {
                dialogPane.content =
                        TextArea().apply {
                            text = errorMessage
                        }
            }.showAndWait()
        }
}

internal fun showWarning(warningMessage: String) = showAlert(warningMessage, Alert.AlertType.WARNING)