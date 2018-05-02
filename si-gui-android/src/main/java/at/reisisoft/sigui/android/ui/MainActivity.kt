package at.reisisoft.sigui.android.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.WebView
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import at.reisisoft.sigui.android.*
import at.reisisoft.sigui.android.R.layout.activity_main
import at.reisisoft.sigui.android.tasks.DownloadApkTask
import at.reisisoft.sigui.android.tasks.UpdateApksTask
import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadType
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val downloadTypeSpinnerMap: Map<DownloadType, Spinner> by lazy {
        mapOf(
            DownloadType.ANDROID_REMOTE to sdRemote,
            DownloadType.ANDROID_LIBREOFFICE_X86 to mainLibO,
            DownloadType.ANDROID_LIBREOFFICE_ARM to mainLibO
        )
    }

    override fun onPause() {
        settings.store(applicationContext)
        super.onPause()
    }

    private fun ensureArrayAdapter(spinner: Spinner): ArrayAdapter<DisplayAbleDownloadInformation> =
        (spinner.adapter as? ArrayAdapter<DisplayAbleDownloadInformation>) ?: kotlin.run {
            ArrayAdapter<DisplayAbleDownloadInformation>(
                this,
                android.R.layout.simple_spinner_dropdown_item
            ).apply { spinner.adapter = this }
        }

    private lateinit var lastSelectedDownloadInformation: DisplayAbleDownloadInformation
    private lateinit var settings: AndroidSettings

    private val downloadTypes by lazy { getDownloadTypes().toTypedArray() }

    override fun onCreate(savedInstanceState: Bundle?) {
        settings = AndroidSettings.load(applicationContext)
        super.onCreate(savedInstanceState)
        setContentView(activity_main)

        progressBar.visibility = View.GONE

        updateApks.setOnClickListener {
            UpdateApksTask({
                progressBar.visibility = View.VISIBLE
            }, {
                val siRemoteSelection = sdRemote.selectedItemPosition
                val mainLibOSelection = mainLibO.selectedItemPosition

                val listSize = (it[DownloadType.ANDROID_LIBREOFFICE_ARM]?.size
                        ?: 0) + (it[DownloadType.ANDROID_LIBREOFFICE_X86]?.size ?: 0)

                ArrayList<DisplayAbleDownloadInformation>(listSize).apply {
                    emptySequence<DownloadInformation>().let { seq ->
                        it[DownloadType.ANDROID_LIBREOFFICE_ARM]?.let { seq + it }
                        it[DownloadType.ANDROID_LIBREOFFICE_X86]?.let { seq + it }
                    }?.map {
                        DisplayAbleDownloadInformation(it)
                    }?.forEach {
                        add(it)
                    }
                    settings = settings.copy(main = this)
                }

                it[DownloadType.ANDROID_REMOTE]?.let {
                    ArrayList<DisplayAbleDownloadInformation>(it.size).apply {
                        it.forEach {
                            add(DisplayAbleDownloadInformation(it))
                        }
                        settings = settings.copy(sdremote = this)
                    }
                }

                settings.store(applicationContext)

                it.asSequence().flatMap { (k, v) ->
                    v.asSequence().map { downloadTypeSpinnerMap[k] to DisplayAbleDownloadInformation(it) }
                }.groupBy({ (spinner, _) ->
                    spinner
                }, { (_, data) -> data }).forEach { (k, data) ->
                    k?.let {
                        ensureArrayAdapter(it).also {
                            it.clear()
                            it.addAll(data)
                            it.notifyDataSetChanged()
                        }
                    }
                }
                sdRemote.setSelection(siRemoteSelection)
                mainLibO.setSelection(mainLibOSelection)

                progressBar.visibility = View.GONE
            }).execute(*downloadTypes)
        }

        settings.apply {
            ensureArrayAdapter(sdRemote).apply {
                addAll(sdremote)
                notifyDataSetChanged()
            }
            ensureArrayAdapter(mainLibO).apply {
                addAll(main)
                notifyDataSetChanged()
            }
            curSelection?.let {
                lastSelectedDownloadInformation = it
                updateCurSelectionUi()
            }

        }

        sequenceOf(sdRemote, mainLibO).forEach {
            it.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    lastSelectedDownloadInformation = it.selectedItem as? DisplayAbleDownloadInformation ?:
                            throw IllegalStateException("Cannot get download information from selection")
                    updateCurSelectionUi()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    it.setSelection(0)
                }

            }
        }

        startInstallation.setOnClickListener {
            if (progressBar.visibility == View.GONE) {
                if (::lastSelectedDownloadInformation.isInitialized)
                    DownloadApkTask(
                        applicationContext,
                        downloadProgress
                    ).execute(lastSelectedDownloadInformation.downloadInformation)
            }
        }

        clearCache.setOnClickListener {
            deleteCache()
            showToast(R.string.clear_cache_done)
        }

        legal.setOnClickListener {
            AlertDialog.Builder(this).also {
                it.setTitle(R.string.about_licenses)
                WebView(this).apply {
                    loadData(LEGAL_HTML, "text/html", "utf-8")
                    it.setView(this)
                }
                it.setNegativeButton(R.string.close) { dialog, _ ->
                    dialog.dismiss()
                }
            }.show()
        }
    }

    private fun updateCurSelectionUi() {
        if (::lastSelectedDownloadInformation.isInitialized)
            curSelection.text = getString(R.string.curSelection) +
                    System.lineSeparator() + System.lineSeparator() +
                    lastSelectedDownloadInformation.toString()
    }
}
