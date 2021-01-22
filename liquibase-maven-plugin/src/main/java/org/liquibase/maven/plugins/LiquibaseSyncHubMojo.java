package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.*;
import liquibase.command.core.SyncHubCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * <p>Syncs all changes in change log with Hub.</p>
 * 
 * @author Wesley Willard
 * @goal   syncHub
 *
 */
public class LiquibaseSyncHubMojo extends AbstractLiquibaseChangeLogMojo {

  	/**
  	 * Specifies the <i>Liquibase Hub Connection ID</i> for Liquibase to use.
  	 *
  	 * @parameter property="liquibase.hubConnectionId"
  	 *
  	 */
  	protected String hubConnectionId;

    /**
     *
     * Specifies the <i>Liquibase Hub API key</i> for Liquibase to use.
     *
     * @parameter property="liquibase.hubProjectId"
     *
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
        SyncHubCommand syncHub = (SyncHubCommand) Scope.getCurrentScope().getSingleton(CommandFactory.class).getCommand("syncHub");
        syncHub.setChangeLogFile(changeLogFile);
        syncHub.setUrl(database.getConnection().getURL());
        syncHub.setHubConnectionId(hubConnectionId);
        syncHub.setHubProjectId(hubProjectId);
        syncHub.setDatabase(database);
        syncHub.setFailIfOnline(false);
        try {
            CommandResult result = syncHub.execute();
            if (!result.succeeded) {
                throw new LiquibaseException(result.message);
            }
        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing syncHub", cee);
        }
    }
}
