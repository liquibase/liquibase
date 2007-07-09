package liquibase.database;

import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;

public class SybaseDatabase extends MSSQLDatabase {
    public String getProductName() {
        return "Sybase SQL Server";
    }

    public String getTypeName() {
        return "sybase";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        }
        return null;
    }


    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return "Sybase SQL Server".equals(getDatabaseProductName());
    }
}
