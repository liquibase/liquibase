package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

import java.util.Locale;


@DataTypeInfo(name="currency", aliases = {"money", "smallmoney"}, minParameters = 0, maxParameters = 0, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class CurrencyType  extends LiquibaseDataType {

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if (originalDefinition.toLowerCase(Locale.US).startsWith("smallmoney")
                    || originalDefinition.toLowerCase(Locale.US).startsWith("[smallmoney]")) {

                return new DatabaseDataType(database.escapeDataTypeName("smallmoney"));
            }
            return new DatabaseDataType(database.escapeDataTypeName("money"));
        }
        if ((database instanceof InformixDatabase) || (database instanceof SybaseASADatabase) || (database instanceof
            SybaseDatabase)) {
            return new DatabaseDataType("MONEY");
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NUMBER", 15, 2);
        }

        if (database instanceof AbstractDb2Database) {
            return new DatabaseDataType("DECIMAL", 19,4);
        }
        if (database instanceof FirebirdDatabase) {
            return new DatabaseDataType("DECIMAL", 18, 4);
        }
        if (database instanceof SQLiteDatabase) {
            return new DatabaseDataType("REAL");
        }
        return new DatabaseDataType("DECIMAL");
    }
}
