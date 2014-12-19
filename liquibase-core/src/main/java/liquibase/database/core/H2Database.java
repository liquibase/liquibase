package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.DatabaseConnection;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.statement.DatabaseFunction;
import liquibase.util.ISODateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;
import java.util.Arrays;
import java.util.List;

public class H2Database extends AbstractJdbcDatabase {

    private static String START_CONCAT = "CONCAT(";
    private static String END_CONCAT = ")";
    private static String SEP_CONCAT = ", ";

    public H2Database() {
        super.unquotedObjectsAreUppercased=true;
        super.setCurrentDateTimeFunction("NOW()");
        this.dateFunctions.add(new DatabaseFunction("CURRENT_TIMESTAMP()"));
        super.sequenceNextValueFunction = "NEXTVAL('%s')";
        super.sequenceCurrentValueFunction = "CURRVAL('%s')";
    }

    @Override
    public String getShortName() {
        return "h2";
    }

    @Override
    public Integer getDefaultPort() {
        return 8082;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "H2";
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:h2")) {
            return "org.h2.Driver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "H2".equals(conn.getDatabaseProductName());
    }

    //    public void dropDatabaseObjects(String schema) throws DatabaseException {
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
//            throw new DatabaseException(e);
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
    public String getViewDefinition(CatalogAndSchema schema, String name) throws DatabaseException {
        String definition = super.getViewDefinition(schema, name);
        if (!definition.startsWith("SELECT")) {
            definition = definition.replaceFirst(".*?\\n", ""); //some h2 versions return "create view....as\nselect
        }

        definition = definition.replaceFirst("/\\*.*",""); //sometimes includes comments at the end
        return definition;
    }


    @Override
    public Date parseDate(String dateAsString) throws DateParseException {
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
    public boolean isSafeToRunUpdate() throws DatabaseException {
        if (getConnection() == null) {
            return true;
        }
        String url = getConnection().getURL();
        boolean isLocalURL = (
                super.isSafeToRunUpdate()
                        || url.startsWith("jdbc:h2:file:")
                        || url.startsWith("jdbc:h2:mem:")
                        || url.startsWith("jdbc:h2:zip:")
                        || url.startsWith("jdbc:h2:~")
        );
        return isLocalURL;
    }

//    @Override
//    public String convertRequestedSchemaToSchema(String requestedSchema) throws DatabaseException {
//        return super.convertRequestedSchemaToSchema(requestedSchema).toLowerCase();
//    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    @Override
    protected String getConnectionSchemaName() {
        return "PUBLIC";
    }

    @Override
    public String getConcatSql(String... values) {
        if (values == null) {
            return null;
        }

        return getConcatSql(Arrays.asList(values));
    }

    /**
     * Recursive way of building CONCAT instruction
     *
     * @param values a non null List of String
     * @return a String containing the CONCAT instruction with all elements, or only a value if there is only one element in the list
     */
    private String getConcatSql(List<String> values) {
        if (values.size() == 1) {
            return values.get(0);
        } else {
            return START_CONCAT + values.get(0) + SEP_CONCAT + getConcatSql(values.subList(1, values.size())) + END_CONCAT;
        }
    }

    @Override
    public String getDateLiteral(String isoDate) {
        String returnString = isoDate;
        try {
            if (isDateTime(isoDate)) {
                ISODateFormat isoTimestampFormat = new ISODateFormat();
                DateFormat dbTimestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                returnString = dbTimestampFormat.format(isoTimestampFormat.parse(isoDate));
            }
        } catch (ParseException e) {
            throw new RuntimeException("Unexpected date format: " + isoDate, e);
        }
        return "'" + returnString + "'";
    }



    @Override
    public boolean isReservedWord(String objectName) {
        return keywords.contains(objectName.toUpperCase());
    }

    private static List keywords = Arrays.asList(
            "CROSS",
            "CURRENT_DATE",
            "CURRENT_TIME",
            "CURRENT_TIMESTAMP",
            "DISTINCT",
            "EXCEPT",
            "EXISTS",
            "FALSE",
            "FOR",
            "FROM",
            "FULL",
            "GROUP",
            "HAVING",
            "INNER",
            "INTERSECT",
            "IS",
            "JOIN",
            "LIKE",
            "LIMIT",
            "MINUS",
            "NATURAL",
            "NOT",
            "NULL",
            "ON",
            "ORDER",
            "PRIMARY",
            "ROWNUM",
            "SELECT",
            "SYSDATE",
            "SYSTIME",
            "SYSTIMESTAMP",
            "TODAY",
            "TRUE",
            "UNION",
            "UNIQUE",
            "WHERE");

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    @Override
    protected String getAutoIncrementClause() {
        return "AUTO_INCREMENT";
    }

    @Override
    protected String getAutoIncrementStartWithClause() {
	return "%d";
    }

    @Override
    protected String getAutoIncrementByClause() {
	return "%d";
    }

    @Override
    public boolean createsIndexesForForeignKeys() {
        return true;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return true;
    }
}
