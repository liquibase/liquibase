package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalHistoryCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * <p>Outputs history of deployments against the configured database.</p>
 *
 * @goal history
 */
public class LiquibaseHistoryMojo extends AbstractLiquibaseMojo {

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        try(Database database = createDatabase()) {
            CommandScope historyCommand = new CommandScope(InternalHistoryCommandStep.COMMAND_NAME);
            historyCommand.addArgumentValue(InternalHistoryCommandStep.DATABASE_ARG, database);
            historyCommand.execute();
        } catch (MojoExecutionException e) {
            throw new LiquibaseException(e);
        }
    }
}
