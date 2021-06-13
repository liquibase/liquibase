package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalHistoryCommandStep;
import liquibase.exception.LiquibaseException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope historyCommand = new CommandScope(InternalHistoryCommandStep.COMMAND_NAME);

      historyCommand.addArgumentValue(InternalHistoryCommandStep.DATABASE_ARG, getLiquibase().getDatabase());

      historyCommand.execute();
    }
}
