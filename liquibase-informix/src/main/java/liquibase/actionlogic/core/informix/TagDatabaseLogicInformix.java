package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.DropTableAction;
import liquibase.action.core.TagDatabaseAction;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.actionlogic.core.TagDatabaseLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Table;

public class TagDatabaseLogicInformix extends TagDatabaseLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public ActionResult execute(TagDatabaseAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        DropTableAction dropTableAction = new DropTableAction();
        dropTableAction.tableName = new ObjectName("max_date_temp");

        return new DelegateResult(new ExecuteSqlAction("SELECT MAX(dateexecuted) max_date FROM " + database.escapeObjectName(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()), Table.class) + " INTO TEMP max_date_temp WITH NO LOG"),
                new ExecuteSqlAction("UPDATE "+database.escapeObjectName(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()), Table.class)+" SET TAG = '"+database.escapeStringForDatabase(action.tag)+"' WHERE DATEEXECUTED = (SELECT max_date FROM max_date_temp);"),
                dropTableAction);
    }
}
