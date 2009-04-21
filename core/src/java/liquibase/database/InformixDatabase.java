package liquibase.database;

import java.sql.Connection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import liquibase.database.statement.RawSqlStatement;
import liquibase.database.statement.SqlStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.InformixDatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

public class InformixDatabase extends AbstractDatabase {

    private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION";

    private Set<String> systemTablesAndViews = new HashSet<String>();

    private static Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public InformixDatabase() {
        super();
        systemTablesAndViews.add("systables");
        systemTablesAndViews.add("syscolumns");
        systemTablesAndViews.add("sysindices");
        systemTablesAndViews.add("systabauth");
        systemTablesAndViews.add("syscolauth");
        systemTablesAndViews.add("sysviews");
        systemTablesAndViews.add("sysusers");
        systemTablesAndViews.add("sysdepend");
        systemTablesAndViews.add("syssynonyms");
        systemTablesAndViews.add("syssyntable");
        systemTablesAndViews.add("sysconstraints");
        systemTablesAndViews.add("sysreferences");
        systemTablesAndViews.add("syschecks");
        systemTablesAndViews.add("sysdefaults");
        systemTablesAndViews.add("syscoldepend");
        systemTablesAndViews.add("sysprocedures");
        systemTablesAndViews.add("sysprocbody");
        systemTablesAndViews.add("sysprocplan");
        systemTablesAndViews.add("sysprocauth");
        systemTablesAndViews.add("sysblobs");
        systemTablesAndViews.add("sysopclstr");
        systemTablesAndViews.add("systriggers");
        systemTablesAndViews.add("systrigbody");
        systemTablesAndViews.add("sysdistrib");
        systemTablesAndViews.add("sysfragments");
        systemTablesAndViews.add("sysobjstate");
        systemTablesAndViews.add("sysviolations");
        systemTablesAndViews.add("sysfragauth");
        systemTablesAndViews.add("sysroleauth");
        systemTablesAndViews.add("sysxtdtypes");
        systemTablesAndViews.add("sysattrtypes");
        systemTablesAndViews.add("sysxtddesc");
        systemTablesAndViews.add("sysinherits");
        systemTablesAndViews.add("syscolattribs");
        systemTablesAndViews.add("syslogmap");
        systemTablesAndViews.add("syscasts");
        systemTablesAndViews.add("sysxtdtypeauth");
        systemTablesAndViews.add("sysroutinelangs");
        systemTablesAndViews.add("syslangauth");
        systemTablesAndViews.add("sysams");
        systemTablesAndViews.add("systabamdata");
        systemTablesAndViews.add("sysopclasses");
        systemTablesAndViews.add("syserrors");
        systemTablesAndViews.add("systraceclasses");
        systemTablesAndViews.add("systracemsgs");
        systemTablesAndViews.add("sysaggregates");
        systemTablesAndViews.add("syssequences");
        systemTablesAndViews.add("sysdirectives");
        systemTablesAndViews.add("sysxasourcetypes");
        systemTablesAndViews.add("sysxadatasources");
        systemTablesAndViews.add("sysseclabelcomponents");
        systemTablesAndViews.add("sysseclabelcomponentelements");
        systemTablesAndViews.add("syssecpolicies");
        systemTablesAndViews.add("syssecpolicycomponents");
        systemTablesAndViews.add("syssecpolicyexemptions");
        systemTablesAndViews.add("sysseclabels");
        systemTablesAndViews.add("sysseclabelnames");
        systemTablesAndViews.add("sysseclabelauth");
        systemTablesAndViews.add("syssurrogateauth");
        systemTablesAndViews.add("sysproccolumns");

        systemTablesAndViews.add("sysdomains");
        systemTablesAndViews.add("sysindexes");
    }

    @Override
    public DatabaseSnapshot createDatabaseSnapshot(String schema,
                                                   Set<DiffStatusListener> statusListeners) throws JDBCException {
        return new InformixDatabaseSnapshot(this, statusListeners, schema);
    }

    @Override
    protected Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    @Override
    public DataType getBigIntType() {
        return new DataType("INT8", false);
    }

    public DataType getBlobType() {
        return new DataType("BLOB", false);
    }

    public DataType getBooleanType() {
        return new DataType("BOOLEAN", false);
    }

    public DataType getClobType() {
        return new DataType("CLOB", false);
    }

    public DataType getCurrencyType() {
        return new DataType("DECIMAL(19,4)", true);
    }

    public String getCurrentDateTimeFunction() {
        return "CURRENT " + DATETIME_FIELD_QUALIFIER;
    }

    public DataType getDateTimeType() {
        return new DataType("DATETIME " + DATETIME_FIELD_QUALIFIER, false);
    }

    @Override
    public DataType getTimeType() {
        return new DataType("INTERVAL " + INTERVAL_FIELD_QUALIFIER, false);
    }

    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:informix")) {
            return "com.informix.jdbc.IfxDriver";
        }
        return null;
    }

    public String getProductName() {
        return "Informix Dynamic Server";
    }

    public String getTypeName() {
        return "informix";
    }

    public DataType getUUIDType() {
        return new DataType("VARCHAR(36,0)", false);
    }

    public boolean isCorrectDatabaseImplementation(Connection conn)
            throws JDBCException {
        return getDatabaseProductName(conn).startsWith("Informix");
    }

    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    public boolean supportsTablespaces() {
        return false;
    }

    @Override
    public String getTrueBooleanValue() {
        return "'t'";
    }

    @Override
    public String getFalseBooleanValue() {
        return "'f'";
    }

    @Override
    public String escapeStringForDatabase(String string) {
        return super.escapeStringForDatabase(string).replace('\n',' ');
    }

    public boolean supportsRestrictForeignKeys() {
        return false;
    }



    @Override
    public String getViewDefinition(String schemaName, String viewName)
            throws JDBCException {
        List<Map> retList = this.getJdbcTemplate().queryForList(getViewDefinitionSql(schemaName, viewName));
        // building the view definition from the multiple rows
        StringBuilder sb = new StringBuilder();
        for (Map rowMap : retList) {
            String s = (String) rowMap.get("viewtext");
            sb.append(s);
        }
        return CREATE_VIEW_AS_PATTERN.matcher(sb.toString()).replaceFirst("");
    }

    @Override
    public SqlStatement getViewDefinitionSql(String schemaName, String name) throws JDBCException {
        // view definition is distributed over multiple rows, each 64 chars
        return new RawSqlStatement("select v.viewtext from sysviews v, systables t where t.tabname = '" + name + "' and v.tabid = t.tabid and t.tabtype = 'V' order by v.seqno");
    }

    @Override
    public String getAutoIncrementClause() {
        return "";
    }

    @Override
    public String getColumnType(String columnType, Boolean autoIncrement) {
        String type = super.getColumnType(columnType, autoIncrement);
        if (autoIncrement != null && autoIncrement) {
            if ("integer".equals(type.toLowerCase())) {
                return "serial(1)";
            } else if ("bigint".equals(type.toLowerCase()) || "bigserial".equals(type.toLowerCase())) {
                return "bigserial";
            } else {
                // Unknown integer type, default to "serial(1)"
                return "serial(1)";
            }
        }
        return type;
    }

    @Override
    public SqlStatement createFindSequencesSQL(String schema)
            throws JDBCException {
        return new RawSqlStatement(
                "select tabname from systables t, syssequences s where s.tabid = t.tabid");
    }

    @Override
    public String convertJavaObjectToString(Object value) {
        if (value != null && value instanceof Boolean) {
            if (((Boolean) value)) {
                return getTrueBooleanValue();
            } else {
                return getFalseBooleanValue();
            }
        }
        return super.convertJavaObjectToString(value);
    }

    @Override
    public String getDateLiteral(String isoDate) {
        if (isTimeOnly(isoDate)) {
            return "INTERVAL (" + super.getDateLiteral(isoDate).replaceAll("'", "") + ") " + INTERVAL_FIELD_QUALIFIER;
        } else if (isDateOnly(isoDate)) {
            return super.getDateLiteral(isoDate);
        } else {
            return "DATETIME (" + super.getDateLiteral(isoDate).replaceAll("'", "") + ") " + DATETIME_FIELD_QUALIFIER;
        }
    }

    @Override
    public boolean supportsSchemas() {
        return false;
    }

}
