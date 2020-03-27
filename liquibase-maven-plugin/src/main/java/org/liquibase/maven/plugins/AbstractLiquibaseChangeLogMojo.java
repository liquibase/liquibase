// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A Liquibase MOJO that requires the user to provide a DatabaseChangeLogFile to be able
 * to perform any actions on the database.
 *
 * @author Peter Murray
 */
public abstract class AbstractLiquibaseChangeLogMojo extends AbstractLiquibaseMojo {

    /**
   * Specifies the directory where Liquibase can find your <i>changelog</i> file.
     *
   * @parameter property="liquibase.changeLogDirectory"
     */
    protected String changeLogDirectory;

    /**
     * Specifies the <i>changelog</i> file for Liquibase to use.
     *
     * @parameter property="liquibase.changeLogFile"
     */
    protected String changeLogFile;


    /**
     * Specifies which contexts Liquibase will execute, which can be separated by a commaif multiple contexts
      are required.
   * If a context is not specified, then ALL contexts will be executed.
     *
     * @parameter property="liquibase.contexts" default-value=""
     */
    protected String contexts;

    /**
     * Specifies which Liquibase labels Liquibase will execute, which can be separated by a commaif multiple labels
      are required or you need to designate a more complex expression.
   * If a label is not specified, then ALL labels will be executed.
     *
     * @parameter property="liquibase.labels" default-value=""
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
     *
     * @param liquibase The {@link liquibase.Liquibase} that has been fully
     *                  configured to run the desired database task.
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
    protected ResourceAccessor getResourceAccessor(ClassLoader cl) {
        List<ResourceAccessor> resourceAccessors = new ArrayList<ResourceAccessor>();
        resourceAccessors.add(new MavenResourceAccessor(cl));
        resourceAccessors.add(new FileSystemResourceAccessor(project.getBasedir()));
        resourceAccessors.add(new ClassLoaderResourceAccessor(getClass().getClassLoader()));

        if (changeLogDirectory != null) {
            calculateChangeLogDirectoryAbsolutePath();
            resourceAccessors.add(new FileSystemResourceAccessor(new File(changeLogDirectory)));
        }

        return new CompositeResourceAccessor(resourceAccessors);
    }

    @Override
    protected Liquibase createLiquibase(Database db) throws MojoExecutionException {

        String changeLog = (changeLogFile == null) ? "" : changeLogFile.trim();
        return new Liquibase(changeLog, Scope.getCurrentScope().getResourceAccessor(), db);

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
