package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;
import liquibase.FileOpener;
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
  public void configureFieldsAndValues(FileOpener fo)
          throws MojoExecutionException, MojoFailureException {
    getLog().warn("This plugin goal is DEPRICATED and will be removed in a future "
                  + "release, please use \"update\" instead of \"migrate\".");
    super.configureFieldsAndValues(fo);
  }

  protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
    if (changesToApply > 0) {
      liquibase.update(changesToApply, contexts);
    } else {
      liquibase.update(contexts);
    }
  }
}
