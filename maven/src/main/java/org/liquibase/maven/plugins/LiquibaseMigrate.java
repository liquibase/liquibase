package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * A Maven wrapper around the Liquibase Migrator class for use in Maven projects.
 * @author Peter Murray, Trace Financial Limited
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
