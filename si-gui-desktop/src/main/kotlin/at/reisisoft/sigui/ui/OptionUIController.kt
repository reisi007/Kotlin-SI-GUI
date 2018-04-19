package at.reisisoft.sigui.ui

import at.reisisoft.sigui.OSUtils
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.settings.SiGuiSetting
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.layout.FlowPane
import javafx.stage.Stage
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OptionUIController : Initializable {

    private lateinit var languageSupport: ResourceBundle

    internal lateinit var settings: SiGuiSetting
        private set

    internal lateinit var closeButton: Button

    internal lateinit var optionHolder: FlowPane

    internal lateinit var downloadTypesSelection: CheckComboBox<DownloadType>


    internal fun internalSetSettings(newSettings: SiGuiSetting) {
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
        downloadTypesSelection.items.addAll(FXCollections.observableArrayList(OSUtils.CURRENT_OS.downloadTypesForOS()))

        downloadTypesSelection.checkModel.let { selectionModel ->
            settings.downloadTypes.forEach { selectionModel.check(it) }
        }

        downloadTypesSelection.converter = object : StringConverter<DownloadType>() {
            private val nameElementMap: MutableMap<String, DownloadType?> = HashMap()

            override fun toString(data: DownloadType?): String {

                val stringVal = data?.let {
                    languageSupport.getString("options.downloadtypes.${it.toString().toLowerCase()}")
                } ?: "null"
                nameElementMap.computeIfAbsent(stringVal, { data })
                return stringVal
            }

            override fun fromString(string: String): DownloadType? = nameElementMap[string]

        }

    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }

    fun updateSettings() {
        //Get all settings and update them -> store at the end
        settings = settings.let {
            ArrayList(downloadTypesSelection.checkModel.checkedItems).let { selectedDownloadTypes ->
                it.copy(downloadTypes = selectedDownloadTypes)
            }
        }
    }

    fun onCloseRequested(actionEvent: ActionEvent) {
        closeButton.scene.window.let {
            if (it is Stage)
                it.close()
            else throw IllegalStateException()
        }
    }
}