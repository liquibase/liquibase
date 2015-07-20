package liquibase.structure.core.mssql;

import liquibase.Scope;
import liquibase.structure.core.DataType;
import liquibase.structure.core.DataTypeTranslator;

import java.util.regex.Pattern;

public class DataTypeTranslatorMSSQL extends DataTypeTranslator {
    @Override
    public int getPriority(Scope scope) {
        return PRIORITY_SPECIALIZED;
    }

    @Override
    public String toSql(DataType dataType, Scope scope) {
        String sql = super.toSql(dataType, scope);

        if (dataType.name.startsWith("[")) {
            return sql;
        } else {
            return sql.replaceFirst("^" + Pattern.quote(dataType.name), "[" + dataType.name + "]");
        }
    }
}
