package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.command.core.SyncHubCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * <p>Syncs all changes in change log with Hub.</p>
 *
 * @author Wesley Willard
 * @goal syncHub
 */
public class LiquibaseSyncHubMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Specifies the <i>Liquibase Hub Connection ID</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubConnectionId"
     */
    protected String hubConnectionId;

    /**
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     */
    protected String hubProjectId;

    @Override
    protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
        //
        // Override because changeLogFile is not required
        //
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase)
            throws LiquibaseException {
        super.performLiquibaseTask(liquibase);
        Database database = liquibase.getDatabase();
        CommandScope syncHub = new CommandScope("syncHub");
        syncHub.addArguments(
                SyncHubCommand.CHANGELOG_FILE_ARG.of(changeLogFile),
                SyncHubCommand.URL_ARG.of(database.getConnection().getURL()),
                SyncHubCommand.HUB_CONNECTION_ID_ARG.of(hubConnectionId),
                SyncHubCommand.HUB_PROJECT_ID_ARG.of(hubProjectId),
                SyncHubCommand.DATABASE_ARG.of(database),
                SyncHubCommand.FAIL_IF_ONLINE_ARG.of(false)
        );

        Scope.getCurrentScope().getSingleton(CommandFactory.class).execute(syncHub);
    }
}
