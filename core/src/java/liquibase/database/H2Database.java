package liquibase.database;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.exception.DateParseException;
import liquibase.exception.JDBCException;
import liquibase.util.StringUtils;

import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    public SqlStatement createFindSequencesSQL(String schema) throws JDBCException {
        return new RawSqlStatement("SELECT SEQUENCE_NAME FROM INFORMATION_SCHEMA.SEQUENCES WHERE SEQUENCE_SCHEMA = '" + convertRequestedSchemaToSchema(schema) + "' AND IS_GENERATED=FALSE");
    }

    @Override
    public String getObjectEscapeCharacter() {
        return "`";
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

    public boolean supportsTablespaces() {
        return false;
    }

    public String getViewDefinition(String schemaName, String name) throws JDBCException {
        return super.getViewDefinition(schemaName, name).replaceFirst(".*?\n", ""); //h2 returns "create view....as\nselect
    }

    public SqlStatement getViewDefinitionSql(String schemaName, String name) throws JDBCException {
        return new RawSqlStatement("SELECT VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_NAME = '" + name + "' AND TABLE_SCHEMA='"+convertRequestedSchemaToSchema(schemaName)+"'");
    }

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

    public String getDateTimeType() {
        return "TIMESTAMP";
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
