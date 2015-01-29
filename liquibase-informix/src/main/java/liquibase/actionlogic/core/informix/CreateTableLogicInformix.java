package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.CreateTableAction;
import liquibase.action.core.ForeignKeyDefinition;
import liquibase.action.core.StringClauses;
import liquibase.action.core.UniqueConstraintDefinition;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;

public class CreateTableLogicInformix extends CreateTableLogic {


    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.get(CreateTableAction.Attr.tablespace, String.class);
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "IN "+tablespace);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateUniqueConstraintSql(UniqueConstraintDefinition uniqueConstraint, Action action, Scope scope) {
        StringClauses clauses = super.generateUniqueConstraintSql(uniqueConstraint, action, scope);

        String constraintClause = clauses.get(UniqueConstraintClauses.constraintName);
        if (constraintClause!= null) {
            clauses.remove(UniqueConstraintClauses.constraintName);
            clauses.append(constraintClause);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateForeignKeySql(ForeignKeyDefinition foreignKeyDefinition, Action action, Scope scope) {
        StringClauses clauses = super.generateForeignKeySql(foreignKeyDefinition, action, scope);
        String nameClause = clauses.get(ForeignKeyClauses.constraintName);

        clauses.remove(ForeignKeyClauses.constraintName);
        clauses.append(ForeignKeyClauses.constraintName, nameClause);

        return clauses;
    }
}
