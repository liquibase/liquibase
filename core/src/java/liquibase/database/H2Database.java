package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

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

    public SqlStatement createFindSequencesSQL() throws JDBCException {
        return new RawSqlStatement("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + getSchemaName() + "' AND IS_GENERATED=FALSE");
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

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String name) throws JDBCException {
        return super.getViewDefinition(name).replaceFirst(".*?\n", ""); //h2 returns "create view....as\nselect
    }

    protected SqlStatement getViewDefinitionSql(String name) throws JDBCException {
        return new RawSqlStatement("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '" + name + "'");
    }

    public String translateDefaultValue(String defaultValue) {
        if (StringUtils.trimToEmpty(defaultValue).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
            return null;
        }
        return defaultValue;
    }

}
