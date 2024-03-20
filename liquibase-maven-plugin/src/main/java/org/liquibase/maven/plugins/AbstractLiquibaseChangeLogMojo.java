// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.configuration.core.DeprecatedConfigurationValueProvider;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.*;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A Liquibase MOJO that requires the user to provide a DatabaseChangeLogFile to be able
 * to perform any actions on the database.
 *
 * @author Peter Murray
 */
public abstract class AbstractLiquibaseChangeLogMojo extends AbstractLiquibaseMojo {

    /**
   * Specifies the directory where Liquibase can find your <i>changelog</i> file. This is an aliases for searchPath
     *
   * @parameter property="liquibase.changeLogDirectory"
     */
    @PropertyElement
    protected String changeLogDirectory;

    /**
     * Specifies the <i>changelog</i> file for Liquibase to use.
     *
     * @parameter property="liquibase.changeLogFile"
     */
    @PropertyElement
    protected String changeLogFile;


    /**
     * Specifies which contexts Liquibase will execute, which can be separated by a comma if multiple contexts
      are required.
   * If a context is not specified, then ALL contexts will be executed.
     *
     * @parameter property="liquibase.contexts" default-value=""
     */
    @PropertyElement
    protected String contexts;

    /**
     * Deprecated version of labelFilter
     *
     * @parameter property="liquibase.labels" default-value=""
     * @deprecated
     */
    @PropertyElement
    @Deprecated
    protected String labels;

    /**
     * Specifies which Liquibase labels Liquibase will execute, which can be separated by a comma if multiple labels
     are required or you need to designate a more complex expression.
     * If a label is not specified, then ALL labels will be executed.
     *
     * @parameter property="liquibase.labelFilter" default-value=""
     */
    @PropertyElement
    protected String labelFilter;


    /**
     * How to handle multiple files being found in the search path that have duplicate paths.
     * Options are WARN (log warning and choose one at random) or ERROR (fail current operation)
     *
     * @parameter property="liquibase.duplicateFileMode" default-value="ERROR"
     */
    @PropertyElement
    protected String duplicateFileMode;

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
        if (StringUtil.isNotEmpty(duplicateFileMode)) {
            DeprecatedConfigurationValueProvider.setData(GlobalConfiguration.DUPLICATE_FILE_MODE.getKey(), GlobalConfiguration.DuplicateFileMode.valueOf(duplicateFileMode.toUpperCase(Locale.ROOT)));
        }
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "changeLogDirectory: " + changeLogDirectory);
        getLog().info(indent + "changeLogFile: " + changeLogFile);
        getLog().info(indent + "context(s): " + contexts);
        getLog().info(indent + "label(s): " + getLabelFilter());
    }

    @Override
    protected ResourceAccessor getResourceAccessor(ClassLoader cl) throws IOException, MojoFailureException {
        List<ResourceAccessor> resourceAccessors = new ArrayList<>();
        resourceAccessors.add(new MavenResourceAccessor(cl));
        resourceAccessors.add(new DirectoryResourceAccessor(project.getBasedir()));
        resourceAccessors.add(new ClassLoaderResourceAccessor(getClass().getClassLoader()));

        String finalSearchPath = searchPath;

        if (changeLogDirectory != null) {
            if (searchPath != null) {
                throw new MojoFailureException("Cannot specify searchPath and changeLogDirectory at the same time");
            }
            calculateChangeLogDirectoryAbsolutePath();
            finalSearchPath = changeLogDirectory;
        }

        return new SearchPathResourceAccessor(finalSearchPath, resourceAccessors.toArray(new ResourceAccessor[0]));
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

    public String getLabelFilter() {
        if (labelFilter == null) {
            return labels;
        }
        return labelFilter;
    }
}
