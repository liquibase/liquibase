package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.CommandResult;
import liquibase.command.LiquibaseCommand;
import liquibase.command.core.HistoryCommand;
import liquibase.exception.LiquibaseException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      HistoryCommand historyCommand = (HistoryCommand) CommandFactory.getInstance().getCommand("history");

      historyCommand.setDatabase(getLiquibase().getDatabase());
      try {
          CommandResult result = historyCommand.execute();
          if (!result.succeeded) {
              throw new LiquibaseException(result.message);
          }
      } catch (CommandExecutionException e) {
        throw new LiquibaseException(e);
      }
    }
}
