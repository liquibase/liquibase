package liquibase.database.core;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.DateParseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Index;
import liquibase.structure.core.Schema;
import liquibase.util.JdbcUtils;
import liquibase.util.StringUtil;

import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public abstract class AbstractDb2Database extends AbstractJdbcDatabase {

    public AbstractDb2Database() {
        super.setCurrentDateTimeFunction("CURRENT TIMESTAMP");
        super.sequenceNextValueFunction = "NEXT VALUE FOR %s";
        super.sequenceCurrentValueFunction = "PREVIOUS VALUE FOR %s";
        super.unquotedObjectsAreUppercased=true;
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
                    this.defaultSchemaName = StringUtil.trimToNull(result);
                } else {
                    this.defaultSchemaName = StringUtil.trimToNull(super.getDefaultSchemaName());
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
            } else if (dateAsString.indexOf('.') > 0 && dateAsString.indexOf('-') > 0) {
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
            tableName = "DbChgLog".toUpperCase(Locale.US);
        } else if (tableName.equals(getDatabaseChangeLogLockTableName())) {
            tableName = "DbChgLogLock".toUpperCase(Locale.US);
        }

        String pkName = super.generatePrimaryKeyName(tableName);
        if (pkName.length() > 18) {
            pkName = pkName.substring(0, 17);
        }
        return pkName;
    }

    @Override
    public CatalogAndSchema getSchemaFromJdbcInfo(String rawCatalogName, String rawSchemaName) {
        if (rawCatalogName != null && rawSchemaName == null) {
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

    @Override
    public boolean isSystemObject(DatabaseObject example) {
        if (example instanceof Index && example.getName() != null && example.getName().matches("SQL\\d+")) {
            return true;
        }
        return super.isSystemObject(example);
    }

    @Override
    public String correctObjectName(final String objectName, final Class<? extends DatabaseObject> objectType) {
        return objectName;
    }

    protected boolean mustQuoteObjectName(String objectName, Class<? extends DatabaseObject> objectType) {
        if (objectType.isAssignableFrom(Schema.class) || objectType.isAssignableFrom(Catalog.class)) {
            return true;
        }
        return super.mustQuoteObjectName(objectName, objectType);
    }

    /**
     * DB2 database are not case sensitive. However schemas and catalogs are case sensitive
     */
    @Override
    public CatalogAndSchema.CatalogAndSchemaCase getSchemaAndCatalogCase() {
        return CatalogAndSchema.CatalogAndSchemaCase.ORIGINAL_CASE;
    }
}