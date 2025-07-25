package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.exception.LiquibaseException;
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

    @Override
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        if (dropFirst) {
            liquibase.dropAll();
        }
        if (changesToApply > 0) {
            liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(getLabelFilter()));
        } else {
            liquibase.update(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()));
        }
    }

    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "drop first? " + dropFirst);
    }
}
