package org.liquibase.ide.common.change.action;

import liquibase.database.Database;

public abstract class BaseDatabaseRefactorAction extends BaseRefactorAction {

    public BaseDatabaseRefactorAction(String title) {
        super(title);
    }

    public boolean isApplicableTo(Class objectType) {
        return objectType.equals(Database.class);
    }
}
