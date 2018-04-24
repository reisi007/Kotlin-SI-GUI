package at.reisisoft.sigui.ui

import at.reisisoft.sigui.OSUtils
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.*
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.FlowPane
import javafx.scene.layout.Pane
import javafx.util.StringConverter
import org.controlsfx.control.CheckComboBox
import java.net.URL
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class OptionUIController : Initializable {

    private lateinit var languageSupport: ResourceBundle

    internal lateinit var settings: SiGuiSetting
        private set

    @FXML
    private lateinit var closeButton: Button
    @FXML
    private lateinit var optionHolder: FlowPane
    @FXML
    private lateinit var downloadTypesSelection: CheckComboBox<DownloadType>
    @FXML
    private lateinit var helppackLanguages: ComboBox<Locale>
    @FXML
    private lateinit var updateHelppackLanguages: Button
    @FXML
    private lateinit var uiLang: ComboBox<Locale>
    @FXML
    private lateinit var downloadFolderText: Label
    @FXML
    private lateinit var downloadFolderButton: Button
    @FXML
    private lateinit var installFolderText: Label
    @FXML
    private lateinit var installFolderButton: Button
    @FXML
    private lateinit var installFolderLabel: Label
    @FXML
    private lateinit var downloadFolderLabel: Label
    @FXML
    private lateinit var rootLayout: Pane
    @FXML
    private lateinit var shortcutCreationEnabled: CheckBox
    @FXML
    private lateinit var shortcutCreationText: Label
    @FXML
    private lateinit var shortcutCreationButton: Button
    @FXML
    private lateinit var shortcutCreationLabel: Label

    internal fun internalInitialize(newSettings: SiGuiSetting, executorService: ExecutorService) {
        if (!::settings.isInitialized) {
            settings = newSettings
            try {
                internalInitialize(executorService)
            } catch (t: Throwable) {
                println("Error on initialize!")
                t.printStackTrace()
            }
        }
    }

    private fun internalInitialize(executorService: ExecutorService) {
        rootLayout.preferWindowSize()

        arrayOf(installFolderText, shortcutCreationText, downloadFolderText).forEach {
            it.addDefaultTooltip()
        }

        closeButton.closeStageOnClick()
        //Setup download tyes
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
        //Setup helppack language
        helppackLanguages.items.addAll(settings.availableHpLanguages)

        helppackLanguages.selectionModel.apply {
            select(settings.hpLanguage)
            if (isEmpty)
                selectFirst()
        }

        updateHelppackLanguages.onAction = EventHandler<ActionEvent> {
            println("Refresh helppack languages clicked!")
            executorService.submit {
                PossibleDownloadHelper.getHelppackLanguages().let { hpLanguages ->
                    settings = settings.copy(availableHpLanguages = hpLanguages)
                    helppackLanguages.selectionModel.let { selectionModel ->
                        runOnUiThread {
                            selectionModel.selectedItem.let { selectedItem: Locale? ->
                                helppackLanguages.items.let {
                                    it.clear()
                                    it.addAll(hpLanguages)
                                    selectionModel.select(selectedItem)
                                    if (selectionModel.isEmpty) {
                                        selectionModel.selectFirst()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Setup UI language
        getSupportedLanguages().let {
            uiLang.items.addAll(it)
            val selectLanguage = if (it.contains(settings.uiLanguage))
                settings.uiLanguage
            else Locale.forLanguageTag(EN_US)
            uiLang.selectionModel.select(selectLanguage)
        }

        //Setup download location
        downloadFolderButton.onAction = EventHandler {
            downloadFolderButton.scene.window.showDirectoryChooser(
                languageSupport.getString(ResourceKey.OPTIONS_OPENFOLDER),
                Paths.get(downloadFolderText.text)
            )?.let { path ->
                downloadFolderText.text = path.toString()
            }
        }

        downloadFolderText.text = settings.downloadFolder.toString()
        downloadFolderLabel.text = languageSupport.getString(ResourceKey.OPTIONS_DOWNLOADFOLDER)

        //Setup installation folder
        installFolderButton.onAction = EventHandler {
            installFolderButton.scene.window.showDirectoryChooser(
                languageSupport.getString(ResourceKey.OPTIONS_OPENFOLDER),
                Paths.get(installFolderText.text)
            )?.let { path ->
                installFolderText.text = path.toString()
            }
        }

        installFolderText.text = settings.rootInstallationFolder.toString()
        installFolderLabel.text = languageSupport.getString(ResourceKey.OPTIONS_ROOTINSTALLFOLDER)

        //Sortcut creation
        shortcutCreationEnabled.isSelected = settings.createDesktopShortCut
        shortcutCreationText.text = settings.shortcutDir.toString()
    }

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }

    /**
     * Always update this function if settings are added. This functions get all settings from the UI.
     *
     * Updates the stored settings
     */
    fun updateSettings() {
        //Get all settings and update them -> store at the end
        settings = settings.let {
            ArrayList(downloadTypesSelection.checkModel.checkedItems).let { selectedDownloadTypes ->
                uiLang.selectionModel.selectedItem.let { selectedUiLanguage ->
                    helppackLanguages.selectionModel.selectedItem.let { selectedHpLanguage ->
                        Paths.get(downloadFolderText.text).let { newDownloadFolder ->
                            Paths.get(installFolderText.text).let { newInstallationFolder ->
                                Paths.get(shortcutCreationText.text).let { newShortcutFolder ->
                                    shortcutCreationEnabled.isSelected.let { isCreaterShortcut ->
                                        it.copy(
                                            downloadTypes = selectedDownloadTypes,
                                            hpLanguage = selectedHpLanguage,
                                            uiLanguage = selectedUiLanguage,
                                            intDownloadFolder = newDownloadFolder,
                                            intRootInstallationFolder = newInstallationFolder,
                                            createDesktopShortCut = isCreaterShortcut,
                                            intShortcutDir = newShortcutFolder
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}