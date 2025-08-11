package liquibase.util;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DownloadUtil {

    private DownloadUtil() {

    }

    public static Path downloadMavenArtifact(String coordinate) {
        final String[] split = coordinate.split(":");
        if (split.length != 3) {
            throw new IllegalArgumentException("Maven coordinates must be in the form groupId:artifactId:version");
        }
        return downloadMavenArtifact(split[0], split[1], split[2]);

    }

    public static Path downloadMavenArtifact(String groupId, String artifactId, String version) {

        Path path = Paths.get(System.getProperty("user.home"),
                ".m2",
                "repository",
                groupId.replace(".", "/"),
                artifactId,
                version,
                artifactId + "-" + version + ".jar");

        final String url = "https://repo1.maven.org/maven2/" + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";

        final File pathAsFile = path.toFile();
        if (pathAsFile.exists()) {
            Scope.getCurrentScope().getLog(DownloadUtil.class).fine("Artifact " + groupId + ":" + artifactId + ":" + version + " is available at " + path);
        } else {
            path = downloadToFile(url, pathAsFile);
        }

        try {
            System.out.println(path.toUri().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return path;

    }

    /**
     * Download the file at the given URL to the given path.
     * @param url the URL to download
     * @param pathAsFile the path to save the file to
     * @return the path to the downloaded file
     */
    public static Path downloadToFile(String url, File pathAsFile) {
        final Logger log = Scope.getCurrentScope().getLog(DownloadUtil.class);
        log.info("Downloading " + url + " to " + pathAsFile);
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            if (!pathAsFile.getParentFile().exists() && !pathAsFile.getParentFile().mkdirs()) {
                throw new UnexpectedLiquibaseException("Could not create "+ pathAsFile.getAbsolutePath() + " directory");
            }

            try (final InputStream response = connection.getInputStream();
                 final OutputStream file = Files.newOutputStream(pathAsFile.toPath())) {
                StreamUtil.copy(response, file);
            }
            log.fine("Saved " + url + " to " + pathAsFile.getAbsolutePath());


        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Error downloading from " + url + ": " + e.getMessage(), e);
        }
        return pathAsFile.toPath();
    }

    /**
     * Fetch the content at the given URL as a string.
     * @param url the URL to fetch
     * @return the content as a string
     */
    public static String fetchAsString(String url) {
        final Logger log = Scope.getCurrentScope().getLog(DownloadUtil.class);
        log.fine("Fetching content from " + url);
        
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(10000); // 10 seconds
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
            connection.setRequestProperty("User-Agent", "Liquibase-LPM-Integration");

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    return response.toString();
                }
            } else {
                throw new UnexpectedLiquibaseException("HTTP request failed with response code: " + responseCode);
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException("Error fetching content from " + url + ": " + e.getMessage(), e);
        }
    }
}
