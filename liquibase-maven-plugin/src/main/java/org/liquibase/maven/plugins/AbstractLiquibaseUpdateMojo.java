package org.liquibase.maven.plugins;

import liquibase.*;
import liquibase.database.Database;
import liquibase.command.CommandScope;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.liquibase.maven.property.PropertyElement;

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
   * @parameter property="liquibase.changesToApply" default-value=0
   */
  @PropertyElement
  protected int changesToApply;

  /**
   * Update to the changeSet with the given tag command.
   * @parameter property="liquibase.toTag"
   */
  protected String toTag;

  /**
   * Whether or not to print a summary of the update operation.
   * Allowed values: 'OFF', 'SUMMARY' (default), 'VERBOSE'
   *
   * @parameter property="liquibase.showSummary"
   */
  @PropertyElement
  protected UpdateSummaryEnum showSummary;

  /**
   * Flag to control where we show the summary.
   * Allowed values: 'LOG', 'CONSOLE', OR 'ALL' (default)
   *
   * @parameter property="liquibase.showSummaryOutput"
   */
  @PropertyElement
  protected UpdateSummaryOutputEnum showSummaryOutput;


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
  protected Liquibase createLiquibase(Database db) throws MojoExecutionException {
    Liquibase liquibase = super.createLiquibase(db);
    liquibase.setShowSummary(showSummary);
    liquibase.setShowSummaryOutput(showSummaryOutput);
    return liquibase;
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "number of changes to apply: " + changesToApply);
  }

}
