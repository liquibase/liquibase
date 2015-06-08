package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.AddPrimaryKeyAction;
import liquibase.action.core.AlterTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.actionlogic.ActionResult;
import liquibase.actionlogic.DelegateResult;
import liquibase.database.Database;
import liquibase.exception.ActionPerformException;
import liquibase.exception.ValidationErrors;
import liquibase.structure.ObjectName;
import liquibase.util.ObjectUtil;
import liquibase.util.StringUtils;

public class AddPrimaryKeyLogic extends AbstractSqlBuilderLogic<AddPrimaryKeyAction> {

    public static enum Clauses {
        constraintName, columnNames, tablespace,
    }

    @Override
    protected Class<AddPrimaryKeyAction> getSupportedAction() {
        return AddPrimaryKeyAction.class;
    }

    @Override
    public ValidationErrors validate(AddPrimaryKeyAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        errors.checkForRequiredField("columnNames", action);
        errors.checkForRequiredField("tableName", action);
        if (ObjectUtil.defaultIfEmpty(action.clustered, false)) {
            errors.addUnsupportedError("Adding a clustered primary key", scope.getDatabase().getShortName());
        }

        return errors;
    }

    @Override
    public ActionResult execute(AddPrimaryKeyAction action, Scope scope) throws ActionPerformException {
        return new DelegateResult(
                new AlterTableAction(action.tableName,
                        generateSql(action, scope)));
    }

    @Override
    protected StringClauses generateSql(AddPrimaryKeyAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses();

        clauses.append("ADD CONSTRAINT");

        //TODO: Informix logic from AddPrimaryKeyGeneratorInformix:
//        // Using auto-generated names of the form <constraint_type><tabid>_<constraintid> can cause collisions
//        // See here: http://www-01.ibm.com/support/docview.wss?uid=swg21156047
//        String constraintName = statement.getConstraintName();
//        if (constraintName != null && !constraintName.matches("[urcn][0-9]+_[0-9]+")) {
//            sql.append(" CONSTRAINT ");
//            sql.append(database.escapeConstraintName(constraintName));
//        }
//
        clauses.append(Clauses.constraintName, database.escapeConstraintName(action.constraintName));
        clauses.append("PRIMARY KEY");
        clauses.append(Clauses.columnNames, "(" + database.escapeColumnNameList(StringUtils.join(action.columnNames, ", ")) + ")");

        String tablespace = action.tablespace;
        if (tablespace != null && database.supportsTablespaces()) {
            clauses.append(Clauses.tablespace, "USING INDEX TABLESPACE " + tablespace);
        }

        return clauses;
    }
}
