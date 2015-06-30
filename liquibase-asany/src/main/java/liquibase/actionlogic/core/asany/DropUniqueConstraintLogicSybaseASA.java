package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.actionlogic.core.DropUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.CollectionUtil;
import liquibase.util.StringClauses;
import liquibase.util.StringUtils;

public class DropUniqueConstraintLogicSybaseASA extends DropUniqueConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    public ValidationErrors validate(DropUniqueConstraintActon action, Scope scope) {
        return super.validate(action, scope)
                .removeRequiredField("constraintName")
                .checkForRequiredField("uniqueColumnNames", action);
    }

    @Override
    protected StringClauses generateSql(DropUniqueConstraintActon action, Scope scope) {
        Database database = scope.getDatabase();
        return new StringClauses()
                .append("DROP UNIQUE")
                .append("(" + StringUtils.join(CollectionUtil.createIfNull(action.uniqueColumnNames), ", ", new StringUtils.ObjectNameFormatter(Column.class, database)) + ")");
    }
}
