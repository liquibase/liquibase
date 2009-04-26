package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
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

	private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
	private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";
	
	private static final DataType UUID_TYPE = new DataType("VARCHAR(36)", false);
	private static final DataType CURRENCY_TYPE = new DataType("MONEY", true);
	private static final DataType CLOB_TYPE = new DataType("CLOB", false);
	private static final DataType BOOLEAN_TYPE = new DataType("BOOLEAN", false);
	private static final DataType BLOB_TYPE = new DataType("BLOB", false);
	private static final DataType BIGINT_TYPE = new DataType("INT8", false);
	private static final DataType TIME_TYPE = new DataType("INTERVAL " + INTERVAL_FIELD_QUALIFIER, false);
	private static final DataType DATETIME_TYPE = new DataType("DATETIME " + DATETIME_FIELD_QUALIFIER, false);

	private static final String PRODUCT_NAME = "Informix Dynamic Server";
	
	private Set<String> systemTablesAndViews = new HashSet<String>();

    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
    
	private static final Pattern INTEGER_PATTERN = Pattern.compile("^(int(eger)?)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern INTEGER8_PATTERN =  Pattern.compile("^(int(eger)?8)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SERIAL_PATTERN = Pattern.compile("^(serial)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SERIAL8_PATTERN = Pattern.compile("^(serial8)(\\s*\\(\\d+\\)|)$", Pattern.CASE_INSENSITIVE);
	
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
    public void setConnection(Connection connection) {
        super.setConnection(connection);
        try {
        	/* 
        	 * TODO Maybe there is a better place for this.
        	 * For each session this statement has to be executed,
        	 * to allow newlines in quoted strings
        	 */
			connection.createStatement().execute("EXECUTE PROCEDURE IFX_ALLOW_NEWLINE('T');");
		} catch (SQLException e) {
			new RuntimeException("Could not allow newline characters in quoted strings with IFX_ALLOW_NEWLINE");
		}
    }
	
	@Override
	public DataType getBigIntType() {
		return BIGINT_TYPE; 
	}

	public DataType getBlobType() {
		return BLOB_TYPE;
	}

	public DataType getBooleanType() {
		return BOOLEAN_TYPE;
	}

	public DataType getClobType() {
		return CLOB_TYPE;
	}

	public DataType getCurrencyType() {
		return CURRENCY_TYPE;
	}

	public String getCurrentDateTimeFunction() {
		return "CURRENT " + DATETIME_FIELD_QUALIFIER;
	}

	public DataType getDateTimeType() {
		return DATETIME_TYPE;
	}
	
	@Override
	public DataType getTimeType() {
		return TIME_TYPE;
	}
	
	public String getDefaultDriver(String url) {
		if (url.startsWith("jdbc:informix-sqli")) {
			return "com.informix.jdbc.IfxDriver";
		}
		return null;
	}

	public String getProductName() {
		return PRODUCT_NAME;
	}

	public String getTypeName() {
		return "informix";
	}

	public DataType getUUIDType() {
		return UUID_TYPE;
	}

	public boolean isCorrectDatabaseImplementation(Connection conn)
			throws JDBCException {
		return PRODUCT_NAME.equals(getDatabaseProductName(conn));
	}

	public boolean supportsInitiallyDeferrableColumns() {
		// TODO dont know if this correct
		return true;
	}

	public boolean supportsTablespaces() {
		return true;
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
	public SqlStatement getViewDefinitionSql(String schemaName, String name)
			throws JDBCException {
		// TODO owner is schemaName ?
		// view definition is distributed over multiple rows, each 64 chars
		return new RawSqlStatement("select v.viewtext from sysviews v, systables t where t.tabname = '"
				+ name + "' and v.tabid = t.tabid and t.tabtype = 'V' order by v.seqno");
	}

	@Override
	public String getAutoIncrementClause() {
		return "";
	}

	@Override
	public String getColumnType(String columnType, Boolean autoIncrement) {
		String type = super.getColumnType(columnType, autoIncrement);
        if (autoIncrement != null && autoIncrement) {
            if (isSerial(type)) {
                return "SERIAL";
            } else if (isSerial8(type)) {
                return "SERIAL8";
            } else {
            	throw new IllegalArgumentException("Unknown autoincrement type: " + columnType);
            }
        }
        return type;
	}

	private boolean isSerial(String type) {
		return INTEGER_PATTERN.matcher(type).matches()
			|| SERIAL_PATTERN.matcher(type).matches();
	}

	private boolean isSerial8(String type) {
		return INTEGER8_PATTERN.matcher(type).matches()
			|| SERIAL8_PATTERN.matcher(type).matches()
			|| "BIGINT".equals(type.toUpperCase());
	}
	
	@Override
	public SqlStatement createFindSequencesSQL(String schema)
			throws JDBCException {
		return new RawSqlStatement(
				"SELECT tabname FROM systables t, syssequences s WHERE s.tabid = t.tabid");
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
        } else if (isDateOnly(isoDate)){
        	return super.getDateLiteral(isoDate);
        } else {
            return "DATETIME (" + super.getDateLiteral(isoDate).replaceAll("'", "") + ") " + DATETIME_FIELD_QUALIFIER;
        }
    }
	
	@Override
	public boolean supportsRestrictForeignKeys() {
		// TODO dont know if this correct
		return false;
	}
	
	
	@Override
	public boolean supportsSchemas() {
		return true;
	}

}
