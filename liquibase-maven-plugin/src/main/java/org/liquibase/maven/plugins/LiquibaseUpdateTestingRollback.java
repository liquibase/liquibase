package org.liquibase.maven.plugins;

import liquibase.exception.LiquibaseException;
import liquibase.Liquibase;

/**
 * Liquibase Update Maven plugin. This plugin allows for DatabaseChangeLogs to be
 * applied to a database as part of a Maven build process while testing rollback support.
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