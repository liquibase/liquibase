package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResult;
import liquibase.command.core.DeactivateChangeLogCommand;
import liquibase.command.core.RegisterChangeLogCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * <p>Deactivates a change log from Hub.</p>
 * 
 * @author Wesley Willard
 * @goal   deactivateChangeLog
 *
 */
public class LiquibaseDeactivateChangeLogMojo extends AbstractLiquibaseChangeLogMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
        throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        DeactivateChangeLogCommand deactivateChangeLogCommand = (DeactivateChangeLogCommand) Scope.getCurrentScope().getSingleton(CommandFactory.class).getCommand("deactivateChangeLog");
        deactivateChangeLogCommand.setChangeLogFile(changeLogFile);
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("changeLogFile", changeLogFile);
        argsMap.put("database", database);
        argsMap.put("liquibase", liquibase);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        deactivateChangeLogCommand.configure(argsMap);
        try {
            CommandResult result = Scope.getCurrentScope().getSingleton(CommandFactory.class).execute(deactivateChangeLogCommand);
            if (result.succeeded) {
                Scope.getCurrentScope().getUI().sendMessage(result.print());
            } else {
                throw new LiquibaseException(result.message);
            }

        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing deactivateChangeLog", cee);
        }
    }
}
