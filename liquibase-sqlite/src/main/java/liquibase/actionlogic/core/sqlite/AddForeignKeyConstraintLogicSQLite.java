package liquibase.actionlogic.core.sqlite;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.actionlogic.UnsupportedActionLogic;
import liquibase.database.Database;
import liquibase.database.core.sqlite.SQLiteDatabase;

public class AddForeignKeyConstraintLogicSQLite extends UnsupportedActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddForeignKeyConstraintAction.class;
    }

    @Override
    protected Class<? extends Database> getUnsupportedDatabase() {
        return SQLiteDatabase.class;
    }
}
