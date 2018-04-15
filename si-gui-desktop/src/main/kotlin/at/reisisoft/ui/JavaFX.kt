package at.reisisoft.ui

import javafx.application.Platform

object JavaFX {
    fun runOnUiThread(action: () -> Unit): Unit = Platform.runLater(action)
}