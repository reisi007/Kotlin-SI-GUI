package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_ARCHIVE
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_DAILY
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_NAMED
import at.reisisoft.ui.JavaFX
import at.reisisoft.ui.doLocalized
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.layout.VBox
import java.net.URL
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.HashMap

class MainUIController : Initializable, AutoCloseable {

    internal lateinit var indicatorUpdateVersions: ProgressIndicator
    internal lateinit var updateListOfVersions: Button
    internal lateinit var downloadAccordion: Accordion
    private lateinit var localisationSupport: ResourceBundle
    internal lateinit var menuBar: MenuBar
    internal lateinit var accordionNameChoiceBoxMap: Map<String, ChoiceBox<DownloadInformation>>
    internal lateinit var accordionNameTitlePaneMap: Map<String, TitledPane>
    internal lateinit var vBoxUpdate: VBox

    fun updateListOfDownloadVersions(actionEvent: ActionEvent) {
        println("Update version clicked!")
        indicatorUpdateVersions.isVisible = true
        executorService.submit {
            Thread.sleep(2000)
            JavaFX.runOnUiThread {
                indicatorUpdateVersions.isVisible = false
            }

        }
    }

    override fun initialize(location: URL, resources: ResourceBundle?) {
        //Add localisation
        localisationSupport = resources ?: throw UnsupportedOperationException("Could not obtain localisation!")

        //Setup accordion
        val tmpChoiceBoxMap: MutableMap<String, ChoiceBox<DownloadInformation>> = HashMap()
        val tmpTitlePaneMap: MutableMap<String, TitledPane> = HashMap()
        arrayOf(DOWNLOADLIST_NAMED, DOWNLOADLIST_DAILY, DOWNLOADLIST_ARCHIVE).forEach { key ->
            localisationSupport.doLocalized(key) { localizedName ->
                downloadAccordion.panes.apply {
                    add(TitledPane(localizedName, ChoiceBox<DownloadInformation>().also {
                        tmpChoiceBoxMap.put(key, it)
                        it.prefWidthProperty().bind(downloadAccordion.widthProperty())
                    }).also {
                        tmpTitlePaneMap.put(key, it)
                    })
                }
            }
        }
        accordionNameChoiceBoxMap = tmpChoiceBoxMap
        accordionNameTitlePaneMap = tmpTitlePaneMap
        downloadAccordion.expandedPane = accordionNameTitlePaneMap[DOWNLOADLIST_DAILY]

        //Setup update button
        updateListOfVersions.prefWidthProperty().bind(vBoxUpdate.widthProperty())
    }

    private val executorServiceDelegate = lazy {
        Executors.newCachedThreadPool()!!
    }

    private val executorService by executorServiceDelegate

    override fun close() {
        if (executorServiceDelegate.isInitialized())
            executorService.shutdown()
        executorService.awaitTermination(10, TimeUnit.SECONDS)
    }
}