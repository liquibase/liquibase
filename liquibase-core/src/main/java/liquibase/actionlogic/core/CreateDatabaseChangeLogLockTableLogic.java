package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateDatabaseChangeLogLockTableAction;
import liquibase.action.core.CreateTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

public class CreateDatabaseChangeLogLockTableLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateDatabaseChangeLogLockTableAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);

        return new DelegateResult((CreateTableAction) new CreateTableAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()))
                .addColumn((ColumnDefinition) new ColumnDefinition("ID", "int").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("LOCKED", "BOOLEAN").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn(new ColumnDefinition("LOCKGRANTED", dateTimeTypeString))
                .addColumn(new ColumnDefinition("LOCKEDBY", charTypeName + "(255)"))
                .set(CreateTableAction.Attr.tablespace, database.getLiquibaseTablespaceName()));
    }

    protected String getCharTypeName(Database database) {
//        if (database instanceof MSSQLDatabase && ((MSSQLDatabase) database).sendsStringParametersAsUnicode()) {
//            return "nvarchar";
//        }
        return "varchar";
    }

    protected String getDateTimeTypeString(Database database) {
//        if (database instanceof MSSQLDatabase) {
//            try {
//                if (database.getDatabaseMajorVersion() >= 10) { // 2008 or later
//                    return "datetime2(3)";
//                }
//            } catch (DatabaseException e) {
//                // ignore
//            }
//        }
        return "datetime";
    }
}