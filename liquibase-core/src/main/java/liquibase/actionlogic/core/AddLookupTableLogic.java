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
import liquibase.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddLookupTableLogic extends AbstractActionLogic<AddLookupTableAction> {
    @Override
    protected Class<AddLookupTableAction> getSupportedAction() {
        return AddLookupTableAction.class;
    }

    @Override
    public ActionResult execute(AddLookupTableAction action, Scope scope) throws ActionPerformException {
        ObjectName newColumnName = action.newColumnName;
        ObjectName newTableName = newColumnName.container;

        String newColumnDataType = action.newColumnDataType;

        ObjectName existingColumnName = action.existingColumnName;
        ObjectName existingTableName = existingColumnName.container;

        List<Action> actions = new ArrayList<>(Arrays.asList(generateCreateAndLoadActions(action, scope)));

        actions.add(new SetNullableAction(newColumnName, newColumnDataType, false));

        AddPrimaryKeyAction addPkAction = new AddPrimaryKeyAction();
        addPkAction.tableName = newTableName;
        addPkAction.columnNames = Arrays.asList(newColumnName.name);

        actions.add(addPkAction);

//        actions.add(new AddForeignKeyConstraintAction(
//                action.constraintName,
//                newTableName,
//                newColumnName.name,
//                existingTableName,
//                existingColumnName.name));

        return new DelegateResult(actions);
    }

    public Action[] generateCreateAndLoadActions(AddLookupTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new Action[]{
                new ExecuteSqlAction(
                        "CREATE TABLE "
                                + database.escapeObjectName(action.newColumnName.container, Table.class)
                                + " AS SELECT DISTINCT "
                                + database.escapeObjectName(action.existingColumnName, Column.class)
                                + " AS "
                                + database.escapeObjectName(action.newColumnName, Column.class)
                                + " FROM "
                                + database.escapeObjectName(action.existingColumnName.container, Table.class)
                                + " WHERE "
                                + database.escapeObjectName(action.existingColumnName, Column.class)
                                + " IS NOT NULL")
        };
    }

}
