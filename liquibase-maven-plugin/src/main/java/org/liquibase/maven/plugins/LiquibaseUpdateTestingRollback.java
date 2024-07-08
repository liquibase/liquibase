package org.liquibase.maven.plugins;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
import liquibase.exception.LiquibaseException;

/**
 * <p>Applies the DatabaseChangeLogs to the database, testing rollback. This is
 * done by updating the database, rolling it back then updating it again.</p>
 *
 * @description Liquibase UpdateTestingRollback Maven plugin
 * @goal updateTestingRollback
 */
public class LiquibaseUpdateTestingRollback extends AbstractLiquibaseUpdateMojo {

    @Override
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        try {
            Scope.child("rollbackOnError", rollbackOnError, () -> {
                liquibase.updateTestingRollback(new Contexts(contexts), new LabelExpression(getLabelFilter()));
            });
        } catch (Exception exception) {
            if (exception instanceof LiquibaseException) {
                throw (LiquibaseException) exception;
            } else {
                throw new LiquibaseException(exception);
            }
        }
    }
}
