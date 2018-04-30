package at.reisisoft.sigui.android.tasks;

import android.content.Context
import android.os.AsyncTask
import at.reisisoft.sigui.android.ui.MainActivity
import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.DownloadType
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper

internal class UpdateApksTask(
    private val preFunction: () -> Unit,
    private val postFunction: (Map<DownloadType, List<DownloadInformation>>) -> Unit
) : AsyncTask<DownloadType, Nothing, Map<DownloadType, List<DownloadInformation>>>() {



    override fun doInBackground(vararg params: DownloadType): Map<DownloadType, List<DownloadInformation>> =
        PossibleDownloadHelper.fetchPossibleFor(listOf(*params)).asSequence().flatMap { (_, it) ->
            it.asSequence()
        }.groupBy { it.supportedDownloadType }

    override fun onPostExecute(result: Map<DownloadType, List<DownloadInformation>>?) {
        if (result != null)
            postFunction(result)
    }

    override fun onPreExecute() {
        preFunction()
    }
}