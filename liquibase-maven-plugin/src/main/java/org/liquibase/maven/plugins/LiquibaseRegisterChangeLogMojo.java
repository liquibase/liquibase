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
 * <p>Syncs all changes in change log with Hub.</p>
 *
 * @author Wesley Willard
 * @goal registerChangeLog
 */
public class LiquibaseRegisterChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     */
    protected String hubProjectId;

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        super.checkRequiredParametersAreSpecified();
        if (hubProjectId == null) {
            throw new MojoFailureException("\nThe Hub project ID must be specified.");
        }
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        CommandScope registerChangeLog = new CommandScope("registerChangeLog");
        registerChangeLog.addArguments(
                RegisterChangeLogCommand.CHANGELOG_FILE_ARG.of(changeLogFile),
                RegisterChangeLogCommand.HUB_PROJECT_ID_ARG.of(UUID.fromString(hubProjectId))
        );

        registerChangeLog.addArgument("changeLogFile", changeLogFile);
        registerChangeLog.addArgument("database", database);
        registerChangeLog.addArgument("liquibase", liquibase);
        registerChangeLog.addArgument("changeLog", liquibase.getDatabaseChangeLog());

        Scope.getCurrentScope().getSingleton(CommandFactory.class).execute(registerChangeLog);
    }
}
