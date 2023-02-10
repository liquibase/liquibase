package org.liquibase.maven.plugins;

import liquibase.GlobalConfiguration;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Displays the SQL which will be executed when the corresponding rollbackOneUpdate
 * command is executed.  This command does not perform the actual rollback.
 * A Liquibase Pro license key is required.
 *
 * @goal rollbackOneUpdateSQL
 */
public class LiquibaseRollbackOneUpdateSQL extends AbstractLiquibaseChangeLogMojo {
    /**
     * Specifies the Deployment ID in the DATABASECHANGELOG table for all changesets you
     * want to rollback.
     *
     * @parameter property="liquibase.deploymentId"
     */
    @PropertyElement
    protected String deploymentId;

    /**
     * Required flag for RollbackOneChangeSet
     *
     * @parameter property="liquibase.force"
     */
    @PropertyElement
    protected String force;

    /**
     * Specifies the path to the generated SQL output file.
     *
     * @parameter property="liquibase.outputFile"
     */
    @PropertyElement
    protected String outputFile;

    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        commandName = "rollbackOneUpdateSQL";
        super.execute();
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        //
        // Check the Pro license
        //
        Database database = liquibase.getDatabase();
        CommandScope liquibaseCommand = new CommandScope("internalRollbackOneUpdateSQL");
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        Writer outputWriter = null;
        try {
            outputWriter = createOutputWriter();
            argsMap.put("outputWriter", outputWriter);
        } catch (IOException ioe) {
            throw new LiquibaseException("Error executing rollbackOneChangeSetSQL.  Unable to create output writer.", ioe);
        }
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        if (force != null && !Boolean.parseBoolean(force)) {
            throw new LiquibaseException("Invalid value for --force.  You must specify 'liquibase.force=true' to use rollbackOneUpdateSQL.");
        }
        argsMap.put("force", Boolean.TRUE);
        argsMap.put("liquibase", liquibase);

        for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
            liquibaseCommand.addArgumentValue(entry.getKey(), entry.getValue());
        }

        liquibaseCommand.execute();
    }

    private void closeOutputWriter(Writer outputWriter) throws IOException {
        if (outputFile == null) {
            return;
        }
        outputWriter.close();
    }

    private Writer createOutputWriter() throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

        return new OutputStreamWriter(getOutputStream(), charsetName);
    }

    private OutputStream getOutputStream() throws IOException {
        if (outputFile == null) {
            return System.out;
        }
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(outputFile, false);
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(getClass()).severe(String.format(
                    coreBundle.getString("could.not.create.output.file"),
                    outputFile));
            throw e;
        }
        return fileOut;
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<>();
        argsMap.put("deploymentId", this.deploymentId);
        argsMap.put("force", this.force);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        argsMap.put("changeLogFile", changeLogFile);
        return argsMap;
    }

}
