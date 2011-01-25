package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;

/**
 * Applies the DatabaseChangeLogs to the database, testing rollback. This is
 * done by updating the database, rolling it back then updating it again.
 * 
 * @description Liquibase UpdateTestingRollback Maven plugin
 * @goal updateTestingRollback
 */
public class LiquibaseUpdateTestingRollback extends AbstractLiquibaseUpdateMojo {

    @Override
    protected void doUpdate(Liquibase liquibase) throws LiquibaseException {
        liquibase.updateTestingRollback(contexts);
    }
}