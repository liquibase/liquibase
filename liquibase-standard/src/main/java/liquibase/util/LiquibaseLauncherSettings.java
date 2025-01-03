package liquibase.util;

/**
 * Convenience class for reading well known Liquibase command line settings (System and/or environment properties).
 *
 * @see LiquibaseLauncher
 */
public class LiquibaseLauncherSettings {

  private static final String LIQUIBASE_HOME_JVM_PROPERTY_NAME = "liquibase.home";
  private static final String LIQUIBASE_LAUNCHER_DEBUG_JVM_PROPERTY_NAME = "liquibase.launcher.debug";
  private static final String LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER_JVM_PROPERTY_NAME = "liquibase.launcher.parent_classloader";
  private static final String LIQUIBASE_CLASSPATH_JVM_PROPERTY_NAME = "liquibase.classpath";
  private static final String LIQUIBASE_INCLUDE_SYSTEM_CLASSPATH_NAME = "liquibase.includeSystemClasspath";
  private static final String LIQUIBASE_DEFAULTS_FILE_JVM_PROPERTY_NAME = "liquibase.defaultsFile";



  /**
   * Agglutinates the different settings, i.e., environment variables or associated JVM system properties, that can be
   * used for customizing the behavior of the class.
   */
  public enum LiquibaseLauncherSetting {
    LIQUIBASE_HOME(LIQUIBASE_HOME_JVM_PROPERTY_NAME),
    LIQUIBASE_LAUNCHER_DEBUG(LIQUIBASE_LAUNCHER_DEBUG_JVM_PROPERTY_NAME),
    LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER(LIQUIBASE_LAUNCHER_PARENT_CLASSLOADER_JVM_PROPERTY_NAME),
    LIQUIBASE_CLASSPATH(LIQUIBASE_CLASSPATH_JVM_PROPERTY_NAME),
    LIQUIBASE_INCLUDE_SYSTEM_CLASSPATH(LIQUIBASE_INCLUDE_SYSTEM_CLASSPATH_NAME),
    LIQUIBASE_DEFAULTS_FILE(LIQUIBASE_DEFAULTS_FILE_JVM_PROPERTY_NAME);


    private final String jvmPropertyName;

    LiquibaseLauncherSetting(String jvmPropertyName) {
      this.jvmPropertyName = jvmPropertyName;
    }

    String getJvmPropertyName() {
      return this.jvmPropertyName;
    }
  }

  public static String getSetting(LiquibaseLauncherSetting setting) {
    String value = System.getProperty(setting.getJvmPropertyName());
    if (value != null) {
      return value;
    }

    return System.getenv(setting.name());
  }
}
