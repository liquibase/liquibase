package liquibase.datatype.core;

import liquibase.database.Database;
import liquibase.database.core.*;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

import java.math.BigInteger;
import java.util.Arrays;

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
                String param1 = parameters[0].toString();
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
            if ((getParameters() != null) && (getParameters().length == 1) && "2147483647".equals(getParameters()[0]
                .toString())) {
                DatabaseDataType type = new DatabaseDataType("CHARACTER");
                type.addAdditionalInformation("VARYING");
                return type;
            }
        }

        return super.toDatabaseDataType(database);
    }


}
