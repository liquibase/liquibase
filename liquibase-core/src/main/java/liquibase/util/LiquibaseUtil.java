package liquibase.util;

import liquibase.Scope;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LiquibaseUtil {

    private static Properties liquibaseBuildProperties;

    public static String getBuildVersion() {
        return getBuildInfo("build.version");
    }

    public static String getBuildTime() {
        return getBuildInfo("build.timestamp");
    }

    // will extract the information from either liquibase.build.properties, which should be a properties file in
    // the jar file, or from the jar file's MANIFEST.MF, which should also have similar information.
    private static String getBuildInfo(String propertyId) {
        if (liquibaseBuildProperties == null) {
            try (InputStream buildProperties =StreamUtil.openStream("liquibase.build.properties", false, null, new ClassLoaderResourceAccessor(LiquibaseUtil.class.getClassLoader()))) {
                liquibaseBuildProperties = new Properties();
                if (buildProperties != null) {
                    liquibaseBuildProperties.load(buildProperties);
                }
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(LiquibaseUtil.class).severe("Cannot read liquibase.build.properties", e);
            }
        }

        String value = liquibaseBuildProperties.getProperty(propertyId);
        if (value == null) {
            return "UNKNOWN";
        }
        return value;
    }
}
