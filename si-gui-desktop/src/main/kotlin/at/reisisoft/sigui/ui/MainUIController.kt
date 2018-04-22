package at.reisisoft.sigui.ui

import at.reisisoft.format
import at.reisisoft.sigui.commons.downloads.*
import at.reisisoft.sigui.download.DownloadFinishedEvent
import at.reisisoft.sigui.download.DownloadManager
import at.reisisoft.sigui.download.DownloadProgressListener
import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.*
import at.reisisoft.withChild
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.stage.Stage
import javafx.util.StringConverter
import java.net.URL
import java.nio.file.FileAlreadyExistsException
import java.util.*
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class MainUIController : Initializable, AutoCloseable {

    @FXML
    private lateinit var indicatorUpdateVersions: ProgressIndicator
    @FXML
    private lateinit var updateListOfVersions: Button
    @FXML
    private lateinit var downloadAccordion: Accordion
    private lateinit var localisationSupport: ResourceBundle


    private var downloadInformation: MutableList<Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane>> =
        ArrayList(3)

    private fun List<Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane>>.find(find: DownloadLocation): Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane> =
        (if (find == DownloadLocation.TESTING) DownloadLocation.STABLE else find).let { dl ->
            this.find { it.first == dl }!!
        }

    private fun List<Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane>>.find(find: ComboBox<DownloadInformation>): Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane> =
        this.find { it.second == find }!!

    private fun List<Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane>>.find(find: TitledPane): Triple<DownloadLocation, ComboBox<DownloadInformation>, TitledPane> =
        this.find { it.third == find }!!

    @FXML
    private lateinit var vBoxUpdate: VBox

    @FXML
    private lateinit var startdlButton: Button
    @FXML
    private lateinit var rootPane: Pane
    @FXML
    private lateinit var centerLayout: Pane

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
                        downloadInformation.find(DownloadLocation.STABLE)
                    else
                        downloadInformation.find(expandedPane)
                }.let { (location, comboBox, _) ->
                    comboBox.selectionModel.selectedItem?.let { selectedItem: DownloadInformation ->
                        location to selectedItem
                    } ?: kotlin.run {
                        location to comboBox.items.firstOrNull()
                    }

                }

        //Update data
        for (location in data.keys) {
            downloadInformation.find(location).let { (_, it, _) ->
                runOnUiThread {
                    it.items.let { items ->
                        items.clear()
                        items.addAll(data.getValue(location))
                    }
                }
            }
        }

        //Update selection
        downloadInformation.find(realCurrentSelection.first).let { (_, expandedComboBox, expandedPane) ->
            downloadAccordion.expandedPane = expandedPane

            expandedComboBox.selectionModel.let { selectionModel ->
                realCurrentSelection.second.let { selectedDlInfo ->
                    runOnUiThread {
                        val nextSelectedItem = selectedDlInfo?.let { old ->
                            expandedComboBox.items.find { it.displayName == old.displayName }
                        } ?: expandedComboBox.items.firstOrNull() //null if list is empty

                        selectionModel.select(nextSelectedItem)

                        val selectionToStore =
                            realCurrentSelection.first to nextSelectedItem

                        //Persist settings
                        settings = settings.copy(downloadedVersions = data, downloadSelection = selectionToStore)
                            .also {
                                if (storeSettings) {
                                    it.asyncPersist()
                                    settings = it
                                }

                            }
                    }
                }
            }
        }
    }


    override fun initialize(location: URL, resources: ResourceBundle?) {
        //Add localisation
        localisationSupport = resources ?: throw UnsupportedOperationException("Could not obtain localisation!")
    }

    private fun initDownloadList() {
        arrayOf(
            DownloadLocation.STABLE to ResourceBundleUtils.DOWNLOADLIST_NAMED,
            DownloadLocation.DAILY to ResourceBundleUtils.DOWNLOADLIST_DAILY,
            DownloadLocation.ARCHIVE to ResourceBundleUtils.DOWNLOADLIST_ARCHIVE
        ).forEach { (dlLocation, key) ->
            localisationSupport.doLocalized(key) { localizedName ->
                downloadAccordion.panes.apply {
                    ComboBox<DownloadInformation>().let { comboBox ->
                        add(TitledPane(localizedName, comboBox).also {
                            //Add element to list
                            downloadInformation.add(Triple(dlLocation, comboBox, it))

                        })
                        comboBox.also {
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

                            it.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                downloadInformation.find(it).let { (location, _, _) ->
                                    updateSelection(location, newValue)
                                }
                            }
                        }

                    }
                }
            }
        }
        updateAccordion(settings.downloadedVersions, settings.downloadSelection)

        //Setup update button
        updateListOfVersions.prefWidthProperty().bind(vBoxUpdate.widthProperty())
    }

    private fun initDownloads() {
        cancelDownloads.onAction = EventHandler<ActionEvent> {
            downloadManagerDelegate.let {
                if (it.isInitialized()) {
                    downloadManager.cancelAllDownloads()
                }
            }
        }
        cancelDownloads.prefWidthProperty().bind(installationRootPane.widthProperty())
        //Init downloadbutton
        startdlButton.prefWidthProperty().bind(downloadAccordion.widthProperty())
    }

    /**
     * This is called when [setSettings] is called for the first time
     */
    private fun internalInitialize() {
        rootPane.preferWindowSize()
        centerLayout.prefWidthProperty().bind(rootPane.widthProperty())

        initMenu()
        initDownloadList()
        initDownloads()
        initInstallation()
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
        JavaFxUtils.loadFXML("optionUI.fxml").let { loader ->
            loader.resources = localisationSupport
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
                    executorService.submit { settings.asyncPersist() }
                }
        }

    }

    private var downloadWindowsOpenTaskStarted = false
    fun openDownloadMenu(actionEvent: ActionEvent) {
        if (!downloadWindowsOpenTaskStarted) {
            downloadWindowsOpenTaskStarted = true
            executorService.submit {
                println("Open download menu")
                //Fetch data
                settings.downloadSelection?.let { (_, nullableInformation) ->
                    nullableInformation?.let { information ->
                        val downloads = PossibleDownloadHelper.getFinalDownloadUrls(
                            information.baseUrl,
                            information.supportedDownloadType,
                            setOf(*LibreOfficeDownloadFileType.values()),
                            settings.hpLanguage ?: Locale.forLanguageTag(EN_US)
                        )

                        //Open
                        JavaFxUtils.loadFXML("downloadUI.fxml").let {
                            it.resources = localisationSupport
                            val parent: Parent = it.load()
                            runOnUiThread {
                                it.getController<DownloadUiConroller>().let controller@{ controller ->
                                    controller.setDownloads(
                                        downloads,
                                        information.baseUrl,
                                        settings.downloadFolder
                                    )
                                    Stage().apply {
                                        title =
                                                localisationSupport.getString(ResourceBundleUtils.DOWNLOADER_TITLE)
                                        scene = Scene(parent)
                                    }.showAndWait()

                                    downloadWindowsOpenTaskStarted = false
                                    controller.downloads as Map<LibreOfficeDownloadFileType, String>
                                }.let { downloads ->
                                    if (downloads.isNotEmpty())
                                        downloads.forEach { fileType, fileName ->
                                            URL("${information.baseUrl}$fileName").let { url ->
                                                downloadManager.addDownload(
                                                    url,
                                                    settings.downloadFolder withChild fileName,
                                                    fileType
                                                )
                                            }
                                        }
                                }
                            }
                        }
                    } ?: kotlin.run { downloadWindowsOpenTaskStarted = false }
                } ?: kotlin.run { downloadWindowsOpenTaskStarted = false }
            }
        }
    }

    @FXML
    private lateinit var downloadProgressBar: ProgressBar
    @FXML
    private lateinit var downloadProgressLabel: Label
    @FXML
    private lateinit var cancelDownloads: Button

    val percentage100 = 100 * 100
    private val downloadManagerDelegate = lazy {
        DownloadManager.getInstance<LibreOfficeDownloadFileType>(
            executorService
        ).apply {
            addDownloadProgressListener(object : DownloadProgressListener<LibreOfficeDownloadFileType> {
                override fun onProgresUpdate(percentage: Double) =
                    (percentage * 100).let {
                        (if (it == Double.NaN) 0.0 else it).let { readablePercentage ->
                            runOnUiThread {
                                downloadProgressBar.progress = percentage
                                downloadProgressLabel.text = readablePercentage.format(2) + '%'
                            }
                        }
                    }

                override fun onCompleted(downloadFinishedEvent: DownloadFinishedEvent<LibreOfficeDownloadFileType>) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onError(e: Exception) = when (e) {
                    is FileAlreadyExistsException -> showWarning(
                        localisationSupport.getReplacedString(
                            ResourceBundleUtils.ERROR_FILEEXISRS,
                            e.file
                        )
                    )
                    else -> showError(e)
                }
            })
        }
    }

    private val downloadManager: DownloadManager<LibreOfficeDownloadFileType> by downloadManagerDelegate


    private fun updateSelection(location: DownloadLocation, information: DownloadInformation) {
        settings = settings.copy(downloadSelection = location to information).apply { asyncPersist() }
    }

    private fun SiGuiSetting.asyncPersist() = executorService.submit { this.persist() }

    // Installation layout
    @FXML
    private lateinit var startInstallButton: Button
    @FXML
    private lateinit var installMainText: Label
    @FXML
    private lateinit var resetMain: Button
    @FXML
    private lateinit var resetSdk: Button
    @FXML
    private lateinit var resetHelp: Button
    @FXML
    private lateinit var runMain: Button
    @FXML
    private lateinit var runSdk: Button
    @FXML
    private lateinit var runHelp: Button
    @FXML
    private lateinit var installSdkText: Label
    @FXML
    private lateinit var installHelpText: Label
    @FXML
    private lateinit var installationRootPane: Region

    private fun initInstallation() {
        startInstallButton.prefWidthProperty().bind(installationRootPane.widthProperty())

    }


    //Menu
    private fun initMenu() {

    }

    fun openManager(event: ActionEvent): Unit {
        TODO()
    }


}