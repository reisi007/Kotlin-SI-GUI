package at.reisisoft.sigui.download;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public interface DownloadManager<T> extends AutoCloseable {

    @NotNull
    static <T> DownloadManager<T> getInstance(@Nullable final ExecutorService executorService) {
        final ExecutorService finExecutorService = (executorService == null) ? Executors.newCachedThreadPool() : executorService;
        final boolean owned = executorService != null; // true if passed executorService is non-null
        return new DownloadManagerImpl<T>(finExecutorService, owned);
    }

    void addDownload(@NotNull URL from, @NotNull Path to, @NotNull T customData);

    void addDownloadProgressListener(@NotNull DownloadProgressListener<T> listener);

    void removeDownloadProgressListener(@NotNull DownloadProgressListener<T> listener);

    void cancelAllDownloads();
}