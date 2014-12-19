package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

/**
 * Generates the SQL that is required to rollback the database to current state after the next update.
 *
 * @description Liquibase FutureRollbackSQL Maven plugin
 * @goal futureRollbackSQL
 */
public class LiquibaseFutureRollbackSQL extends LiquibaseRollback {

    /**
     * The file to output the Rollback SQL script to, if it exists it will be
     * overwritten.
     *
     * @parameter expression="${liquibase.outputFile}"
     *            default-value=
     *            "${project.build.directory}/liquibase/migrate.sql"
     */
    protected File outputFile;

    /** The writer for writing the SQL. */
    private Writer outputWriter;

    @Override
    protected boolean isPromptOnNonLocalDatabase() {
        // Always run on an non-local database as we are not actually modifying
        // the database when run on it.
        return false;
    }

    @Override
    protected Liquibase createLiquibase(ResourceAccessor fo, Database db)
            throws MojoExecutionException {
        Liquibase liquibase = super.createLiquibase(fo, db);

        // Setup the output file writer
        try {
            if (!outputFile.exists()) {
                // Ensure the parent directories exist
                outputFile.getParentFile().mkdirs();
                // Create the actual file
                if (!outputFile.createNewFile()) {
                    throw new MojoExecutionException(
                            "Cannot create the output file; "
                                    + outputFile.getAbsolutePath());
                }
            }
            outputWriter = getOutputWriter(outputFile);
        } catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException(
                    "Failed to create SQL output writer", e);
        }
        getLog().info(
                "Output File: "
                        + outputFile.getAbsolutePath());
        return liquibase;
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(
                indent + "outputFile: " + outputFile);
    }

    @Override
    protected void cleanup(Database db) {
        super.cleanup(db);
        if (outputWriter != null) {
            try {
                outputWriter.close();
            } catch (IOException e) {
                getLog().error(e);
            }
        }
    }

    @Override
    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        liquibase.futureRollbackSQL(null, new Contexts(contexts), new LabelExpression(labels), outputWriter);
    }

    @Override
    protected void checkRequiredRollbackParameters() throws MojoFailureException {
        //nothing to check with futureRollbackSQL
    }
}