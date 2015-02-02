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

public class TagDatabaseLogicInformix extends TagDatabaseLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult(new ExecuteSqlAction("SELECT MAX(dateexecuted) max_date FROM " + database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()) + " INTO TEMP max_date_temp WITH NO LOG"),
                new ExecuteSqlAction("UPDATE "+database.escapeTableName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName())+" SET TAG = '"+database.escapeStringForDatabase(action.get(TagDatabaseAction.Attr.tag, String.class))+"' WHERE DATEEXECUTED = (SELECT max_date FROM max_date_temp);"),
                (DropTableAction) new DropTableAction().set(DropTableAction.Attr.tableName, "max_date_temp"));
    }
}
