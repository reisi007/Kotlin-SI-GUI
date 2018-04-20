package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.LibreOfficeDownloadFileType
import at.reisisoft.ui.closeStageOnClick
import at.reisisoft.ui.closeStageOnClickAction
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import java.net.URL
import java.util.*

class DownloadUiConroller : Initializable {

    private lateinit var languageSupport: ResourceBundle

    val downloads: EnumMap<LibreOfficeDownloadFileType, String> =
        EnumMap(LibreOfficeDownloadFileType::class.java)

    private var alreadyInitialized = false

    @FXML
    private lateinit var abort: Button
    @FXML
    private lateinit var startDl: Button

    fun setDownloads(downloads: Map<LibreOfficeDownloadFileType, String>) {
        if (alreadyInitialized)
            throw IllegalStateException("Controler has already been initialized")
        alreadyInitialized = true
        this.downloads.putAll(downloads)
        internalInitialize()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }

    private fun internalInitialize() {
        startDl.closeStageOnClick()
        abort.onAction = EventHandler<ActionEvent> {
            downloads.clear()
            abort.closeStageOnClickAction()()
        }
    }
}