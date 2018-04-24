package at.reisisoft.sigui.updater;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Updater {

    private static final String updaterUrl = "https://tdf.io/kotlinsiguiupdaterendpoint";
    private static final Path siGuiInstallFolder = Paths.get(".", "kotlin-si-gui").toAbsolutePath().normalize();
    private static final String settingFilename = "si-gui.settings.json";

    public static void main(String[] args) {
        try {
            final String installedeTag = loadCurrentVersionFromDisk();
            final Map.Entry<String, HttpURLConnection> online = loadLastOnlineVersion();
            if (installedeTag.equals(online.getKey())) {
                //Same eTag -> no update needed
                startSiGui();
            } else {
                final Path tmpFile = Paths.get(".", online.getKey() + ".zip");
                Files.deleteIfExists(tmpFile);
                //TODO show progress / Ask to dowenload
                try {
                    try (final InputStream onlineIn = online.getValue().getInputStream();
                         final OutputStream fileOut = Files.newOutputStream(tmpFile, StandardOpenOption.CREATE_NEW)) {
                        final float length = online.getValue().getContentLength();
                        int totalRead = 0;
                        int read;
                        float realPercentage;
                        byte[] buffer = new byte[1024 * 32];
                        while ((read = onlineIn.read(buffer)) >= 0) {
                            totalRead += read;
                            fileOut.write(buffer, 0, read);
                            realPercentage = totalRead / length;
                            String formatted = String.format("%.2f%%", realPercentage * 100);
                            System.out.println("Current progress: " + formatted);
                        }
                    }
                    //Replace the current installed version with the temp version

                    //Delete all files
                    Stream.of(siGuiInstallFolder).filter(Files::exists).flatMap(dir -> {
                        try {
                            return Files.list(dir);
                        } catch (IOException e) {
                            return Stream.empty();
                        }
                    }).filter(it -> !it.getFileName().toString().equals(settingFilename)).filter(Files::exists).sorted(Comparator.reverseOrder()).forEach(it -> {
                        try {
                            Files.deleteIfExists(it);
                        } catch (IOException e) {
                            //Ignore
                        }
                    });
                    //Extract new version
                    extractZip(tmpFile, siGuiInstallFolder);
                    //Start SI GUI
                    startSiGui();
                    //Save etag
                    try (BufferedWriter writer = Files.newBufferedWriter(lastUpdatePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                        writer.write(online.getKey());
                    }
                } finally {
                    Files.deleteIfExists(tmpFile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Better exception handling needed!");
        }
    }

    private static void extractZip(final Path zipFile, final Path out) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipFile, StandardOpenOption.READ))) {
            ZipEntry curEntry;
            Path outFile;
            while ((curEntry = zis.getNextEntry()) != null) {
                if (curEntry.isDirectory())
                    continue;
                outFile = out.resolve(curEntry.getName());
                Files.createDirectories(outFile.getParent());
                Files.copy(zis, outFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    private static String loadCurrentVersionFromDisk() {
        try {
            try (BufferedReader reader = Files.newBufferedReader(lastUpdatePath, StandardCharsets.UTF_8)) {
                String tmp = reader.readLine();
                if (tmp == null)
                    throw new IllegalStateException();
                return tmp;
            }

        } catch (Exception e) {
            return "";
        }
    }

    private static Map.Entry<String, HttpURLConnection> loadLastOnlineVersion() throws IOException {
        URL url = new URL(updaterUrl);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        String etag = con.getHeaderField("ETag");

        return new AbstractMap.SimpleImmutableEntry(etag.substring(1, etag.length() - 1), con);
    }


    private static final Path lastUpdatePath = Paths.get(".", "last_update.txt");

    public static void startSiGui() throws Exception {
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("wind");
        Path scriptFolder = Files.list(siGuiInstallFolder).findFirst().get().resolve("bin");
        if (isWindows)
            new ProcessBuilder("cmd.exe", "/c", "si-gui-desktop.bat").directory(scriptFolder.toFile()).inheritIO().start();
        else
            new ProcessBuilder("si-gui-desktop").directory(scriptFolder.toFile()).inheritIO().start();
    }
}