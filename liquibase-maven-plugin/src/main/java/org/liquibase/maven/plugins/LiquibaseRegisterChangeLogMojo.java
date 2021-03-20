package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResult;
import liquibase.command.core.RegisterChangeLogCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * <p>Registers a change log with Hub.</p>
 * 
 * @author Wesley Willard
 * @goal   registerChangeLog
 *
 */
public class LiquibaseRegisterChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     *
     * Specifies the <i>Liquibase Hub Project ID</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     *
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
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        RegisterChangeLogCommand registerChangeLog =
            (RegisterChangeLogCommand) CommandFactory.getInstance().getCommand("registerChangeLog");
        registerChangeLog.setChangeLogFile(changeLogFile);
        if (hubProjectId != null) {
            registerChangeLog.setHubProjectId(UUID.fromString(hubProjectId));
        }
        registerChangeLog.setProjectName(hubProjectName);
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("changeLogFile", changeLogFile);
        argsMap.put("database", database);
        argsMap.put("liquibase", liquibase);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        registerChangeLog.configure(argsMap);
        try {
            CommandResult result = registerChangeLog.execute();
            if (result.succeeded) {
                Scope.getCurrentScope().getUI().sendMessage(result.print());
            } else {
                throw new LiquibaseException(result.message);
            }

        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing registerChangeLog", cee);
        }
    }
}
