package org.liquibase.maven.plugins;

import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;

import liquibase.FileOpener;
import liquibase.Liquibase;
import liquibase.UIFactory;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.*;
import org.apache.maven.project.MavenProject;

/**
 * A base class for providing Liquibase {@link liquibase.Liquibase}
 * functionality.
 * @author Peter Murray
 * @requiresDependencyResolution test
 */
public abstract class AbstractLiquibaseMojo extends AbstractMojo {

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
   * The Maven project that plugin is running under.
   * @parameter expression="${project}"
   * @required
   * @readonly
   */
  protected MavenProject project;

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

    // Displays the settings for the Mojo depending of verbosity mode.
    displayMojoSettings();

    // Check that all the parameters that must be specified have been by the user.
    checkRequiredParametersAreSpecified();

    Connection connection = null;
    try {
      connection = MavenUtils.getDatabaseConnection(artifactClassLoader,
                                                    driver,
                                                    url,
                                                    username,
                                                    password);

      Liquibase liquibase = createLiquibase(getFileOpener(artifactClassLoader), connection);
      getLog().info("Executing on Database: " + url);

      if (isPromptOnNonLocalDatabase() && !liquibase.isSafeToRunMigration()) {
        if (UIFactory.getInstance().getFacade().promptForNonLocalDatabase(liquibase.getDatabase())) {
          throw new LiquibaseException("User decided not to run against non-local database");
        }
      }

      performLiquibaseTask(liquibase);
    }
    catch (LiquibaseException e) {
      releaseConnection(connection);
      throw new MojoFailureException(e.getMessage());
    }

    releaseConnection(connection);
    getLog().info(MavenUtils.LOG_SEPARATOR);
    getLog().info("");
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
    return new MavenFileOpener();
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
    getLog().info(indent + "prompt on non-local database? " + promptOnNonLocalDatabase);
  }

  protected void releaseConnection(Connection c) {
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
}
