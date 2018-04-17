package at.reisisoft.ui

import javafx.application.Platform

fun runOnUiThread(action: () -> Unit): Unit = Platform.runLater(action)