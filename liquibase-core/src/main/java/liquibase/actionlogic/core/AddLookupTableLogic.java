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
import liquibase.structure.ObjectName;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

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
        ObjectName newColumnName = action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class);
        ObjectName newTableName = newColumnName.getContainer();

        String newColumnDataType = action.get(AddLookupTableAction.Attr.newColumnDataType, String.class);

        ObjectName existingColumnName = action.get(AddLookupTableAction.Attr.existingColumnName, ObjectName.class);
        ObjectName existingTableName = existingColumnName.getContainer();

        List<Action> actions = new ArrayList<>(Arrays.asList(generateCreateAndLoadActions(action, scope)));

        actions.add(new SetNullableAction(newColumnName, newColumnDataType, false));

        actions.add((Action) new AddPrimaryKeyAction()
                .set(AddPrimaryKeyAction.Attr.tableName, newTableName)
                .set(AddPrimaryKeyAction.Attr.columnNames, newColumnName));

        actions.add(new AddForeignKeyConstraintAction(
                action.get(AddLookupTableAction.Attr.constraintName, String.class),
                newTableName,
                new String[]{newColumnName.getName()},
                existingTableName,
                new String[]{existingColumnName.getName()}));

        return new DelegateResult(actions);
    }

    public Action[] generateCreateAndLoadActions(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new Action[]{
                new ExecuteSqlAction(
                        "CREATE TABLE "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, ObjectName.class).getContainer(), Table.class)
                                + " AS SELECT DISTINCT "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                                + " AS "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.newColumnName, String.class), Column.class)
                                + " FROM "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, ObjectName.class).getContainer(), Table.class)
                                + " WHERE "
                                + database.escapeObjectName(action.get(AddLookupTableAction.Attr.existingColumnName, String.class), Column.class)
                                + " IS NOT NULL")
        };
    }

}
