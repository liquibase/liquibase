package org.liquibase.ide.common.action;

import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import org.liquibase.ide.common.IdeFacade;

public abstract class MigratorAction extends BaseDatabaseAction {
    protected MigratorAction(String title) {
        super(title);
    }

    @Override
    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(Database.class);
    }


    public abstract void actionPerform(Database database, IdeFacade ideFacade) throws LiquibaseException;
}
