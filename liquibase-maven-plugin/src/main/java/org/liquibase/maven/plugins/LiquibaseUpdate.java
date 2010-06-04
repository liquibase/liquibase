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

    /**
     * Whether or not to perform a drop on the database before executing the change.
     * @parameter expression="${liquibase.dropFirst}" default-value="false"
     */
    protected boolean dropFirst;

    private boolean dropFirstDefault = false;

  @Override
  protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
      if (dropFirst) {
        liquibase.dropAll();
      }

    if (changesToApply > 0) {
      liquibase.update(changesToApply, contexts);
    } else {
      liquibase.update(contexts);
    }
  }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "drop first? " + dropFirst);

    }
}