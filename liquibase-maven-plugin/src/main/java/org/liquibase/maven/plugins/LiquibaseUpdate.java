package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.command.CommandScope;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.ChangeExecListenerUtils;
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
     * [PRO] If set to true and any changeset in a deployment fails, then the update operation stops, and liquibase attempts to rollback all changesets just deployed. A changeset marked "fail-on-error=false" does not trigger as an error, therefore rollback-on-error will not occur. Additionally, if a changeset is not auto-rollback compliant or does not have a rollback script, then no rollback-on-error will occur for any changeset.
     *
     * @parameter property="liquibase.rollbackOnError" default-value="false"
     */
    @PropertyElement
    protected boolean rollbackOnError;

    @Override
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        if (dropFirst) {
            liquibase.dropAll();
        }
        try {
            if (changesToApply > 0) {
                liquibase.update(changesToApply, new Contexts(contexts), new LabelExpression(getLabelFilter()));
            } else {
                liquibase.update(toTag, new Contexts(contexts), new LabelExpression(getLabelFilter()));
            }
        } catch (LiquibaseException exception) {
            if (rollbackOnError) {
                CommandScope liquibaseCommand = new CommandScope("internalRollbackOnError");
                liquibaseCommand.addArgumentValue("database", liquibase.getDatabase());
                liquibaseCommand.addArgumentValue("exception", exception);
                liquibaseCommand.addArgumentValue("listener", defaultChangeExecListener);
                liquibaseCommand.addArgumentValue("rollbackOnError", rollbackOnError);
                liquibaseCommand.execute();
            } else {
                throw exception;
            }
        }
    }


    @Override
    protected void printSettings(String indent) {
        super.printSettings(indent);
        getLog().info(indent + "drop first? " + dropFirst);
    }
}
