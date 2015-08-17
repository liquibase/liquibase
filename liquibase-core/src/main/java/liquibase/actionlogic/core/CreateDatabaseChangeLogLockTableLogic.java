package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.CreateDatabaseChangeLogLockTableAction;
import liquibase.action.core.CreateTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.OldDataType;
import liquibase.structure.core.Table;

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

        ObjectName tableName = new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName());

        Column idColumn = new Column(new ObjectName(tableName, "ID"));
        idColumn.type = new DataType("int");
        idColumn.nullable = false;

        Column lockedColumn = new Column(new ObjectName(tableName, "LOCKED"));
        lockedColumn.type = new DataType("BOOLEAN");
        lockedColumn.nullable = false;

        CreateTableAction createTableAction = new CreateTableAction(new Table(tableName))
                .addColumn(idColumn)
                .addColumn(lockedColumn)
                .addColumn(new Column(new ObjectName(tableName, "LOCKGRANTED"), dateTimeTypeString))
                .addColumn(new Column(new ObjectName(tableName, "LOCKEDBY"), charTypeName + "(255)"));
        createTableAction.table.tablespace = database.getLiquibaseTablespaceName();

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