package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.action.core.AddPrimaryKeysAction;
import liquibase.action.core.AlterTableAction;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.PrimaryKey;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddPrimaryKeysLogic extends AbstractActionLogic<AddPrimaryKeysAction> {

    public static enum Clauses {
        constraintName, columnNames, tablespace,
    }

    @Override
    protected Class<AddPrimaryKeysAction> getSupportedAction() {
        return AddPrimaryKeysAction.class;
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeysAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        for (PrimaryKey primaryKey : action.primaryKeys) {
            errors.checkForRequiredField("columns", primaryKey);
            if (ObjectUtil.defaultIfEmpty(primaryKey.clustered, false)) {
                errors.addUnsupportedError("Adding a clustered primary key", scope.getDatabase().getShortName());
            }
        }

        return errors;
    }

    @Override
    public ActionResult execute(AddPrimaryKeysAction action, Scope scope) throws ActionPerformException {

        List<Action> actions = new ArrayList<>();

        for (PrimaryKey pk : action.primaryKeys) {
            actions.addAll(Arrays.asList(execute(pk, action, scope)));
        }

        return new DelegateResult(actions.toArray(new Action[actions.size()]));
    }

    protected Action execute(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        return new AlterTableAction(
                pk.columns.get(0).container,
                generateSql(pk, action, scope)
        );
    }


    protected StringClauses generateSql(PrimaryKey pk, AddPrimaryKeysAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses(" ");

        clauses.append("ADD").append("CONSTRAINT");

        //TODO: Informix logic from AddPrimaryKeyGeneratorInformix:
//        // Using auto-generated names of the form <constraint_type><tabid>_<constraintid> can cause collisions
//        // See here: http://www-01.ibm.com/support/docview.wss?uid=swg21156047
//        String constraintName = statement.getConstraintName();
//        if (constraintName != null && !constraintName.matches("[urcn][0-9]+_[0-9]+")) {
//            sql.append(" CONSTRAINT ");
//            sql.append(database.escapeConstraintName(constraintName));
//        }
//
        clauses.append(Clauses.constraintName, database.escapeObjectName(pk.getSimpleName(), PrimaryKey.class));
        clauses.append("PRIMARY KEY");
        clauses.append(Clauses.columnNames, new StringClauses("(", ", ", ")").append(pk.columns, Column.class, scope));

        String tablespace = pk.tablespace;
        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;
    }
}
