// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.changelog.ChangeLogParameters;
import liquibase.command.AbstractSelfConfiguratingCommand;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.LiquibaseCommand;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import org.apache.maven.plugin.MojoFailureException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 *
 * Invokes Liquibase targeted rollback
 *
 * @goal rollbackOneChangeSetSQL
 *
 */
public class LiquibaseRollbackOneChangeSetSQL extends AbstractLiquibaseChangeLogMojo {
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
     * The change set author to rollback
     *
     * @parameter property="liquibase.changeSetAuthor"
     *
     */
    protected String changeSetAuthor;

    /**
     *
     * The path to the changelog where this
     * change set to rollback lives
     *
     * @parameter property="liquibase.changeSetPath"
     *
     */
    protected String changeSetPath;

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

    /**
     *
     * The path to an output file
     *
     * @parameter property="liquibase.outputFile"
     *
     */
    protected String outputFile;
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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
        if (! hasProLicense()) {
            return;
        }
        Database database = liquibase.getDatabase();
        LiquibaseCommand liquibaseCommand = (CommandFactory.getInstance().getCommand("rollbackOneChangeSet"));
        AbstractSelfConfiguratingCommand configuratingCommand = (AbstractSelfConfiguratingCommand)liquibaseCommand;
        Map<String, Object> argsMap = getCommandArgsObjectMap(liquibase);
        ChangeLogParameters clp = new ChangeLogParameters(database);
        Writer outputWriter = null;
        try {
            outputWriter = createOutputWriter();
            argsMap.put("outputWriter", outputWriter);
        }
        catch (IOException ioe) {
            throw new LiquibaseException("Error executing rollbackOneChangeSet.  Unable to create output writer.", ioe);
        }
        argsMap.put("changeLogParameters", clp);
        argsMap.put("liquibase", liquibase);
        configuratingCommand.configure(argsMap);
        try {
            liquibaseCommand.execute();
        }
        catch (CommandExecutionException cee) {
            throw new LiquibaseException("Error executing rollbackOneChangeSet", cee);
        }
        finally {
            try {
                outputWriter.flush();
                outputWriter.close();
            }
            catch (IOException ioe) {
                LogService.getLog(getClass()).info(LogType.LOG, String.format("Unable to close output file"));
            }
            finally {
            }
        }
    }

    private OutputStream getOutputStream() throws IOException {
        if (outputFile == null) {
            return System.out;
        }
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(outputFile, false);
        } catch (IOException e) {
            LogService.getLog(getClass()).severe(LogType.LOG, String.format(
                    coreBundle.getString("could.not.create.output.file"),
            outputFile));
            throw e;
        }
        return fileOut;
    }

    private Writer createOutputWriter() throws IOException {
        String charsetName = LiquibaseConfiguration.getInstance().getConfiguration(GlobalConfiguration.class)
                .getOutputEncoding();

        return new OutputStreamWriter(getOutputStream(), charsetName);
    }

    private Map<String, Object> getCommandArgsObjectMap(Liquibase liquibase) throws LiquibaseException {
        Database database = liquibase.getDatabase();
        Map<String, Object> argsMap = new HashMap<String, Object>();
        argsMap.put("changeSetId", this.changeSetId);
        argsMap.put("changeSetAuthor", this.changeSetAuthor);
        argsMap.put("changeSetPath", this.changeSetPath);
        argsMap.put("force", true);
        argsMap.put("rollbackScript", this.rollbackScript);
        argsMap.put("changeLogFile", this.changeLogFile);
        argsMap.put("database", database);
        argsMap.put("changeLog", liquibase.getDatabaseChangeLog());
        argsMap.put("resourceAccessor", liquibase.getResourceAccessor());
        return argsMap;
    }
}
