package liquibase.datatype.core;

import java.util.Arrays;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.util.StringUtils;

@DataTypeInfo(name="float", aliases = {"java.sql.Types.FLOAT", "java.lang.Float", "real", "java.sql.Types.REAL"}, minParameters = 0, maxParameters = 2, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class FloatType  extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        String originalDefinition = StringUtils.trimToEmpty(getRawDefinition());
        if (database instanceof MSSQLDatabase) {
            if ("real".equalsIgnoreCase(originalDefinition)
                    || "[real]".equals(originalDefinition)
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
        if (database instanceof MySQLDatabase || database instanceof AbstractDb2Database
                || database instanceof H2Database) {
            if (originalDefinition.equalsIgnoreCase("REAL")) {
                return new DatabaseDataType("REAL");
            }
        }
        if (database instanceof FirebirdDatabase || database instanceof InformixDatabase) {
            return new DatabaseDataType("FLOAT");
        }
        return super.toDatabaseDataType(database);
    }

    //sqlite
    //        } else if (columnTypeString.equals("REAL") ||
//                columnTypeString.toLowerCase(Locale.ENGLISH).contains("float")) {
    //            type = new FloatType("REAL");



    //postgres
    //        } else if (type.toDatabaseDataType().toLowerCase().startsWith("float8")) {
//            type.setDataTypeName("FLOAT8");
//        } else if (type.toDatabaseDataType().toLowerCase().startsWith("float4")) {
//            type.setDataTypeName("FLOAT4");

}
