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

public class CreateDatabaseChangeLogTableLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateDatabaseChangeLogTableAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);

        String charTypeName = getCharTypeName(database);
        String dateTimeTypeString = getDateTimeTypeString(database);

        return new DelegateResult((CreateTableAction) new CreateTableAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()))
                .addColumn((ColumnDefinition) new ColumnDefinition("ID", charTypeName+"(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("AUTHOR", charTypeName+"(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("FILENAME", charTypeName+"(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("DATEEXECUTED", dateTimeTypeString).set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("ORDEREXECUTED", "INT").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("EXECTYPE", charTypeName+"(10)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn(new ColumnDefinition("MD5SUM", charTypeName+"(35)"))
                .addColumn(new ColumnDefinition("DESCRIPTION", charTypeName+"(255)"))
                .addColumn(new ColumnDefinition("COMMENTS", charTypeName+"(255)"))
                .addColumn(new ColumnDefinition("TAG", charTypeName+"(255)"))
                .addColumn(new ColumnDefinition("LIQUIBASE", charTypeName+"(20)"))
                .set(CreateTableAction.Attr.tablespace, database.getLiquibaseTablespaceName())
        );
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
