package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalHistoryCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

/**
 * <p>Test connection to the configured database.</p>
 *
 * @goal connect
 */
public class LiquibaseConnectMojo extends AbstractLiquibaseMojo {
    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
      CommandScope connectCommand = new CommandScope("connect");
      connectCommand.addArgumentValue("url", url);
      connectCommand.addArgumentValue("username", username);
      connectCommand.addArgumentValue("password", password);
      connectCommand.addArgumentValue("catalog", defaultCatalogName);
      connectCommand.addArgumentValue("schema", defaultSchemaName);
      connectCommand.provideDependency(Database.class, getLiquibase().getDatabase());
      connectCommand.execute();
    }
}
