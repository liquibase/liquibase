package liquibase.database;

import liquibase.exception.DateParseException;
import liquibase.exception.JDBCException;
import liquibase.statement.RawSqlStatement;
import liquibase.statement.SqlStatement;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class H2Database extends HsqlDatabase {
    private static final DataType DATETIME_TYPE = new DataType("TIMESTAMP", false);

    @Override
    public String getProductName() {
        return "H2 Database";
    }

    @Override
    public String getTypeName() {
        return "h2";
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:h2")) {
            return "org.h2.Driver";
        }
        return null;
    }


    @Override
    public boolean isCorrectDatabaseImplementation(Connection conn) throws JDBCException {
        return "H2".equals(getDatabaseProductName(conn));
    }

    //    public void dropDatabaseObjects(String schema) throws JDBCException {
//        DatabaseConnection conn = getConnection();
//        Statement dropStatement = null;
//        try {
//            dropStatement = conn.createStatement();
//            dropStatement.executeUpdate("DROP ALL OBJECTS");
//            changeLogTableExists = false;
//            changeLogLockTableExists = false;
//            changeLogCreateAttempted = false;
//            changeLogLockCreateAttempted = false;
//        } catch (SQLException e) {
//            throw new JDBCException(e);
//        } finally {
//            try {
//                if (dropStatement != null) {
//                    dropStatement.close();
//                }
//                conn.commit();
//            } catch (SQLException e) {
//                ;
//            }
//        }
//
//    }

    @Override
    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getViewDefinition(String schemaName, String name) throws JDBCException {
        return super.getViewDefinition(schemaName, name).replaceFirst(".*?\n", ""); //h2 returns "create view....as\nselect
    }

    @Override
    public Object convertDatabaseValueToJavaObject(Object defaultValue, int dataType, int columnSize, int decimalDigits) throws ParseException {
        if (defaultValue != null && defaultValue instanceof String) {
            if (StringUtils.trimToEmpty(((String) defaultValue)).startsWith("(NEXT VALUE FOR PUBLIC.SYSTEM_SEQUENCE_")) {
                return null;
            }
            if (StringUtils.trimToNull(((String) defaultValue)) == null) {
                return null;
            }
        }
        return super.convertDatabaseValueToJavaObject(defaultValue, dataType, columnSize, decimalDigits);
    }

    @Override
    protected Date parseDate(String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(' ') > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSSSSS").parse(dateAsString);
            } else {
                if (dateAsString.indexOf(':') > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    @Override
    public DataType getDateTimeType() {
        return DATETIME_TYPE;
    }

    @Override
    public boolean isLocalDatabase() throws JDBCException {
        String url = getConnectionURL();
        boolean isLocalURL = (
                super.isLocalDatabase()
                        || url.startsWith("jdbc:h2:file:")
                        || url.startsWith("jdbc:h2:mem:")
                        || url.startsWith("jdbc:h2:zip:")
                        || url.startsWith("jdbc:h2:~")
        );
        return isLocalURL;
    }

//    @Override
//    public String convertRequestedSchemaToSchema(String requestedSchema) throws JDBCException {
//        return super.convertRequestedSchemaToSchema(requestedSchema).toLowerCase();
//    }

}
