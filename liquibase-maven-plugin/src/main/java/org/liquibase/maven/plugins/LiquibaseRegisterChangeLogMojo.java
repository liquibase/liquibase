package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.command.core.RegisterChangeLogCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.UUID;

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
    protected String hubProjectId;

    /**
     *
     * Specifies the <i>Liquibase Hub Project</i> for Liquibase to create and use.
     *
     * @parameter property="liquibase.hubProjectName"
     *
     */
    protected String hubProjectName;

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();
        if (hubProjectId == null && hubProjectName == null) {
            throw new MojoFailureException("\nThe Hub project ID must be specified.");
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
        registerChangeLog.addArgumentValues(
                RegisterChangeLogCommand.CHANGELOG_FILE_ARG.of(changeLogFile),
                RegisterChangeLogCommand.HUB_PROJECT_ID_ARG.of(UUID.fromString(hubProjectId)),
                RegisterChangeLogCommand.HUB_PROJECT_NAME_ARG.of(hubProjectName)
        );

        registerChangeLog.addArgumentValue("changeLogFile", changeLogFile);
        registerChangeLog.addArgumentValue("database", database);
        registerChangeLog.addArgumentValue("liquibase", liquibase);
        registerChangeLog.addArgumentValue("changeLog", liquibase.getDatabaseChangeLog());

        registerChangeLog.execute();
    }
}
