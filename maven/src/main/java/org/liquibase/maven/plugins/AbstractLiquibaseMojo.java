package org.liquibase.maven.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.exception.*;
import liquibase.log.LogFactory;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 * A base class for providing Liquibase {@link liquibase.Liquibase} functionality.
 * @author Peter Murray
 * @requiresDependencyResolution test
 */
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

  /** Suffix for fields that are representing a default value for a another field. */
  private static final String DEFAULT_FIELD_SUFFIX = "Default";

  /**
   * The fully qualified name of the driver class to use to connect to the database.
   * @parameter expression="${liquibase.driver}"
   */
  protected String driver;

  /**
   * The Database URL to connect to for executing Liquibase.
   * @parameter expression="${liquibase.url}"
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
   * Use an empty string as the password for the database connection. This should not be
   * used along side the {@link #password} setting.
   * @parameter expression="${liquibase.emptyPassword}" default-value="false"
   */
  protected boolean emptyPassword;

  /**
   * Controls the prompting of users as to whether or not they really want to run the
   * changes on a database that is not local to the machine that the user is current
   * executing the plugin on.
   * @parameter expression="${liquibase.promptOnNonLocalDatabase}" default-value="true"
   */
  protected boolean promptOnNonLocalDatabase;

  private boolean promptOnNonLocalDatabaseDefault = true;

  /**
   * Allows for the maven project artifact to be included in the class loader for
   * obtaining the Liquibase property and DatabaseChangeLog files.
   * @parameter expression="${liquibase.includeArtifact}" default-value="true"
   */
  protected boolean includeArtifact;

  private boolean includeArtifactDefault = true;

  /**
   * Controls the verbosity of the output from invoking the plugin.
   * @parameter expression="${liquibase.verbose}" default-value="false"
   * @description Controls the verbosity of the plugin when executing
   */
  protected boolean verbose;

  private boolean verboseDefault = false;

  /**
   * Controls the level of logging from Liquibase when executing. The value can be
   * "all", "finest", "finer", "fine", "info", "warning", "severe" or "off". The value is
   * case insensitive.
   * @parameter expression="${liquibase.logging}" default-value="INFO"
   * @description Controls the verbosity of the plugin when executing
   */
  protected String logging;

  private String loggingDefault = "INFO";

  /**
   * The Liquibase properties file used to configure the Liquibase {@link
   * liquibase.Liquibase}.
   * @parameter expression="${liquibase.propertiesFile}"
   */
  protected String propertiesFile;

  /**
   * Flag allowing for the Liquibase properties file to override any settings provided in
   * the Maven plugin configuration. By default if a property is explicity specified it is
   * not overridden if it also appears in the properties file.
   * @parameter expression="${liquibase.propertyFileWillOverride}" default-value="false"
   */
  protected boolean propertyFileWillOverride;

  /**
   * The Maven project that plugin is running under.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

  /** The {@link Liquibase} object used modify the database. */ 
  private Liquibase liquibase;

  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().info(MavenUtils.LOG_SEPARATOR);

    String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
    if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty).booleanValue()) {
      getLog().warn("LiquiBase did not run because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY
                    + "' system property was set to false");
      return;
    }

    ClassLoader artifactClassLoader = getMavenArtifactClassLoader();
    configureFieldsAndValues(getFileOpener(artifactClassLoader));

    try {
      LogFactory.setLoggingLevel(logging);
    }
    catch (IllegalArgumentException e) {
      throw new MojoExecutionException("Failed to set logging level: " + e.getMessage(),
                                       e);
    }

    // Displays the settings for the Mojo depending of verbosity mode.
    displayMojoSettings();

    // Check that all the parameters that must be specified have been by the user.
    checkRequiredParametersAreSpecified();

    Connection connection = null;
    try {
      String dbPassword = emptyPassword ? "" : password;
      connection = MavenUtils.getDatabaseConnection(artifactClassLoader,
                                                    driver,
                                                    url,
                                                    username,
                                                    dbPassword);

      liquibase = createLiquibase(getFileOpener(artifactClassLoader), connection);
      getLog().info("Executing on Database: " + url);

      if (isPromptOnNonLocalDatabase()) {
        if (!liquibase.isSafeToRunMigration()) {
          if (UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
            throw new LiquibaseException("User decided not to run against non-local database");
          }
        }
      }

      performLiquibaseTask(liquibase);
    }
    catch (LiquibaseException e) {
      cleanup(connection);
      throw new MojoExecutionException("Error setting up or running Liquibase: " + e.getMessage(), e);
    }

    cleanup(connection);
    getLog().info(MavenUtils.LOG_SEPARATOR);
    getLog().info("");
  }

  protected Liquibase getLiquibase() {
    return liquibase;
  }

  protected abstract void performLiquibaseTask(Liquibase liquibase)
          throws LiquibaseException;

  protected boolean isPromptOnNonLocalDatabase() {
    return promptOnNonLocalDatabase;
  }

  private void displayMojoSettings() {
    if (verbose) {
      getLog().info("Settings----------------------------");
      printSettings("    ");
      getLog().info(MavenUtils.LOG_SEPARATOR);
    }
  }

  protected Liquibase createLiquibase(FileOpener fo, Connection conn) throws MojoExecutionException {
      try {
          return new Liquibase("", fo, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
      } catch (JDBCException e) {
          throw new MojoExecutionException(e.getMessage());
      }
  }

  protected void configureFieldsAndValues(FileOpener fo)
          throws MojoExecutionException, MojoFailureException {
    // Load the properties file if there is one, but only for values that the user has not
    // already specified.
    if (propertiesFile != null) {
      getLog().info("Parsing Liquibase Properties File");
      getLog().info("  File: " + propertiesFile);
      try {
        InputStream is = fo.getResourceAsStream(propertiesFile);
        if (is == null) {
          throw new MojoFailureException("Failed to resolve the properties file.");
        }
        parsePropertiesFile(is);
        getLog().info(MavenUtils.LOG_SEPARATOR);
      }
      catch (IOException e) {
        throw new MojoExecutionException("Failed to resolve properties file", e);
      }
    }
  }

  protected ClassLoader getMavenArtifactClassLoader() throws MojoExecutionException {
    try {
      return MavenUtils.getArtifactClassloader(project,
                                               includeArtifact,
                                               getClass(),
                                               getLog(),
                                               verbose);
    }
    catch (MalformedURLException e) {
      throw new MojoExecutionException("Failed to create artifact classloader", e);
    }
  }

  protected FileOpener getFileOpener(ClassLoader cl) {
    FileOpener mFO = new MavenFileOpener(cl);
    FileOpener fsFO = new FileSystemFileOpener(project.getBasedir().getAbsolutePath());
    return new CompositeFileOpener(mFO, fsFO);
  }

  /**
   * Performs some validation after the properties file has been loaded checking that all
   * properties required have been specified.
   * @throws MojoFailureException If any property that is required has not been
   * specified.
   */
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    if (driver == null) {
      throw new MojoFailureException("The driver has not been specified either as a "
                                     + "parameter or in a properties file.");
    } else if (url == null) {
      throw new MojoFailureException("The database URL has not been specified either as "
                                     + "a parameter or in a properties file.");
    }

    if (password != null && emptyPassword) {
      throw new MojoFailureException("A password cannot be present and the empty "
                                     + "password property both be specified.");
    }
  }

  /**
   * Prints the settings that have been set of defaulted for the plugin. These will only
   * be shown in verbose mode.
   * @param indent The indent string to use when printing the settings.
   */
  protected void printSettings(String indent) {
    if (indent == null) {
      indent = "";
    }
    getLog().info(indent + "driver: " + driver);
    getLog().info(indent + "url: " + url);
    getLog().info(indent + "username: " + username);
    getLog().info(indent + "password: " + password);
    getLog().info(indent + "use empty password: " + emptyPassword);
    getLog().info(indent + "prompt on non-local database? " + promptOnNonLocalDatabase);
    getLog().info(indent + "properties file will override? " + propertyFileWillOverride);
  }

  protected void cleanup(Connection c) {
    // Release any locks that we may have on the database.
    if (getLiquibase() != null) {
      try {
        getLiquibase().forceReleaseLocks();
      }
      catch (LockException e) {
        getLog().error(e.getMessage(), e);
      }
      catch (IOException e) {
        getLog().error(e.getMessage(), e);
      }
      catch (JDBCException e) {
        getLog().error(e.getMessage(), e);
      }
    }

    // Clean up the connection
    if (c != null) {
      try {
        c.rollback();
        c.close();
      }
      catch (SQLException e) {
        getLog().error("Failed to close open connection to database.", e);
      }
    }
  }

  /**
   * Parses a properties file and sets the assocaited fields in the plugin.
   * @param propertiesInputStream The input stream which is the Liquibase properties that
   * needs to be parsed.
   * @throws org.apache.maven.plugin.MojoExecutionException If there is a problem parsing
   * the file.
   */
  protected void parsePropertiesFile(InputStream propertiesInputStream)
          throws MojoExecutionException {
    if (propertiesInputStream == null) {
      throw new MojoExecutionException("Properties file InputStream is null.");
    }
    Properties props = new Properties();
    try {
      props.load(propertiesInputStream);
    }
    catch (IOException e) {
      throw new MojoExecutionException("Could not load the properties Liquibase file", e);
    }

    for (Iterator it = props.keySet().iterator(); it.hasNext();) {
      String key = null;
      try {
        key = (String)it.next();
        Field field = MavenUtils.getDeclaredField(this.getClass(), key);

        if (propertyFileWillOverride) {
          getLog().debug("  properties file setting value: " + field.getName());
          setFieldValue(field, props.get(key).toString());
        } else {
          if (!isCurrentFieldValueSpecified(field)) {
            getLog().debug("  properties file setting value: " + field.getName());
            setFieldValue(field, props.get(key).toString());
          }
        }
      }
      catch (Exception e) {
        getLog().info("  '" + key + "' in properties file is not being used by this "
                      + "task.");
      }
    }
  }

  /**
   * This method will check to see if the user has specified a value different to that of
   * the default value. This is not an ideal solution, but should cover most situations in
   * the use of the plugin.
   * @param f The Field to check if a user has specified a value for.
   * @return <code>true</code> if the user has specified a value.
   */
  private boolean isCurrentFieldValueSpecified(Field f) throws IllegalAccessException {
    Object currentValue = f.get(this);
    if (currentValue == null) {
      return false;
    }

    Object defaultValue = getDefaultValue(f);
    if (defaultValue == null) {
      return currentValue != null;
    } else {
      // There is a default value, check to see if the user has selected something other
      // than the default
      return !defaultValue.equals(f.get(this));
    }
  }

  private Object getDefaultValue(Field field) throws IllegalAccessException {
    List<Field> allFields = new ArrayList<Field>();
    allFields.addAll(Arrays.asList(getClass().getDeclaredFields()));
    allFields.addAll(Arrays.asList(AbstractLiquibaseMojo.class.getDeclaredFields()));

    for (Field f : allFields) {
      if (f.getName().equals(field.getName() + DEFAULT_FIELD_SUFFIX)) {
        f.setAccessible(true);
        return f.get(this);
      }
    }
    return null;
  }

  private void setFieldValue(Field field, String value) throws IllegalAccessException {
    if (field.getType().equals(Boolean.class) || field.getType().equals(boolean.class)) {
      field.set(this, Boolean.valueOf(value));
    } else {
      field.set(this, value);
    }
  }
}
