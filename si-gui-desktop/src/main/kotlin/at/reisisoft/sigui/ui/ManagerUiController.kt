package at.reisisoft.sigui.ui

import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.preferWindowSize
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.layout.Pane
import java.net.URL
import java.util.*

class ManagerUiController : Initializable {

    private lateinit var languageSupport: ResourceBundle
    internal lateinit var settings: SiGuiSetting
        private set

    @FXML
    private lateinit var rootLayout: Pane

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }


    internal fun internalInitialize(settings: SiGuiSetting) {
        this.settings = settings
        //setup
        rootLayout.preferWindowSize()
    }
}