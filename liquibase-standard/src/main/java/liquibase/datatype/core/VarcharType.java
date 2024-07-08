package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Locale;

@DataTypeInfo(name="varchar", aliases = {"java.sql.Types.VARCHAR", "java.lang.String", "varchar2", "character varying"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT)
public class VarcharType extends CharType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if ((database instanceof OracleDatabase) || ((database instanceof HsqlDatabase) && ((HsqlDatabase) database)
            .isUsingOracleSyntax())) {
            return new DatabaseDataType("VARCHAR2", getParameters());
        }

        if ((database instanceof InformixDatabase) && (getSize() > 255)) {
            return new DatabaseDataType("LVARCHAR", getParameters());
        }

        if (database instanceof MSSQLDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length > 0) {
                // MSSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                final String param1 = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                parameters[0] =  param1;
                if (!param1.matches("\\d+") || (new BigInteger(param1).compareTo(BigInteger.valueOf(8000L)) > 0)) {

                    DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"), "MAX");
                    type.addAdditionalInformation(getAdditionalInformation());
                    return type;
                }
            }
            if (parameters.length == 0) {
                parameters = new Object[] { 1 };
            } else if (parameters.length > 1) {
                parameters = Arrays.copyOfRange(parameters, 0, 1);
            }
            DatabaseDataType type = new DatabaseDataType(database.escapeDataTypeName("varchar"), parameters);
            type.addAdditionalInformation(getAdditionalInformation());
            return type;
        } else if (database instanceof PostgresDatabase) {
            final Object[] parameters = getParameters();
            if ((parameters != null) && (parameters.length == 1)) {
                // PostgreSQL only supports (n) syntax but not (n CHAR) syntax, so we need to remove CHAR.
                final String parameter = parameters[0].toString().replaceFirst("(?<=\\d+)\\s*(?i)CHAR$", "");
                // PostgreSQL uses max. length implicitly if no length is provided, so we can spare it.
                if ("2147483647".equals(parameter)) {
                    DatabaseDataType type = new DatabaseDataType("CHARACTER");
                    type.addAdditionalInformation("VARYING");
                    return type;
                }
                parameters[0] = parameter;
                DatabaseDataType type = new DatabaseDataType(this.getName().toUpperCase(Locale.US), parameters);
                type.addAdditionalInformation(this.getAdditionalInformation());
                return type;
            }
        }

        return super.toDatabaseDataType(database);
    }


}
