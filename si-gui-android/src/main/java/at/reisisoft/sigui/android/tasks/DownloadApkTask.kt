package at.reisisoft.sigui.android.tasks

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v4.content.FileProvider
import android.view.View
import android.widget.ProgressBar
import at.reisisoft.sigui.android.R
import at.reisisoft.sigui.android.extractFileName
import at.reisisoft.sigui.commons.downloads.DownloadInformation
import at.reisisoft.sigui.commons.downloads.PossibleDownloadHelper
import java.io.File
import java.net.URL

internal class DownloadApkTask(private val context: Context, private val progressBar: ProgressBar) :
    AsyncTask<DownloadInformation, Int, Unit>() {
    override fun doInBackground(vararg params: DownloadInformation?) {
        params.firstOrNull()?.let {
            PossibleDownloadHelper.getAndroidFinalDownloadUrl(it)
        }?.let { finalUrl ->
            finalUrl.extractFileName().let { fileName ->
                val storageFile = File(context.cacheDir, fileName)
                storageFile.createNewFile()
                URL(finalUrl).openConnection().let {
                    (it.contentLength / 100f).let { divisor ->
                        it.getInputStream().use { input ->
                            storageFile.outputStream().use { output ->
                                val buffer = ByteArray(1024 * 16)
                                var read: Int
                                var totalRead = 0
                                do {
                                    read = input.read(buffer)
                                    totalRead += read

                                    onProgressUpdate(Math.round(totalRead / divisor))
                                    if (read >= 0)
                                        output.write(buffer, 0, read)
                                } while (read >= 0)
                            }
                        }
                    }
                }

                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        FileProvider.getUriForFile(
                            context,
                            context.getString(R.string.fileProvider),
                            storageFile
                        ), "application/vnd.android.package-archive"
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    context.startActivity(this)
                }

            }
        }
    }

    override fun onProgressUpdate(vararg values: Int?) {
        values.firstOrNull()?.let { value ->
            progressBar.progress = value
        }
    }

    override fun onPostExecute(result: Unit?) {
        progressBar.visibility = View.GONE
    }

    override fun onPreExecute() {
        progressBar.visibility = View.VISIBLE
    }
}