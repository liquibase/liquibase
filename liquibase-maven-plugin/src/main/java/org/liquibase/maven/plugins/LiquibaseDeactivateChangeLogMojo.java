package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.DeactivateChangelogCommandStep;
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

        CommandScope liquibaseCommand = new CommandScope("deactivateChangeLog");
        liquibaseCommand
                .addArgumentValue(DeactivateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
        liquibaseCommand.execute();
    }
}
