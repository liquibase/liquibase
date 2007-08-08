package liquibase.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import liquibase.migrator.change.DropForeignKeyConstraintChange;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.UnsupportedChangeException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.UnsupportedChangeException;

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

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:mysql")) {
            return "com.mysql.jdbc.Driver";
        }
        return null;
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

    public boolean supportsSequences() {
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


    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public String getConcatSql(String ... values) {
        StringBuffer returnString = new StringBuffer();
        returnString.append("CONCAT_WS(");
        for (String value : values) {
            returnString.append(value).append(", ");
        }

        return returnString.toString().replaceFirst(", $", ")");
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
