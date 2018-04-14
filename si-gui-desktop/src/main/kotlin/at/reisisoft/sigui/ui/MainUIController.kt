package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_ARCHIVE
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_DAILY
import at.reisisoft.sigui.ui.UiStrings.DOWNLOADLIST_NAMED
import at.reisisoft.ui.doLocalized
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.*
import java.net.URL
import java.util.*
import kotlin.collections.HashMap

class MainUIController : Initializable {

    lateinit var mainStartDl: Button
    lateinit var downloadAccordion: Accordion
    lateinit var localisationSupport: ResourceBundle
    lateinit var menuBar: MenuBar
    lateinit var accordionNameChoiceBoxMap: Map<String, ChoiceBox<DownloadInformation>>
    lateinit var accordionNameTitlePaneMap: Map<String, TitledPane>

    fun updateListOfDownloadVersions(actionEvent: ActionEvent) {
        println("Test $actionEvent")
    }

    override fun initialize(location: URL, resources: ResourceBundle?) {
        //Ad localisation
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

    }
}