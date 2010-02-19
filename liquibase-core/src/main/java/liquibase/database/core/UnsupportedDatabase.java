package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

public class UnsupportedDatabase extends AbstractDatabase {

    public int getPriority() {
        return -1;
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        super.setConnection(conn);
        if (currentDateTimeFunction == null) {
            currentDateTimeFunction = findCurrentDateTimeFunction();
        }
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    @Override
    public String getDefaultCatalogName() throws DatabaseException {
        return null;
    }

    /**
     * Always returns null or DATABASECHANGELOG table may not be found.
     */
    @Override
    protected String getDefaultDatabaseSchemaName() throws DatabaseException {
        return null;
    }

    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return false;
    }

    public String getDefaultDriver(String url) {
        return null;
    }    

    public String getTypeName() {
        return "unsupported";
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public String getCurrentDateTimeFunction() {
        return currentDateTimeFunction;
    }

    private String findCurrentDateTimeFunction() {
//todo: reintroduce        try {
//            String nowFunction = null;
//            String dateFunction = null;
//            String dateTimeFunction = null;
//            String timeStampFunction = null;
//
//            String[] timeDateFunctions = getConnection().getMetaData().getTimeDateFunctions().split(",");
//            for (String functionName : timeDateFunctions) {
//                String function = functionName.trim().toUpperCase();
//                if (function.endsWith("TIMESTAMP")) {
//                    timeStampFunction = functionName.trim();
//                }
//                if (function.endsWith("DATETIME")) {
//                    dateTimeFunction = functionName.trim();
//                }
//                if (function.endsWith("DATE")) {
//                    dateFunction = functionName.trim();
//                }
//                if ("NOW".equals(function)) {
//                    nowFunction = functionName.trim();
//                }
//            }
//
//            if (nowFunction != null) {
//                return "{fn "+nowFunction+"()"+"}";
//            } else if (timeStampFunction != null) {
//                return "{fn "+timeStampFunction+"()"+"}";
//            } else if (dateTimeFunction != null) {
//                return "{fn "+dateTimeFunction+"()"+"}";
//            } else if (dateFunction != null) {
//                return "{fn "+dateFunction+"()"+"}";
//            } else {
//                return "CURRENT_TIMESTAMP";
//            }
//
//        } catch (SQLException e) {
//            throw new RuntimeException(e);
//        }
        return "CURRENT_TIMESTAMP";
    }


//todo: reintroduce?    @Override
//    protected boolean canCreateChangeLogTable() throws DatabaseException {
//        //check index size.  Many drivers just return 0, so it's not a great test
//        int maxIndexLength;
//        try {
//            maxIndexLength = getConnection().getMetaData().getMaxIndexLength();
//
//            return maxIndexLength == 0
//                    || maxIndexLength >= 150 + 150 + 255 //id + author + filename length
//                    && super.canCreateChangeLogTable();
//        } catch (SQLException e) {
//            throw new DatabaseException(e);
//        }
//    }

    public boolean supportsTablespaces() {
        return false;
    }
}
