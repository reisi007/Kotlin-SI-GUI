package at.reisisoft.sigui.download;

import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

interface DownloadFinishedListener {

    fun onCompleted(file: Path)

    fun onError(e: Exception)
}

internal class DownloadManagerImpl(private val executorService: ExecutorService, private val owned: Boolean) :
    DownloadManager {

    constructor() : this(Executors.newCachedThreadPool(), true)

    private val listeners: MutableList<DownloadFinishedListener> = ArrayList()

    override fun addDownload(from: URL, to: Path) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun addDownloadFinishedListener(listener: DownloadFinishedListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeDownloadFinishedListener(listener: DownloadFinishedListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelAllDownloads() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun close() {
        if (owned) {
            executorService.shutdownNow()
        }
    }
}