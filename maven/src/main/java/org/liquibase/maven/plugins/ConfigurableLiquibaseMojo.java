// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.*;
import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.exception.JDBCException;
import liquibase.migrator.Migrator;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Configurable Liquibase Mojo for Maven that provides more settings and the ability to
 * override these settings using Properties files when running the Liquibase Migrator.
 * @author Peter Murray
 */
public abstract class ConfigurableLiquibaseMojo extends AbstractLiquibaseMojo {

  /** Suffix for fields that are representing a default value for a another field. */
  private static final String DEFAULT_FIELD_SUFFIX = "Default";

  /**
   * Specifies the change log file to use for Liquibase.
   * @parameter expression="${liquibase.changeLogFile}"
   */
  protected String changeLogFile;

  /**
   * Whether or not to perform a drop on the database before executing the change.
   * @parameter expression="${liquibase.dropFirst}" default-value="false"
   */
  protected boolean dropFirst;

  private boolean dropFirstDefault = false;

  /**
   * The Liquibase contexts to execute, which can be "," separated if multiple contexts
   * are required. If no context is specified then ALL contexts will be executed.
   * @parameter expression="${liquibase.contexts}" default-value=""
   */
  protected String contexts;

  private String contextsDefault = "";

  /**
   * The Liquibase properties file used to configure the Liquibase {@link
   * liquibase.migrator.Migrator}.
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
   * Performs extra {@link liquibase.migrator.Migrator} configuration as required by the
   * extending class. By default this method does nothing, but sub classes can override
   * this method to perform extra configuration steps on the {@link
   * liquibase.migrator.Migrator}.
   * @param migrator The {@link liquibase.migrator.Migrator} to perform the extra
   * configuration on.
   */
  protected void performMigratorConfiguration(Migrator migrator)
          throws MojoExecutionException {
  }

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    if (changeLogFile == null) {
      throw new MojoFailureException("The changeLogFile must be specified.");
    }
  }

  /**
   * Performs the actual Liquibase task on the database using the fully configured {@link
   * liquibase.migrator.Migrator}.
   * @param migrator The {@link liquibase.migrator.Migrator} that has been fully
   * configured to run the desired database task.
   */
  protected void performLiquibaseTask(Migrator migrator) throws LiquibaseException {
    if (dropFirst) {
      migrator.dropAll();
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "properties file will override? " + propertyFileWillOverride);
    getLog().info(indent + "changeLogFile: " + changeLogFile);
    getLog().info(indent + "drop first? " + dropFirst);
    getLog().info(indent + "context(s): " + contexts);
  }

  protected FileOpener getFileOpener(ClassLoader cl) {
    FileOpener mFO = new MavenFileOpener(cl);
    FileOpener fsFO = new FileSystemFileOpener(project.getBasedir().getAbsolutePath());
    return new CompositeFileOpener(mFO, fsFO);
  }

  @Override
  protected void configureFieldsAndValues(FileOpener fo)
          throws MojoExecutionException, MojoFailureException {
    super.configureFieldsAndValues(fo);
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

  protected Migrator createMigrator(FileOpener fo, Connection conn) throws MojoExecutionException {
      try {
          return new Migrator(changeLogFile.trim(),
                              fo,
                              DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
      } catch (JDBCException e) {
          throw new MojoExecutionException(e.getMessage());
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
