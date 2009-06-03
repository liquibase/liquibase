package liquibase.database;

import java.sql.Connection;

import liquibase.exception.JDBCException;

public class DB2iDatabase extends DB2Database {
    @Override
    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return getDatabaseProductName(conn).startsWith("DB2 UDB for AS/400");
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:as400")) {
            return "com.ibm.as400.access.AS400JDBCDriver";
        }
        return null;
    }

    @Override
    public String getProductName() {
        return "DB2 for IBM i";
    }

    @Override
    public String getTypeName() {
        return "db2i";
    }

    @Override
    public DataType getFloatType() {
        return new DataType("DECIMAL", false);
    }
}
