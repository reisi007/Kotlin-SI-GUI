package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadLocation
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import at.reisisoft.sigui.download.DownloadManager
import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_DAILY
import at.reisisoft.ui.doLocalized
import at.reisisoft.ui.runOnUiThread
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.StringConverter
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.HashMap

class MainUIController : Initializable, AutoCloseable {

    internal lateinit var indicatorUpdateVersions: ProgressIndicator
    internal lateinit var updateListOfVersions: Button
    internal lateinit var downloadAccordion: Accordion
    private lateinit var localisationSupport: ResourceBundle
    internal lateinit var menuBar: MenuBar
    internal lateinit var downloadLocationToNameMap: BiMap<DownloadLocation, String>
    internal lateinit var accordionNameComboBoxMap: Map<String, ComboBox<DownloadInformation>>
    internal lateinit var accordionNameTitlePaneMap: BiMap<String, TitledPane>
    internal lateinit var vBoxUpdate: VBox
    private lateinit var settings: SiGuiSetting

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

    fun updateListOfDownloadVersions(actionEvent: ActionEvent) {
        if (indicatorUpdateVersions.isVisible)
            return
        println("Update version clicked!")
        indicatorUpdateVersions.isVisible = true
        executorService.submit {
            PossibleDownloadHelper.fetchPossibleFor(
                arrayOf(//TODO make variable
                    DownloadType.WINDOWSEXE,
                    DownloadType.WINDOWS32,
                    DownloadType.WINDOWS64
                )
            ).let { updatedListOfVersions ->
                updateAccordion(updatedListOfVersions, storeSettings = true)
                runOnUiThread {
                    indicatorUpdateVersions.isVisible = false
                }
            }
        }
    }

    private fun updateAccordion(
        data: Map<DownloadLocation, Set<DownloadInformation>>,
        currentSelection: Pair<DownloadLocation, DownloadInformation?>? = null,
        storeSettings: Boolean = false
    ) {
        //Get selection data
        val realCurrentSelection = currentSelection
                ?: downloadAccordion.expandedPane.let { expandedPane: TitledPane? ->
                    if (expandedPane == null)
                        downloadLocationToNameMap[DownloadLocation.DAILY]!!
                    else
                        accordionNameTitlePaneMap.inverse()[expandedPane]!!
                }.let { uiString ->
                    val location = downloadLocationToNameMap.inverse()[uiString]!!
                    accordionNameComboBoxMap[uiString]!!.let { choiceBox ->
                        choiceBox.selectionModel.selectedItem.let { selectedItem: DownloadInformation? ->
                            location to selectedItem
                        }
                    }
                }

        val selectedUiString = downloadLocationToNameMap[realCurrentSelection.first]!!
        //Update data

        for (location in data.keys) {
            val uiText = downloadLocationToNameMap.getValue(location)
            accordionNameComboBoxMap[uiText]!!.let {
                /*  it.items.let { items ->
                      items.clear()
                      items.addAll(data.getValue(location))
                  }*/
                runOnUiThread {
                    it.items = FXCollections.observableArrayList(data.getValue(location))
                }
            }
        }
        //Update selection
        downloadAccordion.expandedPane = accordionNameTitlePaneMap[selectedUiString]
        accordionNameComboBoxMap[selectedUiString]!!.let { selectedComboBox ->
            selectedComboBox.selectionModel.let { selectionModel ->
                realCurrentSelection.second.let { selectedDlInfo ->
                    runOnUiThread {
                        if (selectedDlInfo == null)
                            selectionModel.select(0)
                        else
                            selectionModel.select(selectedDlInfo)
                    }
                }
            }
        }
        //Persist settings
        settings.copy(downloadedVersions = data, downloadSelection = realCurrentSelection).let {
            if (storeSettings) {
                it.persist()
                settings = it
            }
        }
    }

    override fun initialize(location: URL, resources: ResourceBundle?) {
        //Add localisation
        localisationSupport = resources ?: throw UnsupportedOperationException("Could not obtain localisation!")
    }

    /**
     * This is called when [setSettings] is called for the first time
     */
    private fun internalInitialize() {
        //Setup accordion
        val tmpChoiceBoxMap: MutableMap<String, ComboBox<DownloadInformation>> = HashMap()
        val tmpTitlePaneMap: BiMap<String, TitledPane> = HashBiMap.create()

        downloadLocationToNameMap = HashBiMap.create<DownloadLocation, String>().also {
            arrayOf(
                DownloadLocation.STABLE to UiStrings.DOWNLOADLIST_NAMED,
                DownloadLocation.DAILY to DOWNLOADLIST_DAILY,
                DownloadLocation.ARCHIVE to UiStrings.DOWNLOADLIST_ARCHIVE
            ).forEach { (dlLocation, key) ->
                localisationSupport.doLocalized(key) { localizedName ->
                    it.put(dlLocation, localizedName)
                    downloadAccordion.panes.apply {
                        add(TitledPane(localizedName, ComboBox<DownloadInformation>().also {
                            tmpChoiceBoxMap.put(localizedName, it)
                            val converter = object : StringConverter<DownloadInformation>() {
                                private val relationMap: MutableMap<String, DownloadInformation?> = HashMap()

                                override fun toString(data: DownloadInformation?): String {
                                    val betterToString = data.toString() //TODO better implementation
                                    relationMap.putIfAbsent(betterToString, data)
                                    return betterToString
                                }

                                override fun fromString(string: String): DownloadInformation? {
                                    return relationMap.getValue(string)
                                }
                            }
                            it.prefWidthProperty().bind(downloadAccordion.widthProperty())
                            it.converter = converter

                        }).also {
                            tmpTitlePaneMap.put(localizedName, it)
                        })
                    }
                }
            }
        }

        accordionNameComboBoxMap = tmpChoiceBoxMap
        accordionNameTitlePaneMap = tmpTitlePaneMap

        updateAccordion(settings.downloadedVersions, settings.downloadSelection, false)

        //Setup update button
        updateListOfVersions.prefWidthProperty().bind(vBoxUpdate.widthProperty())
    }

    private val executorServiceDelegate = lazy {
        Executors.newCachedThreadPool()!!
    }

    private val executorService by executorServiceDelegate

    override fun close() {
        settings.persist()
        if (executorServiceDelegate.isInitialized())
            executorService.shutdownNow()
    }

    fun openOptionMenu(actionEvent: ActionEvent) {
        println("Open Options menu")

        FXMLLoader().let { loader ->
            MainUIController::class.java.classLoader.getResource("optionUI.fxml").let { fxml ->
                loader.resources = localisationSupport
                loader.location = fxml
                val parent: Parent = loader.load()
                val optionController = loader.getController<OptionUIController>()!!
                optionController.settings = settings
                Stage().apply {
                    //TODO https://stackoverflow.com/questions/15041760/javafx-open-new-window
                    title = localisationSupport.getString(UiStrings.OPTIONS_TITLE)
                    scene = Scene(parent)
                }.showAndWait()
                settings = optionController.settings
                executorService.submit { settings.persist() }
            }
        }
    }

    private val downloadManager by lazy { DownloadManager.getInstance(executorService) }
}