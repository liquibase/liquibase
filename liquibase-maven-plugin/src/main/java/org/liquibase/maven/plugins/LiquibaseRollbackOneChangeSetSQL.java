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
 * Displays the SQL which will be executed when the corresponding rollbackOneChangeSet command is
 * executed.  This command does not perform the actual rollback.  A Liquibase Pro license key is required.
 *
 * @goal rollbackOneChangeSetSQL
 */
public class LiquibaseRollbackOneChangeSetSQL extends AbstractLiquibaseChangeLogMojo {

    /**
     * The changeset ID to rollback
     *
     * @parameter property="liquibase.changeSetId"
     */
    @PropertyElement
    protected String changeSetId;

    /**
     * Specifies the author of the <i>changeSet</i> you want to rollback.
     *
     * @parameter property="liquibase.changeSetAuthor"
     */
    @PropertyElement
    protected String changeSetAuthor;

    /**
     * Specifies the path to the <i>changelog</i> which contains the <i>change-set</i> you want to rollback.
     *
     * @parameter property="liquibase.changeSetPath"
     */
    @PropertyElement
    protected String changeSetPath;

    /**
     * Specifies the path to a rollback script
     *
     * @parameter property="liquibase.rollbackScript"
     */
    @PropertyElement
    protected String rollbackScript;

    /**
     * Specifies the path to the generated SQL output file.
     *
     * @parameter property="liquibase.outputFile"
     */
    @PropertyElement
    protected String outputFile;

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "Changeset ID:     " + changeSetId);
        getLog().info(indent + "Changeset Author: " + changeSetAuthor);
        getLog().info(indent + "Changeset Path:   " + changeSetPath);
        getLog().info(indent + "Rollback script:   " + rollbackScript);
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        //
        // We override in order to set the command name for later use
        //
        commandName = "rollbackOneChangeSetSQL";
        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license
        //
        Database database = liquibase.getDatabase();
        CommandScope liquibaseCommand = new CommandScope("rollbackOneChangeSetSQL");

        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        argsMap.put("liquibase", liquibase);

        for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
            liquibaseCommand.addArgumentValue(entry.getKey(), entry.getValue());
        }
        liquibaseCommand.setOutput(getOutputStream(outputFile));
        liquibaseCommand.execute();
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("force", true);
        argsMap.put("changeLogFile", this.changeLogFile);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        argsMap.put("changesetId", this.changeSetId);
        argsMap.put("changesetAuthor", this.changeSetAuthor);
        argsMap.put("changesetPath", this.changeSetPath);
        argsMap.put("rollbackScript", this.rollbackScript);
        argsMap.put("outputFile", this.outputFile);
        return argsMap;
    }
}
