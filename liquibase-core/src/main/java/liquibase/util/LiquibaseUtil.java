package liquibase.util;

import liquibase.Scope;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class LiquibaseUtil {

    private static Properties liquibaseBuildProperties;

    public static String getBuildVersion() {
        return getBuildInfo("build.version");
    }

    public static String getBuildTime() {
        return getBuildInfo("build.timestamp");
    }

    public static String getBuildNumber() {
        return getBuildInfo("build.number");
    }

    // will extract the information from liquibase.build.properties, which should be a properties file in
    // the jar file.
    private static String getBuildInfo(String propertyId) {
        String value = "UNKNOWN";
        if (liquibaseBuildProperties == null) {
          Boolean osgiPlatform = Scope.getCurrentScope().get(Scope.Attr.osgiPlatform, Boolean.class);
          if (Boolean.TRUE.equals(osgiPlatform)) {
            Bundle bundle = FrameworkUtil.getBundle(LiquibaseUtil.class);
            URL propURL = bundle.getEntry("liquibase.build.properties");
            if (propURL == null) {
              Scope.getCurrentScope().getLog(LiquibaseUtil.class).severe("Cannot read liquibase.build.properties");
            } else {
              try (InputStream buildProperties = propURL.openStream()) {
                liquibaseBuildProperties = new Properties();
                if (buildProperties != null) {
                    liquibaseBuildProperties.load(buildProperties);
                }
              } catch (IOException e) {
                Scope.getCurrentScope().getLog(LiquibaseUtil.class).severe("Cannot read liquibase.build.properties", e);
              }            
            }
          } else {
            try (InputStream buildProperties = Scope.getCurrentScope().getClassLoader().getResourceAsStream("liquibase.build.properties")) {
                liquibaseBuildProperties = new Properties();
                if (buildProperties != null) {
                    liquibaseBuildProperties.load(buildProperties);
                }
            } catch (IOException e) {
                Scope.getCurrentScope().getLog(LiquibaseUtil.class).severe("Cannot read liquibase.build.properties", e);
            }
          }
        }

        if (liquibaseBuildProperties != null) {
            value = liquibaseBuildProperties.getProperty(propertyId);
        }
        return value;
    }

}
