package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalSyncHubCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.util.UUID;

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
    @PropertyElement
    protected String hubConnectionId;

    /**
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     */
    @PropertyElement
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
        CommandScope syncHub = new CommandScope(InternalSyncHubCommandStep.COMMAND_NAME);
        syncHub
                .addArgumentValue(InternalSyncHubCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(InternalSyncHubCommandStep.URL_ARG, database.getConnection().getURL())
                .addArgumentValue(InternalSyncHubCommandStep.HUB_CONNECTION_ID_ARG, (hubConnectionId != null ? UUID.fromString(hubConnectionId) : null))
                .addArgumentValue(InternalSyncHubCommandStep.HUB_PROJECT_ID_ARG, (hubProjectId != null ? UUID.fromString(hubProjectId) : null))
                .addArgumentValue(InternalSyncHubCommandStep.DATABASE_ARG, database)
                .addArgumentValue(InternalSyncHubCommandStep.FAIL_IF_OFFLINE_ARG, false);

        syncHub.execute();
    }
}
