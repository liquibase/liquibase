// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.*;
import liquibase.resource.CompositeFileOpener;
import liquibase.resource.FileOpener;
import liquibase.resource.FileSystemFileOpener;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Liquibase MOJO that requires the user to provide a DatabaseChangeLogFile to be able
 * to perform any actions on the database.
 * @author Peter Murray
 */
public abstract class AbstractLiquibaseChangeLogMojo extends AbstractLiquibaseMojo {

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

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    if (changeLogFile == null) {
      throw new MojoFailureException("The changeLogFile must be specified.");
    }
  }

  /**
   * Performs the actual Liquibase task on the database using the fully configured {@link
   * liquibase.Liquibase}.
   * @param liquibase The {@link liquibase.Liquibase} that has been fully
   * configured to run the desired database task.
   */
  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    if (dropFirst) {
      liquibase.dropAll();
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "changeLogFile: " + changeLogFile);
    getLog().info(indent + "drop first? " + dropFirst);
    getLog().info(indent + "context(s): " + contexts);
  }

  @Override
  protected FileOpener getFileOpener(ClassLoader cl) {
    FileOpener mFO = new MavenFileOpener(cl);
    FileOpener fsFO = new FileSystemFileOpener(project.getBasedir().getAbsolutePath());
    return new CompositeFileOpener(mFO, fsFO);
  }

  @Override
  protected Liquibase createLiquibase(FileOpener fo, Database db) throws MojoExecutionException {
      String changeLog = changeLogFile == null ? "" : changeLogFile.trim();
      return new Liquibase(changeLog, fo, db);
  }
}
