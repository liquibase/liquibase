package liquibase.log;

import java.util.logging.Level;
import java.util.logging.Logger;

public class LogFactory {
    public static Logger getLogger() {
        return Logger.getLogger("liquibase");
    }

  public static void setLoggingLevel(String logLevel) {
    if ("all".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.ALL);
    } else if ("finest".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.FINEST);
    } else if ("finer".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.FINER);
    } else if ("fine".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.FINE);
    } else if ("info".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.INFO);
    } else if ("warning".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.WARNING);
    } else if ("severe".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.SEVERE);
    } else if ("off".equalsIgnoreCase(logLevel)) {
      getLogger().setLevel(Level.OFF);
    } else {
      throw new IllegalArgumentException("Unknown log level: " + logLevel);
    }
  }
}
