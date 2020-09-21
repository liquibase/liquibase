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
 * <p>Syncs all changes in change log with Hub.</p>
 * 
 * @author Wesley Willard
 * @goal   registerChangeLog
 *
 */
public class LiquibaseRegisterChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     *
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     *
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
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        RegisterChangeLogCommand registerChangeLog =
            (RegisterChangeLogCommand) CommandFactory.getInstance().getCommand("registerChangeLog");
        registerChangeLog.setChangeLogFile(changeLogFile);
        registerChangeLog.setHubProjectId(UUID.fromString(hubProjectId));
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
