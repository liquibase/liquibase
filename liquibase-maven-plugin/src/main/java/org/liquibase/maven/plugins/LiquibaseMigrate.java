package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ResourceAccessor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Liquibase Migration Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process.
 * @author Peter Murray
 * @description Liquibase Migrate Maven plugin
 * @goal migrate
 * @deprecated Use the LiquibaseUpdate class or Maven goal "update" instead.
 */
public class LiquibaseMigrate extends AbstractLiquibaseUpdateMojo {

  @Override
  public void configureFieldsAndValues(ResourceAccessor fo)
          throws MojoExecutionException, MojoFailureException {
    getLog().warn("This plugin goal is DEPRECATED and will be removed in a future "
                  + "release, please use \"update\" instead of \"migrate\".");
    super.configureFieldsAndValues(fo);
  }

  @Override
  protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
    if (changesToApply > 0) {
      liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(labels));
    } else {
      liquibase.update(toTag, new Contexts(contexts), new LabelExpression(labels));
    }
  }
}
