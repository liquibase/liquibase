// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.io.*;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Creates an SQL migration script using the provided DatabaseChangeLog(s) comparing what
 * already exists in the database to what is defined in the DataBaseChangeLog(s).
 * @author Peter Murray
 * @description Liquibase Migrate SQL Maven plugin
 * @goal migrateSQL
 */
public class LiquibaseMigrateSQL extends AbstractLiquibaseMojo {

  /**
   * The file to output the Migration SQL script to, if it exists it will be overwritten.
   * @parameter expression="${liquibase.migrationSqlOutputFile}"
   * default-value="${project.build}/liquibase/migrate.sql"
   */
  protected File migrationSqlOutputFile;

  /**
   * Output a change log SQL for the DatabaseChangeLog table only, not the actual database
   * changes that are required.
   * @parameter expression="${liquibase.outputChangeLogSQLOnly}" default-value="false"
   */
  protected boolean changeLogSqlOnly;

  @Override
  protected void printSettings() {
    super.printSettings();
    getLog().info("   migrationSQLOutputFile: " + migrationSqlOutputFile);
    getLog().info("   changeLogSqlOnly: " + changeLogSqlOnly);
  }

  @Override
  protected void performMigratorConfiguration(Migrator migrator) throws MojoExecutionException {
    Writer w = null;
    try {
      if (!migrationSqlOutputFile.exists()) {
        // Ensure the parent directories exist
        migrationSqlOutputFile.getParentFile().mkdirs();
      }
      w = new FileWriter(migrationSqlOutputFile);
    }
    catch (IOException e) {
      getLog().error(e);
      throw new MojoExecutionException("Failed to create SQL output writer", e);
    }
    
    getLog().info("Output SQL Migration File: " + migrationSqlOutputFile.getAbsolutePath());
    if (changeLogSqlOnly) {
      getLog().info("Change Log only SQL");
      migrator.setMode(Migrator.Mode.OUTPUT_CHANGELOG_ONLY_SQL_MODE);
    } else {
      migrator.setMode(Migrator.Mode.OUTPUT_SQL_MODE);
    }
    migrator.setOutputSQLWriter(w);
  }

  protected void performLiquibaseTask(Migrator migrator) throws MojoExecutionException {
    try {
      migrator.migrate();
    }
    catch (LiquibaseException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
