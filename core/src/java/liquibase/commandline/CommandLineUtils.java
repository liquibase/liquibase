// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package liquibase.commandline;

import javax.xml.parsers.ParserConfigurationException;
import java.sql.Driver;
import java.sql.Connection;
import java.util.Properties;
import java.io.*;
import liquibase.database.*;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;
import liquibase.diff.*;

/**
 * Common Utilitiy methods used in the CommandLine application and the Maven plugin.
 * These methods were orignally moved from {@link Main} so they could be shared.
 * @author Peter Murray
 */
public class CommandLineUtils {

  public static Database createDatabaseObject(ClassLoader classLoader,
                                              String url,
                                              String username,
                                              String password,
                                              String driver,
                                              String defaultSchemaName,
                                              String databaseClass) throws JDBCException {
      if (driver == null) {
          driver = DatabaseFactory.getInstance().findDefaultDriver(url);
      }

      try {
          if (url.startsWith("hibernate:")) {
              return (Database) Class.forName(HibernateDatabase.class.getName(), true, classLoader).getConstructor(String.class).newInstance(url.substring("hibernate:".length()));
          }

          Driver driverObject;
          DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
          if (databaseClass != null) {
              databaseFactory.addDatabaseImplementation((Database) Class.forName(databaseClass, true, classLoader).newInstance());
          }

          try {
              if (driver == null) {
                  driver = databaseFactory.findDefaultDriver(url);
              }

              if (driver == null) {
                  throw new RuntimeException("Driver class was not specified and could not be determined from the url");
              }

              driverObject = (Driver) Class.forName(driver, true, classLoader).newInstance();
          } catch (Exception e) {
              throw new RuntimeException("Cannot find database driver: " + e.getMessage());
          }
          Properties info = new Properties();
          info.put("user", username);
          if (password != null) {
              info.put("password", password);
          }

          Connection connection = driverObject.connect(url, info);
          if (connection == null) {
              throw new JDBCException("Connection could not be created to " + url + " with driver " + driver.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
          }

          Database database = databaseFactory.findCorrectDatabaseImplementation(connection);
          database.setDefaultSchemaName(StringUtils.trimToNull(defaultSchemaName));
          return database;
      } catch (Exception e) {
          throw new JDBCException(e);
      }
  }

  public static void doDiff(Database baseDatabase, Database targetDatabase) throws JDBCException {
      Diff diff = new Diff(baseDatabase, targetDatabase);
      diff.addStatusListener(new OutDiffStatusListener());
      DiffResult diffResult = diff.compare();

      System.out.println("");
      System.out.println("Diff Results:");
      diffResult.printResult(System.out);
  }

  public static void doDiffToChangeLog(String changeLogFile,
                                       Database baseDatabase,
                                       Database targetDatabase)
          throws JDBCException, IOException, ParserConfigurationException {
      Diff diff = new Diff(baseDatabase, targetDatabase);
      diff.addStatusListener(new OutDiffStatusListener());
      DiffResult diffResult = diff.compare();

      if (changeLogFile == null) {
          diffResult.printChangeLog(System.out, targetDatabase);
      } else {
          diffResult.printChangeLog(changeLogFile, targetDatabase);
      }
  }

  public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String defaultSchemaName) throws JDBCException, IOException, ParserConfigurationException {
        Diff diff = new Diff(originalDatabase, defaultSchemaName);
        diff.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = diff.compare();

        PrintStream outputStream = System.out;

        if (StringUtils.trimToNull(changeLogFile) != null) {
            File changeFile = new File(changeLogFile);
            outputStream = new PrintStream(changeFile);
        }
        diffResult.printChangeLog(outputStream, originalDatabase);
    }

  private static class OutDiffStatusListener implements DiffStatusListener {

    public void statusUpdate(String message) {
      System.err.println(message);
    }
  }
}
