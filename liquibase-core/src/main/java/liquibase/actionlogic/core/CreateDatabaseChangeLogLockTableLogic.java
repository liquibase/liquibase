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

public class CreateDatabaseChangeLogLockTableLogic extends AbstractActionLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return CreateDatabaseChangeLogLockTableAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new DelegateResult((CreateTableAction) new CreateTableAction(database.getLiquibaseCatalogName(), database.getLiquibaseSchemaName(), database.getDatabaseChangeLogLockTableName())
                .addColumn((ColumnDefinition) new ColumnDefinition("ID", "int").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn((ColumnDefinition) new ColumnDefinition("LOCKED", "BOOLEAN").set(ColumnDefinition.Attr.isNullable, false))
                .addColumn(new ColumnDefinition("LOCKGRANTED", "DATETIME"))
                .addColumn(new ColumnDefinition("LOCKEDBY", "VARCHAR(255)"))
                .set(CreateTableAction.Attr.tablespace, database.getLiquibaseTablespaceName()));
    }
}
