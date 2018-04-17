package at.reisisoft.sigui.download;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DownloadManager extends AutoCloseable {

    @NotNull
    static DownloadManager getInstance(@Nullable final ExecutorService executorService) {
        final ExecutorService finExecutorService = (executorService == null) ? Executors.newCachedThreadPool() : executorService;
        final boolean owned = executorService != null; // true if passed executorService is non-null
        return new DownloadManagerImpl(finExecutorService, owned);
    }

    void addDownload(@NotNull URL from, @NotNull Path to);

    void addDownloadFinishedListener(@NotNull DownloadFinishedListener listener);

    void removeDownloadFinishedListener(@NotNull DownloadFinishedListener listener);

    void cancelAllDownloads();
}