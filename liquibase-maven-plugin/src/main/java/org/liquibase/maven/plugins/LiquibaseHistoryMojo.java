package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.CommandFactory;
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
      CommandScope historyCommand = new CommandScope("history");

      historyCommand.addArgumentValues(HistoryCommand.DATABASE_ARG.of(getLiquibase().getDatabase()));

      historyCommand.execute();
    }
}
