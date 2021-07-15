package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.RegisterChangelogCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.io.File;
import java.util.*;


/**
 * <p>Registers a change log with Hub.</p>
 *
 * @author Wesley Willard
 * @goal registerChangeLog
 */
public class LiquibaseRegisterChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Specifies the <i>Liquibase Hub Project ID</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     */
    @PropertyElement
    protected String hubProjectId;

    /**
     *
     * Specifies the <i>Liquibase Hub Project</i> for Liquibase to create and use.
     *
     * @parameter property="liquibase.hubProjectName"
     *
     */
    @PropertyElement
    protected String hubProjectName;

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();
        if (hubProjectId == null && hubProjectName == null) {
            throw new MojoFailureException("\nEither the Hub project ID or project name must be specified.");
        }
        if (hubProjectId != null && hubProjectName != null) {
            throw new MojoFailureException("\nThe 'registerchangelog' command failed because too many parameters were provided. Command expects project ID or new projectname, but not both.\n");
        }
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        CommandScope registerChangeLog = new CommandScope("registerChangeLog");
        registerChangeLog
                .addArgumentValue(RegisterChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(RegisterChangelogCommandStep.HUB_PROJECT_ID_ARG, (hubProjectId != null ? UUID.fromString(hubProjectId) : null))
                .addArgumentValue(RegisterChangelogCommandStep.HUB_PROJECT_NAME_ARG, hubProjectName);

        registerChangeLog.addArgumentValue("changeLogFile", changeLogFile);
        registerChangeLog.addArgumentValue("database", database);
        registerChangeLog.addArgumentValue("liquibase", liquibase);
        registerChangeLog.addArgumentValue("changeLog", liquibase.getDatabaseChangeLog());

        registerChangeLog.execute();
    }

    /**
     *
     * Override this method in order to create a ResourceAccessor which only
     * looks for files in root and src/main/resources paths
     *
     * @param   cl
     * @return  ResourceAccessor
     *
     */
    @Override
    protected ResourceAccessor getResourceAccessor(ClassLoader cl) {
        List<ResourceAccessor> resourceAccessors = new ArrayList<ResourceAccessor>();
        File baseDir = project.getBasedir();
        File sourceDir = new File(baseDir, "src/main/resources");
        resourceAccessors.add(new FileSystemResourceAccessor(baseDir, sourceDir));
        return new CompositeResourceAccessor(resourceAccessors);
    }
}
