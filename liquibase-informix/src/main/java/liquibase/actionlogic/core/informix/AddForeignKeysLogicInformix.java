package liquibase.actionlogic.core.informix;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.actionlogic.core.AddForeignKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.informix.InformixDatabase;
import liquibase.structure.core.ForeignKey;
import liquibase.util.StringClauses;

public class AddForeignKeysLogicInformix extends AddForeignKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return InformixDatabase.class;
    }

    @Override
    protected StringClauses generateSql(ForeignKey foreignKey, AddForeignKeysAction action, Scope scope)  {
        StringClauses clauses = super.generateSql(foreignKey, action, scope)
                .remove(Clauses.constraintName)
                .remove("ON UPDATE")
                .remove("ON DELETE CASCADE");

        clauses.append("CONSTRAINT "+foreignKey.name);

        return clauses;
    }
}
