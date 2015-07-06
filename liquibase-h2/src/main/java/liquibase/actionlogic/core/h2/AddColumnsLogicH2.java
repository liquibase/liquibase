package liquibase.actionlogic.core.h2;

import liquibase.Scope;
import liquibase.action.core.AddColumnsAction;
import liquibase.actionlogic.core.AddColumnsLogic;
import liquibase.database.Database;
import liquibase.database.core.h2.H2Database;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class AddColumnsLogicH2 extends AddColumnsLogic {

    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return H2Database.class;
    }

    @Override
    public ValidationErrors validate(AddColumnsAction action, Scope scope) {
        ValidationErrors errors = super.validate(action, scope);
        for (Column column : action.columns) {
//            if (ObjectUtil.defaultIfEmpty(column.isPrimaryKey, false)) {
//                errors.addUnsupportedError("Adding a primary key column is not supported", scope.getDatabase().getShortName());
//            }
        }
        return errors;
    }

    @Override
    protected StringClauses getColumnClause(Column column, AddColumnsAction action, Scope scope) {
        StringClauses clauses = super.getColumnClause(column, action, scope);

        if (StringUtils.trimToNull(column.remarks) != null) {
            clauses.insertAfter(Clauses.primaryKey, "COMMENT '"+scope.getDatabase().escapeStringForDatabase(column.remarks)+"'");
        }

        return clauses;

    }
}
