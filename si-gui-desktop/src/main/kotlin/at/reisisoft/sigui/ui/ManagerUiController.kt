package at.reisisoft.sigui.ui

import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.preferWindowSize
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.cell.CheckBoxListCell
import javafx.scene.layout.Pane
import javafx.util.Callback
import org.controlsfx.control.CheckListView
import java.net.URL
import java.nio.file.Path
import java.util.*

class ManagerUiController : Initializable {

    private lateinit var languageSupport: ResourceBundle
    internal lateinit var settings: SiGuiSetting
        private set

    @FXML
    private lateinit var rootLayout: Pane
    @FXML
    private lateinit var checkListView: CheckListView<Map.Entry<String, Array<Path>>>

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }


    internal fun internalInitialize(settings: SiGuiSetting) {
        this.settings = settings
        //setup
        rootLayout.preferWindowSize()

        // add items and prepare listview
        checkListView.cellFactory =
                Callback { _ ->
                    object : CheckBoxListCell<Map.Entry<String, Array<Path>>>(checkListView::getItemBooleanProperty) {
                        override fun updateItem(item: Map.Entry<String, Array<Path>>?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = item?.key ?: "null"
                        }
                    }
                }

        checkListView.items.addAll(settings.managedInstalledVersions.entries)
    }
}