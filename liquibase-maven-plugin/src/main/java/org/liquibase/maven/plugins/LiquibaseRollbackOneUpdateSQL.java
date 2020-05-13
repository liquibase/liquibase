package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.LiquibaseCommand;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 *
 * Displays the SQL which will be executed when the corresponding rollbackOneUpdate
 * command is executed.  This command does not perform the actual rollback.
 * A Liquibase Pro license key is required.
 *
 * @goal rollbackOneUpdateSQL
 *
 */
public class LiquibaseRollbackOneUpdateSQL extends AbstractLiquibaseChangeLogMojo {
    /**
     *
     * Specifies the Deployment ID in the DATABASECHANGELOG table for all change sets you
     * want to rollback.
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
     * Specifies the path to the generated SQL output file.
     *
     * @parameter property="liquibase.outputFile"
     *
     */
    protected String outputFile;

    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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
        boolean hasProLicense = MavenUtils.checkProLicense(liquibaseProLicenseKey, commandName, getLog());
        if (! hasProLicense) {
            throw new LiquibaseException("The command 'rollbackOneUpdateSQL' requires a Liquibase Pro License, available at http://liquibase.org.");
        }
        Database database = liquibase.getDatabase();
        LiquibaseCommand liquibaseCommand = (CommandFactory.getInstance().getCommand("rollbackOneUpdate"));
        AbstractSelfConfiguratingCommand configuratingCommand = (AbstractSelfConfiguratingCommand)liquibaseCommand;
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        Writer outputWriter = null;
        try {
            outputWriter = createOutputWriter();
            argsMap.put("outputWriter", outputWriter);
        }
        catch (IOException ioe) {
            throw new LiquibaseException("Error executing rollbackOneChangeSetSQL.  Unable to create output writer.", ioe);
        }
        ChangeLogParameters clp = new ChangeLogParameters(database);
        argsMap.put("changeLogParameters", clp);
        if (force != null && ! Boolean.parseBoolean(force)) {
            throw new LiquibaseException("Invalid value for --force.  You must specify 'liquibase.force=true' to use rollbackOneUpdateSQL.");
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
        finally {
            try {
                outputWriter.flush();
                closeOutputWriter(outputWriter);
            }
            catch (IOException ioe) {
                Scope.getCurrentScope().getLog(getClass()).info("Unable to close output file");
            }
        }
    }

    private void closeOutputWriter(Writer outputWriter) throws IOException {
        if (outputFile == null) {
            return;
        }
        outputWriter.close();
    }

    private Writer createOutputWriter() throws IOException {
        String charsetName = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getOutputEncoding();

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
        Map<String, Object> argsMap = new HashMap<String, Object>();
        argsMap.put("deploymentId", this.deploymentId);
        argsMap.put("force", this.force);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        return argsMap;
    }

}
