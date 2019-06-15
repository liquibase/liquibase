// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Liquibase MOJO that requires the user to provide a DatabaseChangeLogFile to be able
 * to perform any actions on the database.
 * @author Peter Murray
 */
public abstract class AbstractLiquibaseChangeLogMojo extends AbstractLiquibaseMojo {

  /**
   * Specifies the change log directory into which liquibase can find the change log file.
   *
   * @parameter expression="${liquibase.changeLogDirectory}"
   */
  protected String changeLogDirectory;

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
    getLog().info(indent + "changeLogDirectory: " + changeLogDirectory);
    getLog().info(indent + "changeLogFile: " + changeLogFile);
    getLog().info(indent + "context(s): " + contexts);
    getLog().info(indent + "label(s): " + labels);
  }

  @Override
  protected ResourceAccessor getFileOpener(ClassLoader cl) {
    List<ResourceAccessor> resourceAccessors = new ArrayList<ResourceAccessor>();
    resourceAccessors.add(new MavenResourceAccessor(cl));
    resourceAccessors.add(new FileSystemResourceAccessor(project.getBasedir().getAbsolutePath()));

    if (changeLogDirectory != null) {
      calculateChangeLogDirectoryAbsolutePath();
      resourceAccessors.add(new FileSystemResourceAccessor(changeLogDirectory));
    }

    return new CompositeResourceAccessor(resourceAccessors);
  }

  @Override
  protected Liquibase createLiquibase(ResourceAccessor fo, Database db) throws MojoExecutionException {

            String changeLog = (changeLogFile == null) ? "" : changeLogFile.trim();
            return new Liquibase(changeLog, fo, db);

  }

  private void calculateChangeLogDirectoryAbsolutePath() {
    if (changeLogDirectory != null) {
      // convert to standard / if using absolute path on windows
      changeLogDirectory = changeLogDirectory.trim().replace('\\', '/');
      // try to know if it's an absolute or relative path : the absolute path case is simpler and don't need more actions
      File changeLogDirectoryFile = new File(changeLogDirectory);
      if (!changeLogDirectoryFile.isAbsolute()) {
        // we are in the relative path case
        changeLogDirectory = project.getBasedir().getAbsolutePath().replace('\\', '/') + "/" + changeLogDirectory;
      }
    }
  }
}
