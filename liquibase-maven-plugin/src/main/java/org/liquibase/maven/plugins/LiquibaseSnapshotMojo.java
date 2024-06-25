package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.CommandScope;
import liquibase.command.core.SnapshotCommand;
import liquibase.command.core.SnapshotCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;

/**
 * <p>The snapshot command captures the current state of the url database, which is the target database.<p/>
 *
 * @author Timur Malikin
 * @goal snapshot
 */
public class LiquibaseSnapshotMojo extends AbstractLiquibaseChangeLogMojo {

    /**
     * Creates a JSON or YAML file that represents the current state of the database.
     *
     * @parameter property="liquibase.snapshotFormat" default-value="json"
     */
    protected String snapshotFormat;

    /**
     * Specifies the file path to where the snapshot JSON or YAML will be written.
     *
     * @parameter property="liquibase.outputFile"
     */
    protected File outputFile;

    /**
     * The writer for writing the snapshot file.
     */
    private Writer outputWriter;

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        Database db = liquibase.getDatabase();
        CommandScope commandScope = new CommandScope(SnapshotCommandStep.COMMAND_NAME);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, db);
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnection().getURL());
        commandScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, snapshotFormat);
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        commandScope.setOutput(outputStream);

        try {
            commandScope.execute();
            outputStream.flush();
        } catch (IOException cee) {
            throw new LiquibaseException(cee);
        }
    }

    @Override
    protected Liquibase createLiquibase(Database db)
            throws MojoExecutionException {
        Liquibase liquibase = super.createLiquibase(db);

        if (outputFile != null) {
            // Setup the output file writer
            try {
                if (!outputFile.exists()) {
                    // Ensure the parent directories exist
                    outputFile.getParentFile().mkdirs();
                    // Create the actual file
                    if (!outputFile.createNewFile()) {
                        throw new MojoExecutionException(
                                "Cannot create the output snapshot file; "
                                        + outputFile.getAbsolutePath());
                    }
                }

                outputWriter = getOutputWriter(outputFile);
            } catch (IOException e) {
                getLog().error(e);
                throw new MojoExecutionException(
                        "Failed to create the output writer", e);
            }
            getLog().info(
                    "Output snapshot file: "
                            + outputFile.getAbsolutePath());
        } else {
            getLog().info("Output snapshot goes to STDOUT");
            outputWriter = new OutputStreamWriter(System.out);
        }

        return liquibase;
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "snapshotFormat: " + snapshotFormat);
        getLog().info(indent + "outputFile: " + outputFile);
    }
}
