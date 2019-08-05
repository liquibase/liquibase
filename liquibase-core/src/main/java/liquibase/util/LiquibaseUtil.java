package liquibase.util;

import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class LiquibaseUtil {

    public static String getBuildVersion() {
        String buildVersion = "UNKNOWN";
        Properties buildInfo = new Properties();
        ClassLoader classLoader = LiquibaseUtil.class.getClassLoader();

        URL buildInfoFile = classLoader.getResource("buildinfo.properties");
        InputStream in = null;
        try {
            if (buildInfoFile != null) {
                URLConnection connection = buildInfoFile.openConnection();
                connection.setUseCaches(false);
                in = connection.getInputStream();
                buildInfo.load(in);
                String o = (String) buildInfo.get("build.version");

                if (o != null) {
                    buildVersion = o;
                }
            } else {
                Class clazz = LiquibaseUtil.class;
                String className = clazz.getSimpleName() + ".class";
                String classPath = clazz.getResource(className).toString();
                if (classPath.startsWith("jar")) {
                    String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) +
                            "/META-INF/MANIFEST.MF";
                    Manifest manifest = null;
                    try {
                        manifest = new Manifest(new URL(manifestPath).openStream());
                    } catch (IOException e) {
                        throw new UnexpectedLiquibaseException("Cannot open a URL to the manifest of our own JAR file.");
                    }
                    Attributes attr = manifest.getMainAttributes();
                    buildVersion = attr.getValue("Bundle-Version");
                }
            }

        } catch (IOException e) {
            // This is not a fatal exception.
            // Build info will be returned as 'UNKNOWN'        }
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // TODO Log this error and remove the RuntimeException.
                    throw new RuntimeException("Failed to close InputStream in LiquibaseUtil.", e);
                }
            }
        }

        return buildVersion;
    }
}
