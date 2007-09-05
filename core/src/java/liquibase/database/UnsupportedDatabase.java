package liquibase.database;

import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class UnsupportedDatabase extends AbstractDatabase {
    private String dateTimeType;


    public void setConnection(Connection conn) {
        super.setConnection(conn);
        dateTimeType = findDateTypeType();
        if (currentDateTimeFunction == null) {
            currentDateTimeFunction = findCurrentDateTimeFunction();
        }
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    public String getCatalogName() throws JDBCException {
        return null;
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    public String getSchemaName() throws JDBCException {
        return null;
    }

    protected String getBooleanType() {
        return "INT";
    }


    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    protected String getCurrencyType() {
        return "DECIMAL";
    }

    protected String getUUIDType() {
        return "VARCHAR(36)";
    }

    protected String getClobType() {
        return "CLOB";
    }

    protected String getBlobType() {
        return "BLOB";
    }

    protected String getDateTimeType() {
        return dateTimeType;
    }

    private String findDateTypeType() {
        ResultSet typeInfo = null;
        try {
            typeInfo = getConnection().getMetaData().getTypeInfo();
            while (typeInfo.next()) {
                if (typeInfo.getInt("DATA_TYPE") == Types.TIMESTAMP) {
                    return typeInfo.getString("TYPE_NAME");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if (typeInfo != null) {
                try {
                    typeInfo.close();
                } catch (SQLException e) {
                    
                }
            }
        }
        return "DATETIME";
    }

    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return false;
    }

    public String getDefaultDriver(String url) {
        return null;
    }    

    public String getProductName() {
        return "Unsupported Database ("+getDatabaseProductName()+")";
    }

    public String getTypeName() {
        return "unknown";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

    private String findCurrentDateTimeFunction() {
        try {
            String nowFunction = null;
            String dateFunction = null;
            String dateTimeFunction = null;
            String timeStampFunction = null;

            String[] timeDateFunctions = getConnection().getMetaData().getTimeDateFunctions().split(",");
            for (String functionName : timeDateFunctions) {
                String function = functionName.trim().toUpperCase();
                if (function.endsWith("TIMESTAMP")) {
                    timeStampFunction = functionName.trim();
                }
                if (function.endsWith("DATETIME")) {
                    dateTimeFunction = functionName.trim();
                }
                if (function.endsWith("DATE")) {
                    dateFunction = functionName.trim();
                }
                if ("NOW".equals(function)) {
                    nowFunction = functionName.trim();
                }
            }

            if (nowFunction != null) {
                return "{fn "+nowFunction+"()"+"}";
            } else if (timeStampFunction != null) {
                return "{fn "+timeStampFunction+"()"+"}";
            } else if (dateTimeFunction != null) {
                return "{fn "+dateTimeFunction+"()"+"}";
            } else if (dateFunction != null) {
                return "{fn "+dateFunction+"()"+"}";
            } else {
                return "NOW()";
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    protected boolean canCreateChangeLogTable() throws JDBCException {
        //check index size.  Many drivers just return 0, so it's not a great test
        int maxIndexLength;
        try {
            maxIndexLength = getConnection().getMetaData().getMaxIndexLength();

            return maxIndexLength == 0
                    || maxIndexLength >= 150 + 150 + 255 //id + author + filename length 
                    && super.canCreateChangeLogTable();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }

    @Override
    protected void dropSequences(DatabaseConnection conn) throws JDBCException {
    }

    public boolean supportsTablespaces() {
        return false;
    }
}
