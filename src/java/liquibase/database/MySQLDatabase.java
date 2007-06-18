package liquibase.database;

import liquibase.migrator.exception.UnsupportedChangeException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.change.DropForeignKeyConstraintChange;

import java.sql.*;

/**
 * Encapsulates MySQL database support.
 */
public class MySQLDatabase extends AbstractDatabase {
    public static final String PRODUCT_NAME = "MySQL";

    public String getProductName() {
        return "MySQL";
    }

    public String getTypeName() {
        return "mysql";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
    }

    protected String getBooleanType() {
        return "TINYINT(1)";
    }

    protected String getCurrencyType() {
        return "DECIMAL";
    }

    protected String getUUIDType() {
        return null;
    }

    protected String getClobType() {
        return "TEXT";
    }

    protected String getBlobType() {
        return "BLOB";
    }

    protected String getDateTimeType() {
        return "DATETIME";
    }

    protected boolean supportsSequences() {
        return false;
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return "NOW()";
    }

    public String getLineComment() {
        return "==";
    }

    public String getDropTableSQL(String tableName) {
        return "DROP TABLE " + tableName;
    }

    protected void dropForeignKeys(Connection conn) throws JDBCException {
        Statement dropStatement = null;
        PreparedStatement fkStatement = null;
        ResultSet rs = null;
        try {
            dropStatement = conn.createStatement();

            fkStatement = conn.prepareStatement("select TABLE_NAME, CONSTRAINT_NAME from INFORMATION_SCHEMA.TABLE_CONSTRAINTS where CONSTRAINT_TYPE='FOREIGN KEY' AND TABLE_SCHEMA=?");
            String schemaNameWithoutHost = getSchemaName().replaceAll("\\@.*", "");
            fkStatement.setString(1, schemaNameWithoutHost);
            rs = fkStatement.executeQuery();
            while (rs.next()) {
                DropForeignKeyConstraintChange dropFK = new DropForeignKeyConstraintChange();
                dropFK.setBaseTableName(rs.getString("TABLE_NAME"));
                dropFK.setConstraintName(rs.getString("CONSTRAINT_NAME"));

                try {
                    dropStatement.execute(dropFK.generateStatements(this)[0]);
                } catch (UnsupportedChangeException e) {
                    throw new JDBCException(e.getMessage());
                }
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            try {
                if (dropStatement != null) {
                    dropStatement.close();
                }
                if (fkStatement != null) {
                    fkStatement.close();
                }
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
        }

    }


}
