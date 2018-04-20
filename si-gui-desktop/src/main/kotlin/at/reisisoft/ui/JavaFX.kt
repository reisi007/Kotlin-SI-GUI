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

internal fun showAlert(exception: Throwable): Unit = runOnUiThread {
    Alert(Alert.AlertType.ERROR, exception.stackTraceAsString(), ButtonType.OK).apply { showAndWait() }
}