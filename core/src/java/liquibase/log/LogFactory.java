package liquibase.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

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
  
  
  /**
   * @param logLevel
   * @param logFile
   */
  public static void setLoggingLevel(String logLevel, String logFile) {
	  Handler fH;
	  
	  try {
		  fH = new FileHandler(logFile);
	  } catch (IOException e) {
		  throw new IllegalArgumentException("Cannot open log file "+logFile+". Reason: "+e.getMessage());
	  }

	  fH.setFormatter(new SimpleFormatter());
	  getLogger().addHandler(fH);
	  getLogger().setUseParentHandlers(false);
	  setLoggingLevel(logLevel);
	  
  } // end of method setLoggingLevel(String logLevel, String logFile)
  
}
