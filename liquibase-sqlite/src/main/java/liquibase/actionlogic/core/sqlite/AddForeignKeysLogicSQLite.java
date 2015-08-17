package liquibase.actionlogic.core.sqlite;

import liquibase.action.Action;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.actionlogic.UnsupportedActionLogic;
import liquibase.database.Database;
import liquibase.database.core.sqlite.SQLiteDatabase;

public class AddForeignKeysLogicSQLite extends UnsupportedActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddForeignKeysAction.class;
    }

    @Override
    protected Class<? extends Database> getUnsupportedDatabase() {
        return SQLiteDatabase.class;
    }
}
