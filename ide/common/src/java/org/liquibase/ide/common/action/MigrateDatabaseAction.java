package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.migrator.Migrator;
import liquibase.exception.LiquibaseException;

public class MigrateDatabaseAction extends MigratorAction {

    public MigrateDatabaseAction() {
        super("Update Database");
    }

    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(Database.class);
    }

    public void actionPerform(Migrator migrator) throws LiquibaseException {
        migrator.migrate();
    }
}
