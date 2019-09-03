package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.jar.Manifest;

import static java.util.Objects.requireNonNull;

public class LiquibaseUtil {

    private static final String PROPERTIES_KEY_BUILD_VERSION   = "build.version";
    private static final String PROPERTIES_KEY_BUILD_TIMESTAMP = "build.timestamp";

    private static final String MANIFEST_ATTRIBUTE_BUNDLE_VERSION = "Bundle-Version";
    private static final String MANIFEST_ATTRIBUTE_BUILD_TIME     = "Build-Time";

    private static final String UNKNOWN_BUILD_INFO = "UNKNOWN";

    public static String getBuildVersion() {
        return getBuildInfo(PROPERTIES_KEY_BUILD_VERSION, MANIFEST_ATTRIBUTE_BUNDLE_VERSION);
    }

    public static String getBuildTime() {
        return getBuildInfo(PROPERTIES_KEY_BUILD_TIMESTAMP, MANIFEST_ATTRIBUTE_BUILD_TIME);
    }

    private static String getBuildInfo(final String propertyId, final String manifestId) {
        requireNonNull(propertyId);
        requireNonNull(manifestId);

        // First try to read from MANIFEST.MF in current JAR
        String buildInfoValue = readFromManifest(manifestId);

        // Then try to read from buildinfo.properties in current JAR
        if (buildInfoValue == null || UNKNOWN_BUILD_INFO.equals(buildInfoValue)) {
            buildInfoValue = readFromProperties(propertyId);
        }

        // If still null - set to UNKNOWN
        if (buildInfoValue == null) {
            buildInfoValue = UNKNOWN_BUILD_INFO;
        }

        return buildInfoValue;
    }

    // package-private for unit test
    static String readFromManifest(final String manifestId) {
        final Class clazz = LiquibaseUtil.class;
        final String className = clazz.getSimpleName() + ".class";
        final String classPath = clazz.getResource(className).toString();
        if (classPath.startsWith("jar")) {
            final String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            try (final InputStream inputStream = new URL(manifestPath).openStream()) {
                final Manifest manifest = new Manifest(inputStream);
                return manifest.getMainAttributes().getValue(manifestId);
            } catch (final IOException e) {
                // TODO maybe better to swallow this exception like in readFromProperties()?
                throw new UnexpectedLiquibaseException("Cannot open a URL to the manifest of our own JAR file");
            }
        }
        return null;
    }

    // package-private for unit test
    static String readFromProperties(final String propertyId) {
        final URL buildInfoFileUrl = LiquibaseUtil.class.getClassLoader().getResource("buildinfo.properties");
        if (buildInfoFileUrl != null) {
            try {
                final URLConnection connection = buildInfoFileUrl.openConnection();
                connection.setUseCaches(false);
                try (InputStream inputStream = connection.getInputStream()) {
                    final Properties properties = new Properties();
                    properties.load(inputStream);
                    final String propertyValue = (String) properties.get(propertyId);
                    if (propertyValue != null) {
                        return propertyValue;
                    }
                }
            } catch (final IOException e) {
                // This is not a fatal exception, ignore
            }
        }
        return null;
    }

    private LiquibaseUtil() {
        // singleton
    }
}
