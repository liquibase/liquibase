package org.liquibase.maven.plugins;

import java.io.*;
import java.lang.reflect.Field;
import java.net.*;
import java.sql.*;
import java.util.*;
import liquibase.migrator.Migrator;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.*;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 * A base class for providing Liquibase {@link liquibase.migrator.Migrator} functionality.
 * @author Peter Murray, Trace Financial Limited
 * @requiresDependencyResolution test
 */
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

  public static final String LOG_SEPARATOR =
          "------------------------------------------------------------------------";

  /**
   * Specifies the change log file to use for Liquibase.
   * @parameter expression=${liquibase.changeLogFile}
   */
  protected String changeLogFile;

  /**
   * The fully qualified name of the driver class to use to connect to the database.
   * @parameter expression=${liquibase.driver}
   */
  protected String driver;

  /**
   * The Database URL to connect to for executing Liquibase.
   * @parameter expression=${liquibase.url}
   */
  protected String url;

  /**
   * The database username to use to connect to the specified database.
   * @parameter expression="${liquibase.username}"
   */
  protected String username;

  /**
   * The database password to use to connect to the specified database.
   * @parameter expression="${liquibase.password}"
   */
  protected String password;

  /**
   * Whether or not to perform a drop on the database before executing the change.
   * @parameter expression="${liquibase.dropFirst}" default-value="false"
   */
  protected boolean dropFirst;

  /**
   * The Liquibase contexts to execute, which can be "," separated if multiple contexts
   * are required. If no context is specified then ALL contexts will be executed.
   * @parameter expression=${liquibase.contexts} default-value=""
   */
  protected String contexts;

  /**
   * Controls the prompting of users as to whether or not they really want to run the
   * changes on a database that is not local to the machine that the user is current
   * executing the plugin on.
   * @parameter expression="${liquibase.promptOnNonLocalDatabase}" default-value="true"
   */
  protected boolean promptOnNonLocalDatabase;

  /**
   * The Liquibase properties file used to configure the Liquibase
   * {@link liquibase.migrator.Migrator}.
   * @parameter expression=${liquibase.propertiesFile}
   */
  protected String propertiesFile;

  /**
   * Flag allowing for the Liquibase properties file to any settings provided in the
   * Maven plugin configuration. By default if a property is explicity specified it is not
   * overridden if it also appears in the properties file.
   * @parameter expression="${liquibase.propertiesFileOverrides}" default-value="false"
   */
  protected boolean propertiesFileOverrides;

  /**
   * The Maven project that plugin is running under.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /**
   * Controls the verbosity of the output from invoking the plugin.
   * @parameter expression="${liquibase.verbose}" default-value="false"
   * @description Controls the verbosity of the plugin when executing
   */
  protected boolean verbose;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info("");
    getLog().info("Liquibase Database Migrate");
    getLog().info(LOG_SEPARATOR);

    String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
    if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty).booleanValue()) {
      getLog().warn("Migrator did not run because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY
                    + "' system property was set to false");
      return;
    }

    // Load the properties file if there is one, but only for values that the user has not
    // already specified.
    if (propertiesFile != null) {
      File f = new File(propertiesFile);
      if (verbose) {
        getLog().info("Loading Liquibase settings from properties file, "
                      + f.getAbsolutePath());
      }
      parsePropertiesFile(propertiesFile);
    }

    if (verbose) {
      getLog().info("Settings----------------------------");
      getLog().info("   properties file will override? " + propertiesFileOverrides);
      getLog().info("   changeLogFile: " + changeLogFile);
      getLog().info("   driver: " + driver);
      getLog().info("   url: " + url);
      getLog().info("   username: " + username);
      getLog().info("   password: " + password);
      getLog().info("   prompt on non-local database? " + promptOnNonLocalDatabase);
      getLog().info("   drop first? " + dropFirst);
      getLog().info(LOG_SEPARATOR);
    }

    Connection connection = null;
    try {
      Driver dbDriver = (Driver)Class.forName(driver,
                                              true,
                                              getArtifactClassloader()).newInstance();

      Properties info = new Properties();
      info.put("user", username);
      info.put("password", password);
      connection = dbDriver.connect(url, info);

      if (connection == null) {
        throw new JDBCException("Connection could not be created to " + url
                                + " with driver " + dbDriver.getClass().getName()
                                + ".  Possibly the wrong driver for the given "
                                + "database URL");
      }

      FileOpener mFO = new MavenFileOpener();
      FileOpener fsFO = new FileSystemFileOpener();
      Migrator migrator = new Migrator(changeLogFile.trim(),
                                       new CompositeFileOpener(mFO, fsFO));
      migrator.setContexts(contexts);
      migrator.init(connection);

      getLog().info("Executing on Database: " + url);

      if (promptOnNonLocalDatabase && !migrator.isSafeToRunMigration()) {
        if (migrator.swingPromptForNonLocalDatabase()) {
          throw new LiquibaseException("Chose not to run against non-production database");
        }
      }

      if (dropFirst) {
        migrator.dropAll();
      }
      performLiquibaseTask(migrator);
    }
    catch (ClassNotFoundException e) {
      releaseConnection(connection);
      throw new MojoFailureException("Missing Class '" + e.getMessage() + "'. Database "
                                     + "driver may not be included in the project "
                                     + "dependencies or with wrong scope.");
    }
    catch (Exception e) {
      releaseConnection(connection);
      throw new MojoFailureException(e.getMessage());
    }
    getLog().info(LOG_SEPARATOR);
  }

  /**
   * Performs the actual Liquibase task on the database using the fully configured
   * {@link liquibase.migrator.Migrator}.
   * @param migrator The {@link liquibase.migrator.Migrator} that has been fully
   * configured to run the desired database task.
   * @throws MojoExecutionException
   */
  protected abstract void performLiquibaseTask(Migrator migrator) throws MojoExecutionException;


  /**
   * Obtains a {@link ClassLoader} that can load from the Maven project dependencies. If
   * the dependencies have not be resolved (or there are none) then this will just end up
   * delegating to the parent {@link ClassLoader} of this class.
   * @return The ClassLoader that can load the resolved dependencies for the Maven
   * project.
   * @throws java.net.MalformedURLException If any of the dependencies cannot be resolved into a
   * URL.
   */
  protected ClassLoader getArtifactClassloader() throws MalformedURLException {
    getLog().debug("Loading artfacts into URLClassLoader");
    URL[] urls = new URL[0];

    Set dependencies = project.getDependencyArtifacts();
    if (dependencies != null || !dependencies.isEmpty()) {
      Set artifactURLs = new HashSet(dependencies.size());

      for (Iterator it = dependencies.iterator(); it.hasNext();) {
        Artifact artifact = (Artifact)it.next();
        File f = artifact.getFile();
        if (f != null) {
          URL fileURL = f.toURI().toURL();
          getLog().debug("  artifact: " + fileURL);
          artifactURLs.add(fileURL);
        } else {
          getLog().warn("Artifact with no actual file, '" + artifact.getGroupId()
                        + ":" + artifact.getArtifactId() + "'");
        }
      }
      urls = (URL[])artifactURLs.toArray(new URL[artifactURLs.size()]);
    } else {
      getLog().debug("there are no resolved artifacts for the Maven project.");
    }
    return new URLClassLoader(urls, getClass().getClassLoader());
  }

  protected void releaseConnection(Connection c) {
    if (c != null) {
      try {
        c.close();
      }
      catch (SQLException e) {
        getLog().error("Failed to close open connection to database.", e);
      }
    }
  }

  /**
   * Parses a properties file and sets the assocaited fields in the plugin.
   * @param propertiesFile The file containing the Liquibase properties that need to be
   * parsed.
   * @throws org.apache.maven.plugin.MojoExecutionException If there is a problem parsing the file.
   */
  protected void parsePropertiesFile(String propertiesFile) throws MojoExecutionException {

    Properties props = new Properties();
    try {
      InputStream propertiesInputStream = new FileInputStream(propertiesFile);
      props.load(propertiesInputStream);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not load the properties Liquibase file", e);
    }

    for (Iterator it = props.keySet().iterator(); it.hasNext(); ) {
      String key = null;
      try {
        key = (String)it.next();
        Field field = AbstractLiquibaseMojo.class.getDeclaredField(key);
        Object currentValue = field.get(this);

        if (currentValue == null) {
          String value = props.get(key).toString();
          if (field.getType().equals(Boolean.class)) {
            field.set(this, Boolean.valueOf(value));
          } else {
            // Only set the value if the user has not already specified it or we are
            // explicity overriding the setting.
            if (propertiesFileOverrides || field.get(this) == null) {
              field.set(this, value);
            }
          }
        }
      }
      catch (Exception e) {
        getLog().warn(e);
        // May need to correct this to make it a more useful exception...
        throw new MojoExecutionException("Unknown parameter: '" + key + "'", e);
      }
    }
  }
}
