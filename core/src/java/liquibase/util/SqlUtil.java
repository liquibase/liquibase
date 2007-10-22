package liquibase.util;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;

import java.util.List;
import java.util.Arrays;
import java.sql.Types;

public class SqlUtil {
    /**
     * Escapes the table name in a database-dependent manner so reserved words can be used as a table name (i.e. "order").
     * Currently only escapes MS-SQL because other DBMSs store table names case-sensitively when escaping is used which
     * could confuse end-users.
     */
    public static String escapeTableName(String tableName, Database database) {
        if (database instanceof MSSQLDatabase) {
            return "["+tableName+"]";
        } else {
            return tableName;
        }
    }

    public static boolean isNumeric(int dataType) {
        List<Integer> numericTypes = Arrays.asList(
                Types.BIGINT,
                Types.BIT,
                Types.INTEGER,
                Types.SMALLINT,
                Types.TINYINT,
                Types.DECIMAL,
                Types.DOUBLE,
                Types.FLOAT,
                Types.NUMERIC,
                Types.REAL
        );

        return numericTypes.contains(dataType);
    }
}
