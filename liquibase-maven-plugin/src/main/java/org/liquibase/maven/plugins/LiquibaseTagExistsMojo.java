package org.liquibase.maven.plugins;

import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoFailureException;
import org.liquibase.maven.property.PropertyElement;

/**
 * <p>Checks for existence of the specified Liquibase tag in the database</p>
 * @goal tagExists
 */
public class LiquibaseTagExistsMojo extends AbstractLiquibaseMojo {

  /**
   * The text to write to the databasechangelog.
   *
   * @parameter property="liquibase.tag"
   * @required
   */
  @PropertyElement
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
    boolean tagExists = liquibase.tagExists(tag);
    getLog().info("Tag " + tag + (tagExists ? " exists" : " does not exist") + " in the database");
  }
}
