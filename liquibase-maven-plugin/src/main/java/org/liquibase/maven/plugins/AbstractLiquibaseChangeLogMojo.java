// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.configuration.HubConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import liquibase.resource.ResourceWriter;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

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

    /**
     *
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hub.apiKey"
     *
     */
    protected String hubApiKey;

    /**
     *
     * Specifies the <i>Liquibase Hub URL</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hub.url"
     *
     */
    protected String hubUrl;

    /**
     * Specifies the <i>Liquibase Hub URL</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hub.mode"
     *
     */
    protected String hubMode;

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
        //
        // Store the Hub API key and URL for later use
        //
        HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class);
        if (StringUtil.isNotEmpty(hubApiKey)) {
            hubConfiguration.setLiquibaseHubApiKey(hubApiKey);
        }
        if (StringUtil.isNotEmpty(hubUrl)) {
            hubConfiguration.setLiquibaseHubUrl(hubUrl);
        }
        if (StringUtil.isNotEmpty(hubMode)) {
            hubConfiguration.setLiquibaseHubMode(hubMode);
        }
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
        return new MavenResourceAccessor(cl, project, changeLogDirectory);
    }

    @Override
    protected Liquibase createLiquibase(Database db) throws MojoExecutionException {

        String changeLog = (changeLogFile == null) ? "" : changeLogFile.trim();
        return new Liquibase(changeLog, Scope.getCurrentScope().getResourceAccessor(), db);

    }
}
