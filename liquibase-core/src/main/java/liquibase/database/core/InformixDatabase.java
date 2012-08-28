package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.structure.Schema;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawSqlStatement;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class InformixDatabase extends AbstractDatabase {
	
	private static final String PRODUCT_NAME = "Informix Dynamic Server";
    private static final String INTERVAL_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";

	private Set<String> systemTablesAndViews = new HashSet<String>();

    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile("^CREATE\\s+.*?VIEW\\s+.*?AS\\s+",
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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
	protected Set<String> getSystemViews() {
		return systemTablesAndViews;
	}

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Informix";
    }

    public Integer getDefaultPort() {
        return 1526;
    }

    @Override
    public void setConnection(DatabaseConnection connection) {
        super.setConnection(connection);
        try {
        	/* 
        	 * TODO Maybe there is a better place for this.
        	 * For each session this statement has to be executed,
        	 * to allow newlines in quoted strings
        	 */
			ExecutorService.getInstance().getExecutor(this).execute(new RawSqlStatement("EXECUTE PROCEDURE IFX_ALLOW_NEWLINE('T');"));
		} catch (Exception e) {
			throw new UnexpectedLiquibaseException("Could not allow newline characters in quoted strings with IFX_ALLOW_NEWLINE", e);
		}
    }
	
	public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }

		return "CURRENT " + DATETIME_FIELD_QUALIFIER;
	}

	public String getDefaultDriver(String url) {
		if (url.startsWith("jdbc:informix-sqli")) {
			return "com.informix.jdbc.IfxDriver";
		}
		return null;
	}

	public String getTypeName() {
		return "informix";
	}

	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn)
			throws DatabaseException {
		return PRODUCT_NAME.equals(conn.getDatabaseProductName());
	}

	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	/*
	 * Informix calls them Dbspaces
	 */
	public boolean supportsTablespaces() {
		return true;
	}

	@Override
	public String getViewDefinition(Schema schema, String viewName) throws DatabaseException {
        schema = correctSchema(schema);
		List<Map> retList = ExecutorService.getInstance().getExecutor(this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getName(), viewName));
		// building the view definition from the multiple rows
		StringBuilder sb = new StringBuilder();
		for (Map rowMap : retList) {
			String s = (String) rowMap.get("VIEWTEXT");
			sb.append(s);
		}
		return CREATE_VIEW_AS_PATTERN.matcher(sb.toString()).replaceFirst("");
	}

	@Override
	public String getAutoIncrementClause(BigInteger startWith, BigInteger incrementBy) {
		return "";
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
		return false;
	}
}
