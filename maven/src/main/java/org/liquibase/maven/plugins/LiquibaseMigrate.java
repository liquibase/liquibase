package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Liquibase Migration Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process.
 * @author Peter Murray
 * @description Liquibase Migrate Maven plugin
 * @goal migrate
 */
public class LiquibaseMigrate extends AbstractLiquibaseMojo {

  protected void performLiquibaseTask(Migrator migrator) throws MojoExecutionException {
    try {
      migrator.migrate();
    }
    catch (LiquibaseException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }
  }
}
