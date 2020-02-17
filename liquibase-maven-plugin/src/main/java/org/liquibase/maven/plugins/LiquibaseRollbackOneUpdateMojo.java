package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.LiquibaseCommand;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * Reverts (rolls back) all non-sequential change sets related by a specific deployment ID
 * that were made during a previous change to your database in a non-sequential manner.
 * It is only available for Liquibase Pro users.
 *
 * @goal rollbackOneUpdate
 *
 */
public class LiquibaseRollbackOneUpdateMojo extends AbstractLiquibaseChangeLogMojo {
    /**
     *
     * Specifies the Deployment ID you want to rollback
     *
     * @parameter property="liquibase.deploymentId"
     *
     */
    protected String deploymentId;

    /**
     *
     * A required flag for rollbackOneUpdate.
     *
     * @parameter property="liquibase.force"
     *
     */
    protected String force;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        commandName = "rollbackOneUpdate";
        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license
        //
        boolean hasProLicense = MavenUtils.checkProLicense(liquibaseProLicenseKey, commandName, getLog());
        if (! hasProLicense) {
            throw new LiquibaseException("The command 'rollbackOneUpdate' requires a Liquibase Pro License, available at http://liquibase.org.");
        }
        Database database = liquibase.getDatabase();
        LiquibaseCommand liquibaseCommand = (CommandFactory.getInstance().getCommand("rollbackOneUpdate"));
        AbstractSelfConfiguratingCommand configuratingCommand = (AbstractSelfConfiguratingCommand)liquibaseCommand;
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        if (force == null || (force != null && ! Boolean.parseBoolean(force))) {
            throw new LiquibaseException("Invalid value for --force.  You must specify 'liquibase.force=true' to use rollbackOneUpdate.");
        }
        argsMap.put("force", Boolean.TRUE);
        argsMap.put("liquibase", liquibase);
        configuratingCommand.configure(argsMap);
        try {
            liquibaseCommand.execute();
        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing rollbackOneUpdate", cee);
        }
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<String, Object>();
        argsMap.put("deploymentId", this.deploymentId);
        argsMap.put("force", this.force);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        return argsMap;
    }

}
