package liquibase.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

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
