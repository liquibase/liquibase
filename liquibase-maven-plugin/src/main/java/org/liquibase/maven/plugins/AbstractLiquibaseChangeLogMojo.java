// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

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
   * The Liquibase contexts to execute, which can be "," separated if multiple contexts
   * are required. If no context is specified then ALL contexts will be executed.
   * @parameter expression="${liquibase.contexts}" default-value=""
   */
  protected String contexts;

    /**
     * The Liquibase labels to execute, which can be "," separated if multiple labels
     * are required or a more complex expression. If no label is specified then ALL all will be executed.
     * @parameter expression="${liquibase.labels}" default-value=""
     */
  protected String labels;

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
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "changeLogFile: " + changeLogFile);
    getLog().info(indent + "context(s): " + contexts);
      getLog().info(indent + "label(s): " + labels);
  }

  @Override
  protected ResourceAccessor getFileOpener(ClassLoader cl) {
    ResourceAccessor mFO = new MavenResourceAccessor(cl);
    ResourceAccessor fsFO = new FileSystemResourceAccessor(project.getBasedir().getAbsolutePath());
    return new CompositeResourceAccessor(mFO, fsFO);
  }

  @Override
  protected Liquibase createLiquibase(ResourceAccessor fo, Database db) throws MojoExecutionException {
        try {
            String changeLog = changeLogFile == null ? "" : changeLogFile.trim();
            return new Liquibase(changeLog, fo, db);
        } catch (LiquibaseException ex) {
            throw new MojoExecutionException("Error creating liquibase: "+ex.getMessage(), ex);
        }
  }
}
