package liquibase.datatype.core;

import java.math.BigInteger;
import java.util.Arrays;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.exception.DatabaseException;

@DataTypeInfo(name="nvarchar", aliases = {"java.sql.Types.NVARCHAR", "nvarchar2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class NVarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof HsqlDatabase
                || database instanceof PostgresDatabase
                || database instanceof DerbyDatabase) {

            return new DatabaseDataType("VARCHAR", getParameters());
        }
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("NVARCHAR2", getParameters());
        }
        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length > 0) {
                String param1 = parameters[0].toString();
                if (!param1.matches("\\d+")
                        || new BigInteger(param1).compareTo(BigInteger.valueOf(4000L)) > 0) {

                    try {
                        if (database.getDatabaseMajorVersion() <= 8) { //2000 or earlier
                            return new DatabaseDataType("[nvarchar]", "4000");
                        }
                    } catch (DatabaseException ignore) { } //assuming it is a newer version

                    return new DatabaseDataType("[nvarchar]", "MAX");
                }
            }
            if (parameters.length == 0) {
                parameters = new Object[] { 1 };
            } else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            return new DatabaseDataType("[nvarchar]", parameters);
        }
        return super.toDatabaseDataType(database);
    }

}