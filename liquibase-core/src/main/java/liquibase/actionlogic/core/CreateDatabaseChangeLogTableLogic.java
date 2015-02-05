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

        return new DelegateResult((CreateTableAction) new CreateTableAction(new ObjectName(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogTableName()))
                .addColumn((ColumnDefinition) new ColumnDefinition("ID", "VARCHAR(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("AUTHOR", "VARCHAR(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("FILENAME", "VARCHAR(255)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("DATEEXECUTED", "DATETIME").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("ORDEREXECUTED", "INT").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("EXECTYPE", "VARCHAR(10)").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn(new ColumnDefinition("MD5SUM", "VARCHAR(35)"))
                .addColumn(new ColumnDefinition("DESCRIPTION", "VARCHAR(255)"))
                .addColumn(new ColumnDefinition("COMMENTS", "VARCHAR(255)"))
                .addColumn(new ColumnDefinition("TAG", "VARCHAR(255)"))
                .addColumn(new ColumnDefinition("LIQUIBASE", "VARCHAR(20)"))
                .set(CreateTableAction.Attr.tablespace, database.getLiquibaseTablespaceName())
        );
    }
}
