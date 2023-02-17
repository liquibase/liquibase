package liquibase.extension.testing.util;

import liquibase.Scope;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.InputStream;
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
        final Logger log = Scope.getCurrentScope().getLog(DownloadUtil.class);

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
            log.fine("Artifact " + groupId + ":" + artifactId + ":" + version + " is available at " + path);
        } else {
            log.info("Downloading " + url + " to " + path);

            try {
                final HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("GET");

                if (!pathAsFile.getParentFile().exists() && !pathAsFile.getParentFile().mkdirs()) {
                    throw new UnexpectedLiquibaseException("Could not create "+pathAsFile.getAbsolutePath()+" directory");
                }

                try (final InputStream response = connection.getInputStream();
                     final OutputStream file = Files.newOutputStream(pathAsFile.toPath())) {
                    StreamUtil.copy(response, file);
                }
                log.fine("Saved " + url + " to " + pathAsFile.getAbsolutePath());

                path = pathAsFile.toPath();
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException("Error downloading from " + url + ": " + e.getMessage(), e);
            }
        }

        try {
            System.out.println(path.toUri().toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return path;

    }
}
