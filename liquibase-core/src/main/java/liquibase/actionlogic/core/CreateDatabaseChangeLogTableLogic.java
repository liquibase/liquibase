package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.CreateDatabaseChangeLogTableAction;
import liquibase.action.core.CreateTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.Column;
import liquibase.structure.core.DataType;
import liquibase.structure.core.Table;

public class CreateDatabaseChangeLogTableLogic extends AbstractActionLogic<CreateDatabaseChangeLogTableAction> {

    @Override
    protected Class<CreateDatabaseChangeLogTableAction> getSupportedAction() {
        return CreateDatabaseChangeLogTableAction.class;
    }

    @Override
    public ActionResult execute(CreateDatabaseChangeLogTableAction action, Scope scope) throws ActionPerformException {
        Database database = scope.getDatabase();

        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);

        ObjectReference tableName = new ObjectReference(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName());
        CreateTableAction createTableAction = new CreateTableAction(new Table(tableName))
                .addColumn(new Column(tableName, "ID", new DataType(charTypeName, 255), false))
                .addColumn(new Column(tableName, "AUTHOR", new DataType(charTypeName, 255), false))
                .addColumn(new Column(tableName, "FILENAME", new DataType(charTypeName, 255), false))
                .addColumn(new Column(tableName, "DATEEXECUTED", new DataType(dateTimeTypeString), false))
                .addColumn(new Column(tableName, "ORDEREXECUTED", new DataType(DataType.StandardType.INTEGER), false))
                .addColumn(new Column(tableName, "EXECTYPE", new DataType(charTypeName, 10), false))
                .addColumn(new Column(tableName, "MD5SUM", new DataType(charTypeName, 35)))
                .addColumn(new Column(tableName, "DESCRIPTION", new DataType(charTypeName, 255)))
                .addColumn(new Column(tableName, "COMMENTS", new DataType(charTypeName, 255)))
                .addColumn(new Column(tableName, "TAG", new DataType(charTypeName, 255)))
                .addColumn(new Column(tableName, "LIQUIBASE", new DataType(charTypeName, 20)));
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
