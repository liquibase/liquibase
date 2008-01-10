// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import liquibase.migrator.Migrator;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * A Maven Mojo that allows a database to be tagged using Liquibase.
 * @author Peter Murray
 * @goal tag
 */
public class LiquibaseTag extends AbstractLiquibaseMojo {

  /**
   * @parameter expression="${liquibase.tag}"
   * @required
   */
  private String tag;

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    if (tag == null || tag.trim().length() == 0) {
      throw new MojoFailureException("The tag must be specified.");
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "tag: " + tag);
  }

  protected void performLiquibaseTask(Migrator migrator) throws LiquibaseException {
    migrator.tag(tag);
  }
}
