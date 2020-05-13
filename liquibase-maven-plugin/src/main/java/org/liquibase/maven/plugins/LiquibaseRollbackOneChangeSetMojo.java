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
 * Reverts (rolls back) one non-sequential <i>changeSet</i> made during a previous change to your database. It is only available for Liquibase Pro users.
 *
 * @goal rollbackOneChangeSet
 *
 */
public class LiquibaseRollbackOneChangeSetMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     *
     * The change set ID to rollback
     *
     * @parameter property="liquibase.changeSetId"
     *
     */
    protected String changeSetId;

    /**
     *
     * Specifies the author of the <i>changeSet</i> you want to rollback.
     *
     * @parameter property="liquibase.changeSetAuthor"
     *
     */
    protected String changeSetAuthor;

    /**
     *
     * Specifies the path to the <i>changelog</i> which contains the <i>change-set</i> you want to rollback.
     *
     * @parameter property="liquibase.changeSetPath"
     *
     */
    protected String changeSetPath;

    /**
     *
     * A required flag which indicates you intend to run rollbackOneChangeSet
     *
     * @parameter property="liquibase.force"
     *
     */
    protected String force;

    /**
     *
     * Specifies the path to a rollback script
     *
     * @parameter property="liquibase.rollbackScript"
     *
     */
    protected String rollbackScript;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        commandName = "rollbackOneChangeSet";
        super.execute();
    }

    @Override
    protected void printSettings(String indent) {
      super.printSettings(indent);
        getLog().info(indent + "Change Set ID:     " + changeSetId);
        getLog().info(indent + "Change Set Author: " + changeSetAuthor);
        getLog().info(indent + "Change Set Path:   " + changeSetPath);
        getLog().info(indent + "Rollback script:   " + rollbackScript);
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license
        //
        boolean hasProLicense = MavenUtils.checkProLicense(liquibaseProLicenseKey, commandName, getLog());
        if (! hasProLicense) {
            throw new LiquibaseException("The command 'rollbackOneChangeSet' requires a Liquibase Pro License, available at http://liquibase.org.");
        }
        Database database = liquibase.getDatabase();
        LiquibaseCommand liquibaseCommand = (CommandFactory.getInstance().getCommand("rollbackOneChangeSet"));
        AbstractSelfConfiguratingCommand configuratingCommand = (AbstractSelfConfiguratingCommand)liquibaseCommand;
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        if (force == null || (force != null && ! Boolean.parseBoolean(force))) {
            throw new LiquibaseException("Invalid value for --force.  You must specify 'liquibase.force=true' to use rollbackOneChangeSet.");
        }
        argsMap.put("force", Boolean.TRUE);
        argsMap.put("liquibase", liquibase);
        configuratingCommand.configure(argsMap);
        try {
            liquibaseCommand.execute();
        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing rollbackOneChangeSet", cee);
        }
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<String, Object>();
        argsMap.put("changeSetId", this.changeSetId);
        argsMap.put("changeSetAuthor", this.changeSetAuthor);
        argsMap.put("changeSetPath", this.changeSetPath);
        argsMap.put("force", this.force);
        argsMap.put("rollbackScript", this.rollbackScript);
        argsMap.put("changeLogFile", this.changeLogFile);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        return argsMap;
    }
}
