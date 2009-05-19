package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;

/**
 * Liquibase Update Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process.
 * @author Peter Murray
 * @description Liquibase Update Maven plugin
 * @goal update
 */
public class LiquibaseUpdate extends AbstractLiquibaseUpdateMojo {

  @Override
  protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
    if (changesToApply > 0) {
      liquibase.update(changesToApply, contexts);
    } else {
      liquibase.update(contexts);
    }
  }
}