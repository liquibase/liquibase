package liquibase.actionlogic.core.asany;

import liquibase.Scope;
import liquibase.action.Action;
import liquibase.action.core.DropUniqueConstraintActon;
import liquibase.action.core.StringClauses;
import liquibase.actionlogic.core.DropUniqueConstraintLogic;
import liquibase.database.Database;
import liquibase.database.core.asany.SybaseASADatabase;
import liquibase.exception.ValidationErrors;
import liquibase.structure.core.Column;
import liquibase.util.StringUtils;

import java.util.ArrayList;

public class DropUniqueConstraintLogicSybaseASA extends DropUniqueConstraintLogic {
    @Override
    protected Class<? extends Database> getRequiredDatabase() {
        return SybaseASADatabase.class;
    }

    @Override
    public ValidationErrors validate(Action action, Scope scope) {
        return super.validate(action, scope)
                .removeRequiredField(DropUniqueConstraintActon.Attr.constraintName)
                .checkForRequiredField(DropUniqueConstraintActon.Attr.uniqueColumnNames, action);
    }

    @Override
    protected StringClauses generateSql(Action action, Scope scope) {
        Database database = scope.get(Scope.Attr.database, Database.class);
        return new StringClauses()
                .append("DROP UNIQUE")
                .append("(" + StringUtils.join(action.get(DropUniqueConstraintActon.Attr.uniqueColumnNames, new ArrayList<String>()), ", ", new StringUtils.ObjectNameFormatter(Column.class, database)) + ")");
    }
}
