package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name="float", aliases = {"java.sql.Types.FLOAT", "java.lang.Float", "real", "java.sql.Types.REAL"}, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class FloatType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtil.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if ("real".equals(originalDefinition.toLowerCase(Locale.US))
                    || "[real]".equals(originalDefinition.toLowerCase(Locale.US))
                    || "java.lang.Float".equals(originalDefinition)
                    || "java.sql.Types.REAL".equals(originalDefinition)) {

                return new DatabaseDataType(database.escapeDataTypeName("real"));
            }
            Object[] parameters = getParameters();
            if (parameters.length == 0) {
                parameters = new Object[] { 53 };
            }
            else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            return new DatabaseDataType(database.escapeDataTypeName("float"), parameters);
        }
        if ((database instanceof MySQLDatabase) || (database instanceof AbstractDb2Database) || (database instanceof H2Database)) {
            if ("REAL".equals(originalDefinition.toUpperCase(Locale.US))) {
                return new DatabaseDataType("REAL");
            }
        }
        if ((database instanceof FirebirdDatabase) || (database instanceof InformixDatabase)) {
            return new DatabaseDataType("FLOAT");
        } else if (database instanceof PostgresDatabase) {
            if ("real".equals(originalDefinition.toLowerCase(Locale.US))) {
                return new DatabaseDataType("REAL");
            }
        }
        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.NUMERIC;
    }

}
