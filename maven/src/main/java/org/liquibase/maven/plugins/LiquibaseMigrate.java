package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;

/**
 * Liquibase Migration Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process.
 * @author Peter Murray
 * @description Liquibase Migrate Maven plugin
 * @goal migrate
 * @deprecated Use the LiquibaseUpdate class or Maven goal "update" instead.
 */
public class LiquibaseMigrate extends AbstractLiquibaseChangeLogMojo {

  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    super.performLiquibaseTask(liquibase);
      liquibase.update(contexts);
  }
}
