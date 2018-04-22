package at.reisisoft.ui


import javafx.application.Platform
import javafx.beans.binding.DoubleExpression
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import javafx.stage.Window
import java.nio.file.Path
import java.nio.file.Paths

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

internal fun Window.showDirectoryChooser(titleString: String, initalPath: Path = Paths.get(".")): Path? =
    DirectoryChooser().apply {
        title = titleString
        initialDirectory = initalPath.toFile()
    }.showDialog(this)?.toPath()

internal fun Window.showFileChooser(
    titleString: String,
    initialPath: Path,
    vararg extensionFilers: FileChooser.ExtensionFilter
): Path? = FileChooser().apply {
    title = titleString
    initialDirectory = initialPath.toFile()
    this.extensionFilters.addAll(*extensionFilers)
}.showOpenDialog(this)?.toPath()

operator fun DoubleExpression.times(double: Double): DoubleExpression = this.multiply(double)
operator fun DoubleExpression.times(int: Int): DoubleExpression = this.multiply(int)

internal fun Pane.preferWindowSize() {
    sceneProperty().addListener { _, _, scene ->
        prefWidthProperty().bind(scene.widthProperty())
        prefHeightProperty().bind(scene.heightProperty())
    }
}

internal fun Label.addDefaultTooltip() {
    tooltip = Tooltip().also {
        it.textProperty().bind(textProperty())
        it.font = Font(it.font.name, 12.0)
        // Cannot change delay in Java 8
    }
}