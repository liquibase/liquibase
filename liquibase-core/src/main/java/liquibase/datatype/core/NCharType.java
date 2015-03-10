package liquibase.datatype.core;

import java.math.BigInteger;
import java.util.Arrays;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name="nchar", aliases = { "java.sql.Types.NCHAR", "nchar2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NCharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof HsqlDatabase) {
            return new DatabaseDataType("CHAR", getParameters());
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NCHAR", getParameters());
        }
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length > 0) {
                String param1 = parameters[0].toString();
                if (!param1.matches("\\d+")
                        || new BigInteger(param1).compareTo(BigInteger.valueOf(4000)) > 0) {

                    return new DatabaseDataType("[nchar]", 4000);
                }
            }
            if (parameters.length == 0) {
              parameters = new Object[] { 1 };
            } else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            return new DatabaseDataType("[nchar]", parameters);
        }
        return super.toDatabaseDataType(database);
    }

}