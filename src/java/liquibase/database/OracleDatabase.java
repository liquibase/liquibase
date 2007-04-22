package liquibase.database;

import liquibase.migrator.MigrationFailedException;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * This class will handle all the database related tasks for the Oracle database. This class has
 * the methods to generate the statements specific for the oracle database.
 */
public class OracleDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "oracle";

    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    protected boolean supportsSequences() {
        return true;
    }

    protected String getBooleanType() {
        return "NUMBER(1)";
    }

    protected String getCurrencyType() {
       return "NUMBER(15, 2)";
    }

    protected String getUUIDType() {
        return "RAW(16)";
    }

    protected String getClobType() {
        return "CLOB";
    }

    protected String getBlobType() {
        return "BLOB";
    }

    protected String getDateType() {
        return "DATE";
    }

    protected String getDateTimeType() {
        return "TIMESTAMP";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws SQLException{
        return PRODUCT_NAME.equalsIgnoreCase(conn.getMetaData().getDatabaseProductName());
    }

    public String getCurrentDateTimeFunction() {
        return "SYSDATE";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getFalseBooleanValue() {
        return "0";
    }

    protected String getSelectChangeLogLockSQL() {
        return (super.getSelectChangeLogLockSQL()+" for update").toUpperCase();
    }

    public String getRenameColumnSQL(String tableName, String oldColumnName, String newColumnName) {
        return "ALTER TABLE "+tableName+" RENAME COLUMN "+oldColumnName+" TO "+newColumnName;
    }

    public String getDropNullConstraintSQL(String tableName, String columnName) {
        return "ALTER TABLE "+tableName+" MODIFY "+columnName+" NULL";
    }

    public String getAddNullConstraintSQL(String tableName, String columnName, String defaultNullValue) {
        return "ALTER TABLE "+tableName+" MODIFY "+columnName+" NOT NULL";
    }

    public String getDropIndexSQL(String tableName, String indexName) {
        return "DROP INDEX "+indexName;
    }

    protected void dropSequences(Connection conn) throws SQLException, MigrationFailedException {
        ResultSet rs = null;
        Statement selectStatement = null;
        Statement dropStatement = null;
        try {
            selectStatement = conn.createStatement();
            dropStatement = conn.createStatement();
            rs = selectStatement.executeQuery("SELECT SEQUENCE_NAME FROM USER_SEQUENCES");
            while (rs.next()) {
                String sequenceName = rs.getString("SEQUENCE_NAME");
                log.finest("Dropping sequence " + sequenceName);
                String sql = "DROP SEQUENCE " + sequenceName;
                try {
                    dropStatement.executeUpdate(sql);
                } catch (SQLException e) {
                    throw new MigrationFailedException("Error dropping sequence '" + sequenceName + "': " + e.getMessage(), e);
                }
            }
        } finally {
            if (selectStatement != null) {
                selectStatement.close();
            }
            if (dropStatement != null) {
                dropStatement.close();
            }
            if (rs != null) {
                rs.close();
            }
        }
    }

    public String getCreateSequenceSQL(String sequenceName, Integer startValue, Integer incrementBy, Integer minValue, Integer maxValue, Boolean ordered) {
        String sql = super.getCreateSequenceSQL(sequenceName, startValue, incrementBy, minValue, maxValue, ordered);
        if (ordered != null && ordered) {
            sql += " ORDER ";
        }
        return sql;
    }

    public String getAlterSequenceSQL(String sequenceName, Integer startValue, Integer incrementBy, Integer minValue, Integer maxValue, Boolean ordered) {
        String sql = super.getAlterSequenceSQL(sequenceName, incrementBy, minValue, maxValue, ordered);

        if (ordered != null && ordered) {
            sql += " ORDER ";
        }
        return sql;
    }
}
