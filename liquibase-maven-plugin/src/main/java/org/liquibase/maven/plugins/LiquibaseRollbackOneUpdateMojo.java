// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
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
 * Invokes Liquibase rollback by Deployment ID
 *
 * @goal rollbackOneUpdate
 *
 */
public class LiquibaseRollbackOneUpdateMojo extends AbstractLiquibaseChangeLogMojo {
    /**
     *
     * The Deployment ID to rollback
     *
     * @parameter property="liquibase.deploymentId"
     *
     */
    protected String deploymentId;

    /**
     *
     * Required flag for RollbackOneChangeSet
     *
     * @parameter property="liquibase.force"
     *
     */
    protected String force;

    /**
     *
     * The path to a rollback script
     *
     * @parameter property="liquibase.rollbackScript"
     *
     */
    protected String rollbackScript;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        commandName = "rollbackOneUpdate";
        super.execute();
    }

    @Override
    protected void printSettings(String indent) {
      super.printSettings(indent);
        getLog().info(indent + "Rollback script:   " + rollbackScript);
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license
        //
        if (! hasProLicense()) {
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
        argsMap.put("rollbackScript", this.rollbackScript);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        return argsMap;
    }

}
