package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtils;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class DB2Database extends AbstractJdbcDatabase {

    private static final Set<String> systemTablesAndViews;

    static {
      systemTablesAndViews = new HashSet<String>();
      systemTablesAndViews.add("SYSCHKCST");
      systemTablesAndViews.add("SYSCST");
      systemTablesAndViews.add("SYSCSTCOL");
      systemTablesAndViews.add("SYSCSTDEP");
      systemTablesAndViews.add("SYSKEYCST");
      systemTablesAndViews.add("SYSREFCST");
    }

    private DataServerType dataServerType;

    public DB2Database() {
        super.setCurrentDateTimeFunction("CURRENT TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "PREVIOUS VALUE FOR %s";
        super.unquotedObjectsAreUppercased=true;
    }

    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return conn.getDatabaseProductName().startsWith("DB2");
    }

    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:db2")) {
            return "com.ibm.db2.jcc.DB2Driver";
        }
        return null;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public Integer getDefaultPort() {
        return 446;
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

    @Override
    public boolean supportsCatalogs() {
        return true;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "DB2";
    }

    @Override
    public String getShortName() {
        return "db2";
    }

    @Override
    public String getDefaultCatalogName() {

        if (defaultCatalogName != null) {
            return defaultCatalogName;
        }

        if (defaultSchemaName != null) {
            return defaultSchemaName;
        }


        if (getConnection() == null) {
            return null;
        }
        if (getConnection() instanceof OfflineConnection) {
            return ((OfflineConnection) getConnection()).getSchema();
        }

        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = ((JdbcConnection) getConnection()).createStatement();
            rs = stmt.executeQuery("select current schema from sysibm.sysdummy1");
            if (rs.next()) {
                String result = rs.getString(1);
                if (result != null) {
                    this.defaultSchemaName = StringUtils.trimToNull(result);
                } else {
                    this.defaultSchemaName = StringUtils.trimToNull(super.getDefaultSchemaName());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not determine current schema", e);
        } finally {
            JdbcUtils.close(rs, stmt);
        }

        return defaultSchemaName;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    /**
     * Return an DB2 date literal with the same value as a string formatted using ISO 8601.
     * <p/>
     * Convert an ISO8601 date string to one of the following results:
     * to_date('1995-05-23', 'YYYY-MM-DD')
     * to_date('1995-05-23 09:23:59', 'YYYY-MM-DD HH24:MI:SS')
     * <p/>
     * Implementation restriction:
     * Currently, only the following subsets of ISO8601 are supported:
     * YYYY-MM-DD
     * hh:mm:ss
     * YYYY-MM-DDThh:mm:ss
     */
    @Override
    public String getDateLiteral(String isoDate) {
        String normalLiteral = super.getDateLiteral(isoDate);

        if (isDateOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("DATE(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else if (isTimeOnly(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("TIME(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else if (isDateTime(isoDate)) {
            StringBuffer val = new StringBuffer();
            val.append("TIMESTAMP(");
            val.append(normalLiteral);
            val.append(')');
            return val.toString();
        } else {
            return "UNSUPPORTED:" + isoDate;
        }
    }

    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName) throws DatabaseException {
        schema = schema.customize(this);
        String definition = ExecutorService.getInstance().getExecutor(this).queryForObject(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName), String.class);

        return "FULL_DEFINITION: " + definition;
    }

    @Override
    public java.util.Date parseDate(String dateAsString) throws DateParseException {
        try {
            if (dateAsString.indexOf(' ') > 0) {
                return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateAsString);
            } else if ((dateAsString.indexOf('.') > 0) && (dateAsString.indexOf('-') > 0)) {
                return new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS").parse(dateAsString);

            } else {
                if (dateAsString.indexOf(':') > 0) {
                    return new SimpleDateFormat("HH:mm:ss").parse(dateAsString);
                } else if (dateAsString.indexOf('.') > 0) {
                    return new SimpleDateFormat("HH.mm.ss").parse(dateAsString);
                } else {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(dateAsString);
                }
            }
        } catch (ParseException e) {
            throw new DateParseException(dateAsString);
        }
    }

    @Override
    public String generatePrimaryKeyName(String tableName) {
        if (tableName.equals(getDatabaseChangeLogTableName())) {
            tableName = "DbChgLog".toUpperCase();
        } else if (tableName.equals(getDatabaseChangeLogLockTableName())) {
            tableName = "DbChgLogLock".toUpperCase();
        }

        String pkName = super.generatePrimaryKeyName(tableName);
        if (pkName.length() > 18) {
            pkName = pkName.substring(0, 17);
        }
        return pkName;
    }

    @Override
    public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
        if ((rawCatalogName != null) && (rawSchemaName == null)) {
            rawSchemaName = rawCatalogName;
        }
        return new CatalogAndSchema(rawSchemaName, null).customize(this);
    }

    @Override
    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return null;
    }

    @Override
    public String getJdbcSchemaName(CatalogAndSchema schema) {
        return correctObjectName(schema.getCatalogName(), Catalog.class);
    }

    @Override
    public boolean jdbcCallsCatalogsSchemas() {
        return true;
    }

    /**
     * Determine the DB2 data server type. This replaces the isZOS() and
     * isAS400() methods, which was based on DatabaseMetaData
     * getDatabaseProductName(), which does not work correctly for some DB2
     * types.
     *
     * @see <a href="http://www.ibm.com/support/knowledgecenter/SSEPEK_10.0.0/com.ibm.db2z10.doc.java/src/tpc/imjcc_c0053013.html">ibm.com</a>
     * @return the data server type
     */
    public DataServerType getDataServerType() {
        if (this.dataServerType == null) {
            DatabaseConnection databaseConnection = getConnection();
            if ((databaseConnection != null) && (databaseConnection instanceof JdbcConnection)) {
                try {
                    String databaseProductVersion = databaseConnection.getDatabaseProductVersion();
                    String databaseProductName = databaseConnection.getDatabaseProductName();
                    if (databaseProductVersion.startsWith("SQL")) {
                        this.dataServerType = DataServerType.DB2LUW;
                    } else if (databaseProductVersion.startsWith("QSQ") || databaseProductName.startsWith("DB2 UDB for AS/400")) {
                        this.dataServerType = DataServerType.DB2I;
                    } else if (databaseProductVersion.startsWith("DSN")) {
                        this.dataServerType = DataServerType.DB2Z;
                    }
                } catch (DatabaseException e) {
                    this.dataServerType = DataServerType.DB2LUW;
                }
            } else {
                this.dataServerType = DataServerType.DB2LUW;
            }
        }
        return this.dataServerType;
    }

    public boolean isZOS() {
        return getDataServerType() == DataServerType.DB2Z;
    }

    public boolean isAS400() {
       return getDataServerType() == DataServerType.DB2I;
    }

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if ((example instanceof Index) && (example.getName() != null) && example.getName().matches("SQL\\d+")) {
            return true;
        }
        return super.isSystemObject(example);
    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        // According to
        // https://www.ibm.com/support/knowledgecenter/SSEPGG_9.7.0/com.ibm.db2.luw.sql.ref.doc/doc/r0000859.html
        return 12;
    }

    @Override
    public int getDefaultFractionalDigitsForTimestamp() {
        return 6;
    }

    public enum DataServerType {
        /**
         * DB2 on Linux, Unix and Windows
         */
        DB2LUW,

        /**
         * DB2 on IBM iSeries
         */
        DB2I,

        /**
         * DB2 on IBM zSeries
         */
        DB2Z
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }
}
