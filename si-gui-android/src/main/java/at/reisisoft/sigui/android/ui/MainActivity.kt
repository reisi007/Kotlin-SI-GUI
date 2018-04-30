package at.reisisoft.sigui.android.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import at.reisisoft.sigui.android.DisplayAbleDownloadInformation
import at.reisisoft.sigui.android.R.layout.activity_main
import at.reisisoft.sigui.android.getDownloadTypes
import at.reisisoft.sigui.android.tasks.UpdateApksTask
import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val downloadTypeSpinnerMap: Map<DownloadType, Spinner> by lazy {
        mapOf(
            DownloadType.ANDROID_REMOTE to siRemote,
            DownloadType.ANDROID_LIBREOFFICE_X86 to mainLibO,
            DownloadType.ANDROID_LIBREOFFICE_ARM to mainLibO
        )
    }

    private lateinit var lastSelectedDownloadInformation: DownloadInformation

    private val downloadTypes by lazy { getDownloadTypes().toTypedArray() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        progressBar.visibility = View.GONE

        updateApks.setOnClickListener {
            UpdateApksTask({
                progressBar.visibility = View.VISIBLE
            }, {
                val siRemoteSelection = siRemote.selectedItemPosition
                val mainLibOSelection = mainLibO.selectedItemPosition

                it.asSequence().flatMap { (k, v) ->
                    v.asSequence().map { downloadTypeSpinnerMap[k] to DisplayAbleDownloadInformation(it) }
                }.groupBy({ (spinner, _) ->
                    spinner
                }, { (_, data) -> data }).forEach { (k, data) ->
                    k?.let {
                        (it.adapter as? ArrayAdapter<DisplayAbleDownloadInformation> ?: kotlin.run {
                            ArrayAdapter<DisplayAbleDownloadInformation>(
                                this,
                                android.R.layout.simple_spinner_dropdown_item
                            ).apply {
                                it.adapter = this
                            }
                        }).also {
                            it.clear()
                            it.addAll(data)
                        }
                    }
                }
                siRemote.setSelection(siRemoteSelection)
                mainLibO.setSelection(mainLibOSelection)

                progressBar.visibility = View.GONE
            }).execute(*downloadTypes)
        }

        startInstallation.setOnClickListener {
            TODO()
        }
    }
}
