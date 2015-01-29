package liquibase.actionlogic.core.mysql;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.ExecuteSqlAction;
import liquibase.action.core.ColumnDefinition;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.mysql.MySQLDatabase;

import java.util.List;

public class CreateTableLogicMySQL extends CreateTableLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return MySQLDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);

        StringClauses clauses = super.generateSql(action, scope);
        String remarks = action.get(CreateTableAction.Attr.remarks, String.class);
        if (remarks != null) {
            clauses.append("COMMENT='"+database.escapeStringForDatabase(remarks)+"'");
        }

        return clauses;
    }

    @Override
    protected StringClauses generateColumnSql(ColumnDefinition column, Action action, Scope scope, List<Action> additionalActions) {
        StringClauses clauses = super.generateColumnSql(column, action, scope, additionalActions);

        Database database = scope.get(Scope.Attr.database, Database.class);
        String remarks = column.get(ColumnDefinition.Attr.remarks, String.class);

        if (remarks != null) {
            clauses.append("COMMENT '" + database.escapeStringForDatabase(remarks) + "'");
        }


        return clauses;
    }
}
