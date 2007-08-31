package liquibase.database;

import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

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


    public void dropDatabaseObjects() throws JDBCException {
        DatabaseConnection conn = getConnection();
        Statement dropStatement = null;
        try {
            dropStatement = conn.createStatement();
            dropStatement.executeUpdate("DROP ALL OBJECTS");
            changeLogTableExists = false;
            changeLogLockTableExists = false;
            changeLogCreateAttempted = false;
            changeLogLockCreateAttempted = false;
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            try {
                if (dropStatement != null) {
                    dropStatement.close();
                }
                conn.commit();
            } catch (SQLException e) {
                ;
            }
        }

    }
}
