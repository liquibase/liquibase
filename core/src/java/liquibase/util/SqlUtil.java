package liquibase.util;

import liquibase.database.Database;
import liquibase.database.MSSQLDatabase;

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
}
