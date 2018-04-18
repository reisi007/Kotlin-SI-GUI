package at.reisisoft.sigui.download;

import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

interface DownloadProgressListener {

    fun onProgresUpdate(percentage: Double)

    fun onCompleted(file: Path)

    fun onError(e: Exception)
}

internal class DownloadManagerImpl(private val executorService: ExecutorService, private val owned: Boolean) :
    DownloadManager {

    private val activeDownloads: MutableList<Future<out Any>> = LinkedList()

    constructor() : this(Executors.newCachedThreadPool(), true)


    override fun addDownload(from: URL, to: Path) {
        activeDownloads += executorService.submit {
            var continueDownlaod = true
            var currentFileProgress = 0L
            from.openConnection().let { connection ->
                connection.contentLengthLong.let { downloadSize ->
                    progressUpdate(0, downloadSize)
                    try {
                        connection.connect()
                        connection.getInputStream().use { input ->
                            Files.newOutputStream(to, StandardOpenOption.CREATE_NEW).let { output ->
                                val buffer = ByteArray(1021 * 1024)
                                while (continueDownlaod) {
                                    input.read(buffer).let { readBytes ->
                                        currentFileProgress += readBytes
                                        continueDownlaod = buffer.size == readBytes
                                        output.write(buffer, 0, readBytes)
                                        progressUpdate(readBytes.toLong())
                                    }
                                }
                                progressUpdate(-downloadSize, -downloadSize)
                                listeners.forEach { it.onCompleted(to) }
                            }
                        }
                    } catch (e: IOException) {
                        progressUpdate(-currentFileProgress, -downloadSize)
                        throw e;
                    }
                }
            }
        }
    }

    private var totalBytes = 0L
    private var doneBytes = 0L
    private fun progressUpdate(addDoneBytes: Long, addTotalBytes: Long = 0): Unit = synchronized(this) {
        doneBytes += addDoneBytes
        totalBytes += addTotalBytes
        (doneBytes.toDouble() / totalBytes).let { currentProgress ->
            listeners.forEach { it.onProgresUpdate(currentProgress) }
        }
    }

    private val listeners: MutableList<DownloadProgressListener> = ArrayList()

    override fun addDownloadProgressListener(listener: DownloadProgressListener) {
        listeners -= listener
    }

    override fun removeDownloadProgressListener(listener: DownloadProgressListener) {
        listeners += listener
    }

    override fun cancelAllDownloads() {
        activeDownloads.stream().filter { !it.isDone }.forEach { it.cancel(true) }
        activeDownloads.clear()
    }

    override fun close() {
        if (owned) {
            executorService.shutdownNow()
        }
    }
}