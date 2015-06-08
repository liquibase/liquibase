package liquibase.actionlogic.core;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropViewAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.AbstractActionLogic;
import liquibase.actionlogic.AbstractSqlBuilderLogic;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.sql.UnparsedSql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
import liquibase.statement.core.DropViewStatement;
import liquibase.structure.ObjectName;
import liquibase.structure.core.Relation;
import liquibase.structure.core.View;

public class DropViewLogic extends AbstractSqlBuilderLogic<DropViewAction> {

    @Override
    protected Class<DropViewAction> getSupportedAction() {
        return DropViewAction.class;
    }

    @Override
    public ValidationErrors validate(DropViewAction action, Scope scope) {
        return super.validate(action, scope)
                .checkForRequiredField("viewName", action);
    }

    @Override
    protected StringClauses generateSql(DropViewAction action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP VIEW")
                .append(database.escapeObjectName(action.viewName, View.class
                ));
    }
}
