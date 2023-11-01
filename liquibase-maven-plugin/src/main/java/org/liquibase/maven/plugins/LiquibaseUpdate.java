package org.liquibase.maven.plugins;

import liquibase.*;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.apache.maven.plugin.MojoExecutionException;
import org.liquibase.maven.property.PropertyElement;

/**
 * <p>Applies the DatabaseChangeLogs to the database. Useful as part of the build
 * process.</p>
 *
 * @author Peter Murray
 * @description Liquibase Update Maven plugin
 * @goal update
 */
public class LiquibaseUpdate extends AbstractLiquibaseUpdateMojo {

    /**
     * Whether or not to perform a drop on the database before executing the change.
     *
     * @parameter property="liquibase.dropFirst" default-value="false"
     */
    @PropertyElement
    protected boolean dropFirst;

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
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        if (dropFirst) {
            liquibase.dropAll();
        }
        try {
            Scope.child("rollbackOnError", rollbackOnError, () -> {
                if (changesToApply > 0) {
                    liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(getLabelFilter()));
                } else {
                    liquibase.update(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()));
                }
            });
        } catch (Exception exception) {
            if (exception instanceof LiquibaseException) {
                handleUpdateException((LiquibaseException) exception); //need this until update-to-tag and update-count are refactored
                throw (LiquibaseException) exception;
            } else {
                throw new LiquibaseException(exception);
            }
        }
    }

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
        getLog().info(indent + "drop first? " + dropFirst);
    }
}
