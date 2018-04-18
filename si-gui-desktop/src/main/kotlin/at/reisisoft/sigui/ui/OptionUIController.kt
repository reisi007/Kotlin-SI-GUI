package at.reisisoft.sigui.ui

import at.reisisoft.sigui.settings.SiGuiSetting
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import java.net.URL
import java.util.*

class OptionUIController : Initializable, AutoCloseable {

    private lateinit var translation: ResourceBundle

    internal lateinit var settings: SiGuiSetting

    internal lateinit var closeButton: Button

    internal lateinit var optionHolder: FlowPane

    @JvmName("internalSetSettings")
    internal fun setSettings(newSettings: SiGuiSetting) {
        if (!::settings.isInitialized) {
            settings = newSettings
            try {
                internalInitialize()
            } catch (t: Throwable) {
                println("Error on initialize!")
                t.printStackTrace()
            }
        }
    }

    private fun internalInitialize() {

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        translation = resources!!
    }

    override fun close() {
        //Nothing to close ATM
    }

    fun onCloseRequested(actionEvent: ActionEvent) {
        closeButton.scene.window.let {
            if (it is Stage)
                it.close()
            else throw IllegalStateException()
        }
    }
}