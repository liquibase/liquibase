package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.CreateTableAction;
import liquibase.actionlogic.core.CreateTableLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.ForeignKey;
import liquibase.structure.core.UniqueConstraint;
import liquibase.util.StringClauses;

public class CreateTableLogicInformix extends CreateTableLogic {


    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(CreateTableAction action, Scope scope) {
        StringClauses clauses = super.generateSql(action, scope);

        String tablespace = action.tablespace;
        if (tablespace != null) {
            clauses.replace(Clauses.tablespace, "IN "+tablespace);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateUniqueConstraintSql(UniqueConstraint uniqueConstraint, CreateTableAction action, Scope scope) {
        StringClauses clauses = super.generateUniqueConstraintSql(uniqueConstraint, action, scope);

        String constraintClause = clauses.get(UniqueConstraintClauses.constraintName);
        if (constraintClause!= null) {
            clauses.remove(UniqueConstraintClauses.constraintName);
            clauses.append(constraintClause);
        }

        return clauses;
    }

    @Override
    protected StringClauses generateForeignKeySql(ForeignKey foreignKey, CreateTableAction action, Scope scope) {
        StringClauses clauses = super.generateForeignKeySql(foreignKey, action, scope);
        String nameClause = clauses.get(ForeignKeyClauses.constraintName);

        clauses.remove(ForeignKeyClauses.constraintName);
        clauses.append(ForeignKeyClauses.constraintName, nameClause);

        return clauses;
    }
}
