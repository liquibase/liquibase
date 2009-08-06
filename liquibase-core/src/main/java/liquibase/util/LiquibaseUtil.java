package liquibase.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class LiquibaseUtil {
    public static String getBuildVersion() {
        Properties buildInfo = new Properties();
        ClassLoader classLoader = LiquibaseUtil.class.getClassLoader();

        URL buildInfoFile = classLoader.getResource("buildinfo.properties");
        try {
            if (buildInfoFile == null) {
                return "UNKNOWN";
            } else {
                InputStream in = buildInfoFile.openStream();

                buildInfo.load(in);
                String o = (String) buildInfo.get("build.version");
                if (o == null) {
                    return "UNKNOWN";
                } else {
                    return o;
                }
            }
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }
}
