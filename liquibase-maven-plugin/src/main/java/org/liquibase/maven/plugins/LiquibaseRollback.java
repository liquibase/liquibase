// Version:   $Id: $
// Copyright: Copyright(c) 2007 Trace Financial Limited
package org.liquibase.maven.plugins;

import java.text.*;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Invokes Liquibase rollbacks on a database.
 * @author Peter Murray
 * @goal rollback
 */
public class LiquibaseRollback extends AbstractLiquibaseChangeLogMojo {

  protected enum RollbackType {

    TAG, DATE, COUNT
  }

  /**
   * The tag to roll the database back to. 
   * @parameter expression="${liquibase.rollbackTag}"
   */
  protected String rollbackTag;

  /**
   * The number of change sets to rollback.
   * @parameter expression="${liquibase.rollbackCount}" default-value="-1"
   */
  protected int rollbackCount;

  /**
   * The date to rollback the database to. The format of the date must match that of the
   * <code>DateFormat.getDateInstance()</code> for the platform the plugin is executing
   * on.
   * @parameter expression="${liquibase.rollbackDate}"
   */
  protected String rollbackDate;

  /** The type of the rollback that is being performed. */
  protected RollbackType type;

  @Override
  protected void checkRequiredParametersAreSpecified() throws MojoFailureException {
    super.checkRequiredParametersAreSpecified();

    checkRequiredRollbackParameters();
  }

  protected void checkRequiredRollbackParameters() throws MojoFailureException {
    if (rollbackCount == -1 && rollbackDate == null && rollbackTag == null) {
      throw new MojoFailureException("One of the rollback options must be specified, "
                                     + "please specify one of rollbackTag, rollbackCount "
                                     + "or rollbackDate");
    }

    if (rollbackCount!=-1 && rollbackCount <= 0) {
      throw new MojoFailureException("A rollback count of " + rollbackCount + " is meaningless, please "
                                     + "select a value greater than 0");
    }

    String message = "Cannot specify multiple rollbackXXX options, please select only"
                     + " one of rollbackTag, rollbackCount, rollbackDate.";

    if (rollbackCount > 0) {
      if (rollbackDate != null || rollbackTag != null) {
        throw new MojoFailureException(message);
      }
      type = RollbackType.COUNT;
    } else if (rollbackDate != null) {
      if (rollbackTag != null || rollbackCount > 0) {
        throw new MojoFailureException(message);
      }
      type = RollbackType.DATE;
    } else if (rollbackTag != null) {
      if (rollbackCount > 0 || rollbackDate != null) {
        throw new MojoFailureException(message);
      }
      type = RollbackType.TAG;
    }
  }

  @Override
  protected void printSettings(String indent) {
    super.printSettings(indent);
    getLog().info(indent + "rollback Count: " + rollbackCount);
    getLog().info(indent + "rollback Date: " + rollbackDate);
    getLog().info(indent + "rollback Tag: " + rollbackTag);
  }

  @Override
  protected void performLiquibaseTask(Liquibase liquibase) throws LiquibaseException {
    switch (type) {
      case COUNT: {
        liquibase.rollback(rollbackCount, new Contexts(contexts), new LabelExpression(labels));
        break;
      }
      case DATE: {
        DateFormat format = DateFormat.getDateInstance();
        try {
          liquibase.rollback(format.parse(rollbackDate), new Contexts(contexts), new LabelExpression(labels));
        }
        catch (ParseException e) {
          String message = "Error parsing rollbackDate: " + e.getMessage();
          if (format instanceof SimpleDateFormat) {
            message += "\nDate must match pattern: " + ((SimpleDateFormat)format).toPattern();
          }
          throw new LiquibaseException(message, e);
        }
        break;
      }
      case TAG: {
        liquibase.rollback(rollbackTag, new Contexts(contexts), new LabelExpression(labels));
        break;
      }
      default: {
        throw new IllegalStateException("Unexpected rollback type, " + type);
      }
    }
  }
}
