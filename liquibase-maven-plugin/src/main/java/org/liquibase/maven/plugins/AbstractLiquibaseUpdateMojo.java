package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;

/**
 * Liquibase Update Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process.
 * @author Peter Murray
 * @description Liquibase Update Maven plugin
 */
public abstract class AbstractLiquibaseUpdateMojo extends AbstractLiquibaseChangeLogMojo {

  /**
   * The number of changes to apply to the database. By default this value is 0, which
   * will result in all changes (not already applied to the database) being applied.
   * @parameter expression="${liquibase.changesToApply}" default-value=0
   */
  protected int changesToApply;

  /**
   * Update to the changeSet with the given tag command.
   * @parameter expression="${liquibase.toTag}"
   */
  protected String toTag;

  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    super.performLiquibaseTask(liquibase);
    doUpdate(liquibase);
  }

  /**
   * Performs the actual "update" work on the database.
   * @param liquibase The Liquibase object to use to perform the "update".
   */
  protected abstract void doUpdate(Liquibase liquibase) throws LiquibaseException;

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "number of changes to apply: " + changesToApply);
  }
}