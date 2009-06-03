package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.UnsupportedDatabaseSnapshot;
import liquibase.diff.DiffStatusListener;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Set;

public class UnsupportedDatabase extends AbstractDatabase {
    private String dateTimeType;
    private static final DataType BOOLEAN_TYPE = new DataType("INT", false);
    private static final DataType CURRENCY_TYPE = new DataType("DECIMAL", true);
    private static final DataType UUID_TYPE = new DataType("CHAR(36)", false);
    private static final DataType CLOB_TYPE = new DataType("CLOB", false);
    private static final DataType BLOB_TYPE = new DataType("BLOB", false);


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
    public String getDefaultCatalogName() throws JDBCException {
        return null;
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    protected String getDefaultDatabaseSchemaName() throws JDBCException {
        return null;
    }

    public DataType getBooleanType() {
        return BOOLEAN_TYPE;
    }


    public String getFalseBooleanValue() {
        return "0";
    }

    public String getTrueBooleanValue() {
        return "1";
    }

    public DataType getCurrencyType() {
        return CURRENCY_TYPE;
    }

    public DataType getUUIDType() {
        return UUID_TYPE;
    }

    public DataType getClobType() {
        return CLOB_TYPE;
    }

    public DataType getBlobType() {
        return BLOB_TYPE;
    }

    public DataType getDateTimeType() {
        return new DataType(dateTimeType, false);
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
                    ;
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
        return getDatabaseProductName().toLowerCase().replaceAll("\\W","");
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
                return "CURRENT_TIMESTAMP";
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

    public boolean supportsTablespaces() {
        return false;
    }

    public DatabaseSnapshot createDatabaseSnapshot(String schema, Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new UnsupportedDatabaseSnapshot(this, statusListeners, schema);
    }
}
