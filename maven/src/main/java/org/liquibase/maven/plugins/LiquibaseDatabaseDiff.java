// Version:   $Id: $
// Copyright: Copyright(c) 2008 Trace Financial Limited
package org.liquibase.maven.plugins;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import liquibase.Liquibase;
import liquibase.commandline.CommandLineUtils;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven Mojo for performing Database Diffs.
 * @author Peter Murray
 * @goal diff
 */
public class LiquibaseDatabaseDiff extends AbstractLiquibaseChangeLogMojo {

  /**
   * The fully qualified name of the driver class to use to connect to the base database.
   * If this is not specified, then the {@link #driver} will be used instead.
   * @parameter expression="${liquibase.baseDriver}"
   */
  protected String baseDriver;

  /**
   * The base database URL to connect to for executing Liquibase. If performing a diff
   * against a Hibernate config xml file, then use <b>"hibernate:PATH_TO_CONFIG_XML"</b>
   * as the URL. The path to the hibernate configuration file can be relative to the test
   * classpath for the Maven project.
   * @parameter expression="${liquibase.baseUrl}"
   */
  protected String baseUrl;

  /**
   * The base database username to use to connect to the specified database.
   * @parameter expression="${liquibase.baseUsername}"
   */
  protected String baseUsername;

  /**
   * The base database password to use to connect to the specified database. If this is
   * null then an empty password will be used. 
   * @parameter expression="${liquibase.basePassword}"
   */
  protected String basePassword;

  /**
   * The base database password to use to connect to the specified database. If this is
   * null then an empty password will be used.
   * @parameter expression="${liquibase.baseDefaultSchemaName}"
   */
  protected String baseDefaultSchemaName;

  /**
   * The diff change log file to output the differences to. If this is null then the
   * differences will be output to the screen.
   * @parameter expression="${liquibase.diffChangeLogFile}"
   */
  protected String diffChangeLogFile;

  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    ClassLoader cl = null;
    try {
      cl = getMavenArtifactClassLoader();
    }
    catch (MojoExecutionException e) {
      throw new LiquibaseException("Could not create the class loader, " + e, e);
    }

    Database db = liquibase.getDatabase();
    Database baseDatabase = CommandLineUtils.createDatabaseObject(cl, baseUrl, baseUsername, basePassword, baseDriver, baseDefaultSchemaName, null);

    getLog().info("Performing Diff on database " + db.toString());
    if (diffChangeLogFile != null) {
      try {
        CommandLineUtils.doDiffToChangeLog(diffChangeLogFile, baseDatabase, db);
        getLog().info("Differences written to Change Log File, " + diffChangeLogFile);
      }
      catch (IOException e) {
        throw new LiquibaseException(e);
      }
      catch (ParserConfigurationException e) {
        throw new LiquibaseException(e);
      }
    } else {
      CommandLineUtils.doDiff(baseDatabase, db);
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "baseDriver: " + baseDriver);
    getLog().info(indent + "baseUrl: " + baseUrl);
    getLog().info(indent + "baseUsername: " + baseUsername);
    getLog().info(indent + "basePassword: " + basePassword);
    getLog().info(indent + "baseDefaultSchema: " + baseDefaultSchemaName);
    getLog().info(indent + "diffChangeLogFile: " + diffChangeLogFile);
  }

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    if (baseUrl == null) {
      throw new MojoFailureException("A base database or hibernate configuration file "
                                     + "must be provided to perform a diff.");
    }

    if (baseUrl.startsWith("hibernate:")) {

    } else {
      if (baseUsername == null) {
        throw new MojoFailureException("The username cannot be null for the base "
                                       + "database when not using hibernate.");
      }
      if (basePassword == null) {
        basePassword = "";
      }
    }
  }

  @Override
  protected boolean isPromptOnNonLocalDatabase() {
    return false;
  }
}
