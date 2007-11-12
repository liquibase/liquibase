package org.liquibase.ide.common.action;

import liquibase.migrator.Migrator;
import liquibase.exception.LiquibaseException;

public abstract class MigratorAction extends BaseDatabaseAction {
    protected MigratorAction(String title) {
        super(title);
    }

    public abstract void actionPerform(Migrator migrator) throws LiquibaseException;
}
