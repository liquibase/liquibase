package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandScope;
import liquibase.command.core.SnapshotCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**
 * <p>The snapshot command captures the current state of the url database, which is the target database.<p/>
 *
 * @author Timur Malikin
 * @goal snapshot
 */
public class LiquibaseSnapshotMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Creates a TXT, JSON or YAML file that represents the current state of the database.
     *
     * @parameter property="liquibase.snapshotFormat" default-value="txt"
     */
    protected String snapshotFormat;

    /**
     * Specifies the file path to where the snapshot JSON or YAML will be written.
     *
     * @parameter property="liquibase.outputFile"
     */
    protected File outputFile;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        Database db = liquibase.getDatabase();
        CommandScope commandScope = new CommandScope(SnapshotCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, db);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnection().getURL());
        commandScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, snapshotFormat);
        PrintStream output = createPrintStream();
        commandScope.setOutput(output);

        commandScope.execute();
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "snapshotFormat: " + snapshotFormat);
        getLog().info(indent + "outputFile: " + outputFile);
    }

    private PrintStream createPrintStream() throws LiquibaseException {
        try {
            return (outputFile != null ? new PrintStream(outputFile) : System.out);
        } catch (FileNotFoundException e) {
            throw new LiquibaseException(e);
        }
    }
}
