package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddForeignKeyConstraintAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.core.Column;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddLookupTableLogic extends AbstractActionLogic {
    @Override
    protected Class<? extends Action> getSupportedAction() {
        return AddLookupTableAction.class;
    }

    @Override
    public ActionResult execute(Action action, Scope scope) throws ActionPerformException {
        String newTableCatalogName = action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class);
        String newTableSchemaName = action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class);
        String newTableName = action.get(AddLookupTableAction.Attr.newTableName, String.class);
        String newColumnName = action.get(AddLookupTableAction.Attr.newColumnName, String.class);
        String newColumnDataType = action.get(AddLookupTableAction.Attr.newColumnDataType, String.class);

        String existingTableCatalogName = action.get(AddLookupTableAction.Attr.existingTableCatalogName, String.class);
        String existingTableSchemaName = action.get(AddLookupTableAction.Attr.existingTableSchemaName, String.class);
        String existingTableName = action.get(AddLookupTableAction.Attr.existingTableName, String.class);
        String existingColumnName = action.get(AddLookupTableAction.Attr.existingColumnName, String.class);

        List<Action> actions = new ArrayList<>(Arrays.asList(generateCreateAndLoadActions(action, scope)));

        actions.add(new SetNullableAction(newTableCatalogName, newTableSchemaName, newTableName, newColumnName, newColumnDataType, false));

        actions.add((Action) new AddPrimaryKeyAction()
                .set(AddPrimaryKeyAction.Attr.catalogName, newTableCatalogName)
                .set(AddPrimaryKeyAction.Attr.schemaName, newTableSchemaName)
                .set(AddPrimaryKeyAction.Attr.tableName, newTableName)
                .set(AddPrimaryKeyAction.Attr.columnNames, newColumnName));

        actions.add(new AddForeignKeyConstraintAction(
                action.get(AddLookupTableAction.Attr.constraintName, String.class),
                newTableCatalogName,
                newTableSchemaName,
                newTableName,
                new String[]{newColumnName},
                existingTableCatalogName,
                existingTableSchemaName,
                existingTableName,
                new String[]{existingColumnName}));

        return new DelegateResult(actions);
    }

    public Action[] generateCreateAndLoadActions(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new Action[]{
                new ExecuteSqlAction(
                        "CREATE TABLE "
                                + database.escapeTableName(action.get(AddLookupTableAction.Attr.newTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.newTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.newTableName, String.class))
                                + " AS SELECT DISTINCT "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                                + " AS "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                                + " FROM "
                                + database.escapeTableName(action.get(AddLookupTableAction.Attr.existingTableCatalogName, String.class), action.get(AddLookupTableAction.Attr.existingTableSchemaName, String.class), action.get(AddLookupTableAction.Attr.existingTableName, String.class))
                                + " WHERE "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                                + " IS NOT NULL")
        };
    }

}
