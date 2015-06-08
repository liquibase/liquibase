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

public class CreateDatabaseChangeLogLockTableLogic extends AbstractActionLogic<CreateDatabaseChangeLogLockTableAction> {

    @Override
    protected Class<CreateDatabaseChangeLogLockTableAction> getSupportedAction() {
        return CreateDatabaseChangeLogLockTableAction.class;
    }

    @Override
    public ActionResult execute(CreateDatabaseChangeLogLockTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();
        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);

        ColumnDefinition idColumn = new ColumnDefinition("ID", "int");
        idColumn.isNullable = false;

        ColumnDefinition lockedColumn = new ColumnDefinition("LOCKED", "BOOLEAN");
        lockedColumn.isNullable = false;

        CreateTableAction createTableAction = new CreateTableAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName()))
                .addColumn(idColumn)
                .addColumn(lockedColumn)
                .addColumn(new ColumnDefinition("LOCKGRANTED", dateTimeTypeString))
                .addColumn(new ColumnDefinition("LOCKEDBY", charTypeName + "(255)"));
        createTableAction.tablespace = database.getLiquibaseTablespaceName();

        return new DelegateResult(createTableAction);
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