// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.io.*;
import java.sql.Connection;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Creates an SQL migration script using the provided DatabaseChangeLog(s) comparing what
 * already exists in the database to what is defined in the DataBaseChangeLog(s).
 *
 * @author Peter Murray
 * @description Liquibase Migrate SQL Maven plugin
 * @goal migrateSQL
 */
public class LiquibaseMigrateSQL extends ConfigurableLiquibaseMojo {

    /**
     * The file to output the Migration SQL script to, if it exists it will be overwritten.
     *
     * @parameter expression="${liquibase.migrationSqlOutputFile}"
     * default-value="${project.build.directory}/liquibase/migrate.sql"
     */
    protected File migrationSqlOutputFile;

    /**
     * Controls the prompting of users as to whether or not they really want to run the
     * changes on a database that is not local to the machine that the user is current
     * executing the plugin on.
     *
     * @parameter expression="${liquibase.promptOnNonLocalDatabase}" default-value="false"
     */
    protected boolean promptOnNonLocalDatabase;

    private boolean promptOnNonLocalDatabaseDefault = false;

    /** The writer fro writing the migration SQL. */  
    private Writer outputWriter;

    @Override
    protected boolean isPromptOnNonLocalDatabase() {
        return promptOnNonLocalDatabase;
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "migrationSQLOutputFile: " + migrationSqlOutputFile);
    }

    @Override
    protected void performMigratorConfiguration(Liquibase liquibase) throws MojoExecutionException {
        try {
            if (!migrationSqlOutputFile.exists()) {
                // Ensure the parent directories exist
                migrationSqlOutputFile.getParentFile().mkdirs();
            }
            outputWriter = new FileWriter(migrationSqlOutputFile);
        }
        catch (IOException e) {
            getLog().error(e);
            throw new MojoExecutionException("Failed to create SQL output writer", e);
        }
        getLog().info("Output SQL Migration File: " + migrationSqlOutputFile.getAbsolutePath());
    }

    protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
        getLog().info("Creating Change Log SQL and Migrating Database");
        super.performLiquibaseTask(liquibase);
        liquibase.update(contexts, outputWriter);
    }

  @Override
  protected void releaseConnection(Connection c) {
    super.releaseConnection(c);
    try {
      outputWriter.close();
    }
    catch (IOException e) {
      getLog().error(e);
    }
  }
}
