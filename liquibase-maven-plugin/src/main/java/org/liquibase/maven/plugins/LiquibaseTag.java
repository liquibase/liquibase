package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Writes a Liquibase tag to the database.
 * 
 * @author Peter Murray
 * @goal tag
 */
public class LiquibaseTag extends AbstractLiquibaseMojo {

  /**
   * The text to write to the databasechangelog.
   *
   * @parameter expression="${liquibase.tag}"
   * @required
   */
  private String tag;

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    if ((tag == null) || tag.trim().isEmpty()) {
      throw new MojoFailureException("The tag must be specified.");
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "tag: " + tag);
  }

  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    liquibase.tag(tag);
  }
}
