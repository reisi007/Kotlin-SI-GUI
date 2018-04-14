package at.reisisoft.sigui.ui

import at.reisisoft.sigui.commons.downloads.DownloadType
import javafx.collections.FXCollections
import javafx.event.ActionEvent
import javafx.fxml.Initializable
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import java.net.URL
import java.util.*

class MainUIController : Initializable {

    lateinit var mainStartDl: Button
    lateinit var listOfDownlaods: ChoiceBox<DownloadType>


    fun updateListOfDownloadVersions(actionEvent: ActionEvent) {
        println("Test")
    }

    override fun initialize(location: URL, resources: ResourceBundle?) {
        //TODO TEMPORARY
        listOfDownlaods.items = FXCollections.observableArrayList(*DownloadType.values())
    }
}