package at.reisisoft.sigui.updater;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

public class Updater {
    private static boolean isWindows = System.getProperty("os.name").toLowerCase().contains("wind");

    private static final String updaterUrl = "https://tdf.io/kotlinsiguiupdaterendpoint";
    private static final Path siGuiInstallFolder = Paths.get(".", "kotlin-si-gui").toAbsolutePath().normalize();
    private static final String settingFilename = "si-gui.settings.json";
    private static final Path scriptFilePath = siGuiInstallFolder.resolve("si-gui-desktop.jar");

    public static void main(String[] args) {
        int exitValue;
        try {
            final String installedeTag = loadCurrentVersionFromDisk();
            final Map.Entry<String, HttpURLConnection> online = loadLastOnlineVersion();

            if (installedeTag.equals(online.getKey())) {
                //Same eTag -> no update needed
                exitValue = startSiGui().waitFor();
            } else {
                exitValue = downloadAndStartSiGui(online.getKey(), online.getValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Better exception handling needed!");
        }
        System.exit(exitValue);
    }

    private static int downloadAndStartSiGui(String eTag, HttpURLConnection urlConnection) throws Exception {
        final Path tmpFile = Paths.get(".", eTag + ".jar");
        Files.deleteIfExists(tmpFile);
        try {
            try (final InputStream onlineIn = urlConnection.getInputStream();
                 final OutputStream fileOut = Files.newOutputStream(tmpFile, StandardOpenOption.CREATE_NEW)) {
                final float length = urlConnection.getContentLength();
                Map.Entry<JDialog, JProgressBar> progressWindow = openProgressWindow();
                int totalRead = 0;
                int read;
                float realPercentage;
                byte[] buffer = new byte[1024 * 32];
                while ((read = onlineIn.read(buffer)) >= 0) {
                    totalRead += read;
                    fileOut.write(buffer, 0, read);
                    realPercentage = totalRead / length;
                    String formatted = String.format(Locale.US, "%.2f%%", realPercentage * 100);
                    progressWindow.getValue().setValue(Math.round(realPercentage * 100 * 100));
                    System.out.println("Current progress: " + formatted);
                }
                progressWindow.getKey().setVisible(false);
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
            Files.deleteIfExists(scriptFilePath);
            Files.move(tmpFile, scriptFilePath);

            boolean canExec = scriptFilePath.toFile().setExecutable(true, false);
            if (!isWindows && !canExec)
                throw new IllegalStateException("Cannot make " + scriptFilePath + " executable...");
            //Start SI GUI
            Process sigui = startSiGui();
            //Save etag
            try (BufferedWriter writer = Files.newBufferedWriter(lastUpdatePath, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                writer.write(eTag);
            }
            return sigui.waitFor();
        } finally {
            Files.deleteIfExists(tmpFile);
        }
    }


    private static String loadCurrentVersionFromDisk() {
        try {
            try (BufferedReader reader = Files.newBufferedReader(lastUpdatePath, StandardCharsets.UTF_8)) {
                final String tmp = reader.readLine();
                if (tmp == null)
                    throw new IllegalStateException();
                return tmp;
            }

        } catch (Exception e) {
            return "";
        }
    }

    private static Map.Entry<String, HttpURLConnection> loadLastOnlineVersion() throws IOException {
        final URL url = new URL(updaterUrl);

        final HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        final String etag = con.getHeaderField("ETag");

        return new AbstractMap.SimpleImmutableEntry(etag.substring(1, etag.length() - 1), con);
    }


    private static final Path lastUpdatePath = Paths.get(".", "last_update.txt");

    private static Process startSiGui() throws Exception {
        final File execFolder = scriptFilePath.getParent().toFile();
        return new ProcessBuilder("java", "-jar", "si-gui-desktop.jar").directory(execFolder).inheritIO().start();
    }

    private static Map.Entry<JDialog, JProgressBar> openProgressWindow() {
        JPanel container = new JPanel();
        container.setLayout(new BorderLayout());
        container.setVisible(true);
        JProgressBar progressBar = new JProgressBar(0, 10000);
        progressBar.setSize(300, 100);

        container.add(BorderLayout.CENTER, progressBar);

        JDialog dialog = new JDialog((Window) null);
        dialog.setTitle("Updating Kotlin SI-GUI...");

        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setSize(300, 100);
        dialog.add(container);
        dialog.setLocationRelativeTo(null); //Center on screen
        dialog.setVisible(true);
        return new AbstractMap.SimpleImmutableEntry<>(dialog, progressBar);
    }
}