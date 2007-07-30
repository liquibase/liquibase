package liquibase.database;

import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;

public class H2Database extends HsqlDatabase {
    public String getProductName() {
        return "H2 Database";
    }

    public String getTypeName() {
        return "h2";
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:h2")) {
            return "org.h2.Driver";
        }
        return null;
    }


    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return "H2".equals(getDatabaseProductName(conn));
    }

    public String createFindSequencesSQL() throws JDBCException {
        return "SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '"+getSchemaName()+"' AND IS_GENERATED=FALSE";
    }
}
