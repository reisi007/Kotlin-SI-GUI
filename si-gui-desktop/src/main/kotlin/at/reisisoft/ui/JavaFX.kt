package at.reisisoft.ui


import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ButtonType
import javafx.stage.Stage

fun runOnUiThread(action: () -> Unit): Unit = Platform.runLater(action)

internal fun Button.closeStageOnClick() {
    onAction = EventHandler<ActionEvent> { closeStageOnClickAction()() }
}

internal fun Button.closeStageOnClickAction(): () -> Unit = {
    this.scene.window.let {
        if (it is Stage)
            it.close()
        else throw IllegalStateException()
    }
}

internal fun showError(exception: Throwable): Unit = exception.stackTraceAsString().let(::showError)

internal fun showError(errorMessage: String) = showAlert(errorMessage)

internal fun showAlert(errorMessage: String, alertType: Alert.AlertType = Alert.AlertType.ERROR): Unit = runOnUiThread {
    Alert(alertType, errorMessage, ButtonType.OK).apply { showAndWait() }
}

internal fun showWarning(warningMessage: String) = showAlert(warningMessage, Alert.AlertType.WARNING)