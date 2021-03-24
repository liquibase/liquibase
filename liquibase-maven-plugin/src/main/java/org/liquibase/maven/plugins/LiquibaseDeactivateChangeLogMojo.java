package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.DeactivateChangeLogCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

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

        CommandScope liquibaseCommand = new CommandScope("deactivateChangeLog");
        liquibaseCommand.addArgumentValues(
                DeactivateChangeLogCommand.CHANGE_LOG_FILE_ARG.of(changeLogFile),
                DeactivateChangeLogCommand.CHANGE_LOG_ARG.of(liquibase.getDatabaseChangeLog())
        );
        liquibaseCommand.execute();
    }
}
