package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.AbstractAction;
import liquibase.action.Action;
import liquibase.action.core.DropTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.logging.LogFactory;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropTableStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Relation;
import liquibase.structure.core.Table;

public class DropTableLogic extends AbstractSqlBuilderLogic {

    @Override
    protected Class<? extends Action> getSupportedAction() {
        return DropTableAction.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        ValidationErrors errors = super.validate(action, scope)
                .checkForRequiredField(DropTableAction.Attr.tableName, action);

        if (action.get(DropTableAction.Attr.cascadeConstraints, false) && !database.supportsDropTableCascadeConstraints()) {
            errors.addWarning("Database does not support drop with cascade");
        }
        return errors;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        StringClauses clauses = new StringClauses()
                .append("DROP TABLE")
                .append(database.getQualifiedName(action.get(DropTableAction.Attr.tableName, ObjectName.class), Table.class));

        if (action.get(DropTableAction.Attr.cascadeConstraints, false) && database.supportsDropTableCascadeConstraints()) {
            clauses.append("CASCADE");
        }
        return clauses;
    }
}
