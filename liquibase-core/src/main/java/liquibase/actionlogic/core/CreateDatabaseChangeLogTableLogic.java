package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateDatabaseChangeLogTableAction;
import liquibase.action.core.CreateTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectName;

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

        CreateTableAction createTableAction = new CreateTableAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()))
                .addColumn(new ColumnDefinition("ID", charTypeName + "(255)", false))
                .addColumn(new ColumnDefinition("AUTHOR", charTypeName + "(255)", false))
                .addColumn(new ColumnDefinition("FILENAME", charTypeName + "(255)", false))
                .addColumn(new ColumnDefinition("DATEEXECUTED", dateTimeTypeString, false))
                .addColumn(new ColumnDefinition("ORDEREXECUTED", "INT", false))
                .addColumn(new ColumnDefinition("EXECTYPE", charTypeName + "(10)", false))
                .addColumn(new ColumnDefinition("MD5SUM", charTypeName + "(35)"))
                .addColumn(new ColumnDefinition("DESCRIPTION", charTypeName + "(255)"))
                .addColumn(new ColumnDefinition("COMMENTS", charTypeName + "(255)"))
                .addColumn(new ColumnDefinition("TAG", charTypeName + "(255)"))
                .addColumn(new ColumnDefinition("LIQUIBASE", charTypeName + "(20)"));
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
