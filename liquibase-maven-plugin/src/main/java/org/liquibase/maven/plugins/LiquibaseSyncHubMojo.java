package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.SyncHubCommandStep;
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
        syncHub
                .addArgumentValue(SyncHubCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(SyncHubCommandStep.URL_ARG, database.getConnection().getURL())
                .addArgumentValue(SyncHubCommandStep.HUB_CONNECTION_ID_ARG, hubConnectionId)
                .addArgumentValue(SyncHubCommandStep.HUB_PROJECT_ID_ARG, hubProjectId)
                .addArgumentValue(SyncHubCommandStep.DATABASE_ARG, database)
                .addArgumentValue(SyncHubCommandStep.FAIL_IF_ONLINE_ARG, false);

        syncHub.execute();
    }
}
