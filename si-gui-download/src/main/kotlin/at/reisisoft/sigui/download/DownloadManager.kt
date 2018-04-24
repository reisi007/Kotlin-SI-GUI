package at.reisisoft.sigui.download;

import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import javax.net.ssl.HttpsURLConnection

data class DownloadFinishedEvent<Custom>(val file: Path, val customData: Custom)

interface DownloadProgressListener<Custom> {

    fun onProgresUpdate(percentage: Double)

    fun onCompleted(downloadFinishedEvent: DownloadFinishedEvent<Custom>)

    fun onError(e: Exception)
}

internal class DownloadManagerImpl<T>(private val executorService: ExecutorService, private val owned: Boolean) :
    DownloadManager<T> {

    private val activeDownloads: MutableList<Future<out Any>> = LinkedList()
    private var downloadCancelInProgress = false

    constructor() : this(Executors.newCachedThreadPool(), true)


    override fun addDownload(from: URL, to: Path, customData: T) {
        downloadCancelInProgress = false
        activeDownloads += executorService.submit {
            var currentFileProgress = 0L
            var continueDownload = true
            from.openConnection().let { connection ->
                connection.contentLengthLong.let { downloadSize ->
                    progressUpdate(0, downloadSize)
                    try {
                        connection.connect()
                        if (connection is HttpsURLConnection) {
                            connection.inputStream.buffered()
                        }
                        val BUFFER_SIZE = 1024 * 32
                        connection.getInputStream().let { BufferedInputStream(it, BUFFER_SIZE) }.use { input ->
                            Files.newOutputStream(to, StandardOpenOption.CREATE_NEW).use { output ->
                                val buffer = ByteArray(BUFFER_SIZE)
                                while (continueDownload) {
                                    input.read(buffer).let { readBytes ->
                                        if (readBytes >= 0) {
                                            currentFileProgress += readBytes
                                            output.write(buffer, 0, readBytes)
                                            progressUpdate(readBytes.toLong())
                                        } else
                                            continueDownload = false
                                    }
                                }
                                progressUpdate(-downloadSize, -downloadSize)
                                DownloadFinishedEvent(to, customData).let { event ->
                                    listeners.forEach { it.onCompleted(event) }
                                }
                            }
                        }
                    } catch (e: IOException) {
                        progressUpdate(-currentFileProgress, -downloadSize)
                        if (!downloadCancelInProgress) {
                            processException(e)
                            throw e;
                        }
                    }
                }
            }
        }
    }

    private fun processException(e: Exception) = listeners.forEach { it.onError(e) }

    private var totalBytes = 0L
    private var doneBytes = 0L
    private fun progressUpdate(addDoneBytes: Long, addTotalBytes: Long = 0): Unit = synchronized(this) {
        doneBytes += addDoneBytes
        totalBytes += addTotalBytes
        (if (totalBytes == 0L) 0.0 else (doneBytes.toDouble() / totalBytes)).let { currentProgress ->
            listeners.forEach { it.onProgresUpdate(currentProgress) }
        }
    }

    private val listeners: MutableList<DownloadProgressListener<T>> = ArrayList()

    override fun addDownloadProgressListener(listener: DownloadProgressListener<T>) {
        listeners += listener
    }

    override fun removeDownloadProgressListener(listener: DownloadProgressListener<T>) {
        listeners -= listener
    }

    override fun cancelAllDownloads() {
        downloadCancelInProgress = true
        activeDownloads.stream().filter { !it.isDone }.forEach { it.cancel(true) }
        activeDownloads.clear()
    }

    override fun close() {
        if (owned) {
            executorService.shutdownNow()
        }
    }
}