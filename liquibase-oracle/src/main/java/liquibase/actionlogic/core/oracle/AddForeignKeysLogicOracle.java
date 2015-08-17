package liquibase.actionlogic.core.oracle;

import liquibase.Scope;
import liquibase.action.core.AddForeignKeysAction;
import liquibase.actionlogic.core.AddForeignKeysLogic;
import liquibase.database.Database;
import liquibase.database.core.oracle.OracleDatabase;
import liquibase.structure.core.ForeignKey;
import liquibase.util.StringClauses;

public class AddForeignKeysLogicOracle extends AddForeignKeysLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return OracleDatabase.class;
    }

    @Override
    protected StringClauses generateSql(ForeignKey foreignKey, AddForeignKeysAction action, Scope scope) {
        return super.generateSql(foreignKey, action, scope)
                .remove("ON UPDATE")
                .remove("ON DELETE RESTRICT")
                .remove("ON DELETE NO ACTION")
                ;
    }
}
