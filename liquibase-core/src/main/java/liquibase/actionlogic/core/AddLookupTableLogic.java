package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.AddLookupTableAction;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.action.core.SetNullableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.structure.ObjectReference;
import liquibase.structure.core.PrimaryKey;

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
        ObjectReference newColumnName = action.newColumnName;
        ObjectReference newTableName = newColumnName.container;

        String newColumnDataType = action.newColumnDataType;

        ObjectReference existingColumnName = action.existingColumnName;
        ObjectReference existingTableName = existingColumnName.container;

        List<Action> actions = new ArrayList<>(Arrays.asList(generateCreateAndLoadActions(action, scope)));

        actions.add(new SetNullableAction(newColumnName, newColumnDataType, false));

        AddPrimaryKeysAction addPkAction = new AddPrimaryKeysAction(new PrimaryKey(new ObjectReference(newTableName, null), newColumnName.name));

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
                                + database.escapeObjectName(action.newColumnName.container)
                                + " AS SELECT DISTINCT "
                                + database.escapeObjectName(action.existingColumnName)
                                + " AS "
                                + database.escapeObjectName(action.newColumnName)
                                + " FROM "
                                + database.escapeObjectName(action.existingColumnName.container)
                                + " WHERE "
                                + database.escapeObjectName(action.existingColumnName)
                                + " IS NOT NULL")
        };
    }

}
