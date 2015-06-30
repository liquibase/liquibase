package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.core.DropTableAction;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Table;
import liquibase.util.ObjectUtil;
import liquibase.util.StringClauses;

public class DropTableLogic extends AbstractSqlBuilderLogic<DropTableAction> {

    @Override
    protected Class<DropTableAction> getSupportedAction() {
        return DropTableAction.class;
    }

    @Override
    public ValidationErrors validate(DropTableAction action, Scope scope) {
        Database database = scope.getDatabase();

        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField("tableName", action);

        if (ObjectUtil.defaultIfEmpty(action.cascadeConstraints, false) && !database.supportsDropTableCascadeConstraints()) {
            errors.addWarning("Database does not support drop with cascade");
        }
        return errors;
    }

    @Override
    protected StringClauses generateSql(DropTableAction action, Scope scope) {
        Database database = scope.getDatabase();
        StringClauses clauses = new StringClauses()
                .append("DROP TABLE")
                .append(database.getQualifiedName(action.tableName, Table.class));

        if (ObjectUtil.defaultIfEmpty(action.cascadeConstraints, false) && database.supportsDropTableCascadeConstraints()) {
            clauses.append("CASCADE");
        }
        return clauses;
    }
}
