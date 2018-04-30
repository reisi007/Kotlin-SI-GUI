package at.reisisoft.sigui.android

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.support.v4.content.FileProvider
import at.reisisoft.sigui.commons.downloads.DownloadType
import java.io.File

fun getDownloadTypes(): Set<DownloadType> =
    Build.SUPPORTED_ABIS.asSequence().map {
        when {
            it.contains("arm", true) -> DownloadType.ANDROID_LIBREOFFICE_ARM
            it.contains("x86", true) -> DownloadType.ANDROID_LIBREOFFICE_X86
            else -> null
        }
    }.filterNotNull().plus(DownloadType.ANDROID_REMOTE).toSet()

// https://stackoverflow.com/questions/39147608/android-install-apk-with-intent-view-action-not-working-with-file-provider/40131196#40131196
internal fun Activity.installApk(downloadFile: File) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", downloadFile).let {
            Intent(Intent.ACTION_INSTALL_PACKAGE).apply {
                setData(it)
                setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.also { startActivity(it) }
        }
    else {
        Uri.fromFile(downloadFile).let {
            Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(it, "application/vnd.android.package-archive");
                setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }.also { startActivity(it) }
            startActivity(intent);
        }
    }
}

private fun String.extractFileName() = substring(lastIndexOf('/') + 1, length)

//https://stackoverflow.com/a/4969421/1870799
internal fun Activity.asyncDownloadAndInstall(urlAsString: String): File? =
    urlAsString.extractFileName().let { fileName ->
        File(applicationContext.cacheDir, fileName).let { toFile ->
            Uri.parse("file://$toFile").let { fileUri ->
                DownloadManager.Request(Uri.parse(urlAsString)).apply {
                    getString(R.string.download).let { "$it: ${toFile.name}" }.let { title ->
                        setDescription(title)
                        setTitle(title)
                        setDestinationUri(fileUri)
                    }
                }
            }.let { request ->
                getSystemService(Context.DOWNLOAD_SERVICE).let {
                    it as? DownloadManager ?: throw IllegalStateException("DownloadmManager not foun!")
                }.let { dlM ->
                    dlM.enqueue(request).let { downloadId ->
                        object : BroadcastReceiver() {
                            override fun onReceive(context: Context, intent: Intent) {
                                Intent(Intent.ACTION_VIEW).let { i ->
                                    installApk(toFile)
                                    unregisterReceiver(this)
                                    finish()
                                }
                            }
                        }
                    }.let freturn@{
                        registerReceiver(it, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
                        return@freturn toFile
                    }
                }
            }
        }
    }