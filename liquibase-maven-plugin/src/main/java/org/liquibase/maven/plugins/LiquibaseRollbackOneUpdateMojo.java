package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Rolls back all changesets from any specific update, if all changesets can be rolled back.
 * By default, the last update is rolled back, but an optional deployentId parameter can target any update.
 * (Liquibase Pro only).
 *
 * @goal rollbackOneUpdate
 *
 */
public class LiquibaseRollbackOneUpdateMojo extends AbstractLiquibaseChangeLogMojo {
    /**
     *
     * Specifies the update your want to rollback.  A list of the updates's
     * changesets grouped by their deploymentId can be found by using the <i>history</i> command.
     *
     * @parameter property="liquibase.deploymentId"
     *
     */
    @PropertyElement
    protected String deploymentId;

    /**
     *
     * A required flag for rollbackOneUpdate.
     *
     * @parameter property="liquibase.force"
     *
     */
    @PropertyElement
    protected String force;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        commandName = "rollbackOneUpdate";
        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Call the base class method so that
        // Hub settings will be made
        //
        super.performLiquibaseTask(liquibase);

        //
        // Check the Pro license
        //
        Database database = liquibase.getDatabase();
        CommandScope liquibaseCommand = new CommandScope("internalRollbackOneUpdate");
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        if (force == null || (force != null && ! Boolean.parseBoolean(force))) {
            throw new LiquibaseException("Invalid value for --force.  You must specify 'liquibase.force=true' to use rollbackOneUpdate.");
        }
        argsMap.put("force", Boolean.TRUE);
        argsMap.put("liquibase", liquibase);
        for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
            liquibaseCommand.addArgumentValue(entry.getKey(), entry.getValue());
        }

        liquibaseCommand.execute();
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<String, Object>();
        argsMap.put("deploymentId", this.deploymentId);
        argsMap.put("force", this.force);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        argsMap.put("changeLogFile", changeLogFile);
        return argsMap;
    }

}
