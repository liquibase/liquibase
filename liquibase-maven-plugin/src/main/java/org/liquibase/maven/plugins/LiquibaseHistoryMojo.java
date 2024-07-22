package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.HistoryCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.exception.LiquibaseException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope historyCommand = new CommandScope(HistoryCommandStep.COMMAND_NAME);

      historyCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getLiquibase().getDatabase());

      historyCommand.execute();
    }
}
