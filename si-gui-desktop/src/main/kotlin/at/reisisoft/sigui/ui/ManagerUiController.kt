package at.reisisoft.sigui.ui

import at.reisisoft.sigui.settings.SiGuiSetting
import at.reisisoft.ui.*
import at.reisisoft.ui.JavaFxUtils.showAlert
import at.reisisoft.ui.JavaFxUtils.showError
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.Alert
import javafx.scene.control.Button
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.cell.CheckBoxListCell
import javafx.scene.layout.Pane
import javafx.util.Callback
import org.controlsfx.control.CheckListView
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ExecutorService
import kotlin.collections.ArrayList
import kotlin.streams.asSequence

class ManagerUiController : Initializable {

    private lateinit var languageSupport: ResourceBundle
    internal lateinit var settings: SiGuiSetting
        private set

    @FXML
    private lateinit var rootLayout: Pane
    @FXML
    private lateinit var checkListView: CheckListView<Map.Entry<String, Array<Path>>>
    @FXML
    private lateinit var deleteProgress: ProgressIndicator
    @FXML
    private lateinit var closeButton: Button
    @FXML
    private lateinit var deleteButton: Button

    override fun initialize(location: URL?, resources: ResourceBundle?) {
        languageSupport = resources!!
    }


    internal fun internalInitialize(settings: SiGuiSetting, executorService: ExecutorService) {
        this.settings = settings
        //setup
        rootLayout.preferWindowSize()

        // add items and prepare listview
        checkListView.cellFactory =
                Callback { _ ->
                    object : CheckBoxListCell<Map.Entry<String, Array<Path>>>(checkListView::getItemBooleanProperty) {
                        override fun updateItem(item: Map.Entry<String, Array<Path>>?, empty: Boolean) {
                            super.updateItem(item, empty)
                            text = item?.key ?: ""
                        }
                    }
                }

        checkListView.items.addAll(settings.managedInstalledVersions.entries)

        closeButton.closeStageOnClick()

        deleteButton.onAction = EventHandler {
            if (deleteProgress.isVisible)
                return@EventHandler
            runOnUiThread { deleteProgress.isVisible = true }
            checkListView.checkModel.checkedItems.let { ArrayList(it) }.let { items ->
                executorService.submit {
                    //Safe delete all files
                    try {
                        items.asSequence().flatMap { it.value.asSequence() }.filter { Files.exists(it) }
                            .flatMap {
                                Files.walk(it).asSequence()
                            }.sortedWith(Comparator.reverseOrder())
                            .map {
                                try {
                                    Files.delete(it)
                                    null
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    it
                                }
                            }.filter(Objects::nonNull).map { it!!.toString() }
                            .joinToString(System.lineSeparator()) { it }.let {
                                if (it.isBlank())
                                    showAlert(
                                        languageSupport.getString(ResourceKey.INSTALL_SUCCESS),
                                        Alert.AlertType.INFORMATION
                                    )
                                else showWarning(languageSupport.getString(ResourceKey.MANAGER_DELETE_FAILURE) + System.lineSeparator() + System.lineSeparator() + it)
                            }
                    } catch (e: Exception) {
                        showError(e)
                    }

                    runOnUiThread {
                        checkListView.items.removeAll(items)
                        deleteProgress.isVisible = false
                    }
                }
            }
        }
    }
}