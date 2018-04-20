package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.*
import at.reisisoft.sigui.download.DownloadManager
import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.doLocalized
import at.reisisoft.ui.getReplacedString
import at.reisisoft.ui.runOnUiThread
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import javafx.event.ActionEvent
import javafx.fxml.FXML
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

    @FXML
    private lateinit var indicatorUpdateVersions: ProgressIndicator
    @FXML
    private lateinit var updateListOfVersions: Button
    @FXML
    private lateinit var downloadAccordion: Accordion
    private lateinit var localisationSupport: ResourceBundle
    @FXML
    private lateinit var menuBar: MenuBar
    @FXML
    private lateinit var downloadLocationToNameMap: BiMap<DownloadLocation, String>
    @FXML
    private lateinit var accordionNameComboBoxMap: Map<String, ComboBox<DownloadInformation>>
    @FXML
    private lateinit var accordionNameTitlePaneMap: BiMap<String, TitledPane>
    @FXML
    private lateinit var vBoxUpdate: VBox

    private lateinit var settings: SiGuiSetting

    @JvmName("internalInitialize")
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
                settings.downloadTypes
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
                runOnUiThread {
                    it.items.let { items ->
                        items.clear()
                        items.addAll(data.getValue(location))
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
                DownloadLocation.STABLE to ResourceBundleUtils.DOWNLOADLIST_NAMED,
                DownloadLocation.DAILY to ResourceBundleUtils.DOWNLOADLIST_DAILY,
                DownloadLocation.ARCHIVE to ResourceBundleUtils.DOWNLOADLIST_ARCHIVE
            ).forEach { (dlLocation, key) ->
                localisationSupport.doLocalized(key) { localizedName ->
                    it.put(dlLocation, localizedName)
                    downloadAccordion.panes.apply {
                        add(TitledPane(localizedName, ComboBox<DownloadInformation>().also {
                            tmpChoiceBoxMap.put(localizedName, it)
                            val converter = object : StringConverter<DownloadInformation>() {
                                private val relationMap: MutableMap<String, DownloadInformation?> = HashMap()

                                override fun toString(data: DownloadInformation?): String {
                                    var betterToString: String = data?.let {
                                        when {
                                            it.displayName == "FRESH" -> localisationSupport.getString(
                                                ResourceBundleUtils.DOWNLOADLIST_FRESH
                                            )
                                            it.displayName == "STABLE" -> localisationSupport.getString(
                                                ResourceBundleUtils.DOWNLOADLIST_STABLE
                                            )
                                            it.displayName.startsWith("TESTING") -> localisationSupport.getReplacedString(
                                                ResourceBundleUtils.DOWNLOADLIST_TESTING,
                                                it.displayName.let {
                                                    it.lastIndexOf(' ').let { offset ->
                                                        it.substring(offset + 1)
                                                    }
                                                })
                                            else -> it.displayName
                                        }
                                    } ?: "NULL"

                                    betterToString = betterToString + " (" +
                                            (data?.supportedDownloadType?.let {
                                                (when (it) {
                                                    DownloadType.LINUX_DEB_64, DownloadType.LINUX_DEB_32 -> "DEB - "
                                                    DownloadType.LINUX_RPM_64, DownloadType.LINUX_RPM_32 -> "RPM - "
                                                    else -> ""
                                                }).plus(
                                                    when (it) {
                                                        DownloadType.WINDOWSEXE, DownloadType.WINDOWS32, DownloadType.LINUX_RPM_32, DownloadType.LINUX_DEB_32
                                                        -> "32 bit"
                                                        else -> "64 bit"
                                                    }
                                                )
                                            } ?: "???") + ")"
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
                loader.getController<OptionUIController>()!!
                    .let controller@{ optionController ->
                        optionController.internalInitialize(settings, executorService)
                        Stage().apply {
                            //TODO https://stackoverflow.com/questions/15041760/javafx-open-new-window
                            title = localisationSupport.getString(ResourceBundleUtils.OPTIONS_TITLE)
                            scene = Scene(parent)
                        }.showAndWait()
                        optionController.updateSettings()
                        return@controller optionController.settings
                    }.also { newSettings ->
                        settings = newSettings
                        executorService.submit { settings.persist() }
                    }
            }
        }
    }

    private val downloadManager: DownloadManager<LibreOfficeDownloadFileType> by lazy {
        DownloadManager.getInstance<LibreOfficeDownloadFileType>(
            executorService
        )
    }
}