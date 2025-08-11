package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.ExecutorService;
import liquibase.statement.core.GetViewDefinitionStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class InformixDatabase extends AbstractJdbcDatabase {

    private static final String PRODUCT_NAME = "Informix Dynamic Server"; // product name returned by Informix driver
    private static final String PRODUCT_NAME_DB2JCC_PREFIX = "IDS"; // prefix of the product name (e.g. "IDS/UNIX64") returned by IBM DB2 Universal JDBC (jcc) driver.
    private static final String TIME_FIELD_QUALIFIER = "HOUR TO FRACTION(5)";
    private static final String DATETIME_FIELD_QUALIFIER = "YEAR TO FRACTION(5)";

    private final Set<String> systemTablesAndViews = new HashSet<>();

	private static final String CREATE_VIEW_AS_REGEX ="^CREATE\\s+.*?VIEW\\s+.*?AS\\s+";
    private static final Pattern CREATE_VIEW_AS_PATTERN = Pattern.compile(CREATE_VIEW_AS_REGEX,
    		Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    public InformixDatabase() {
        super.setCurrentDateTimeFunction("CURRENT " + DATETIME_FIELD_QUALIFIER);
        super.sequenceNextValueFunction = "%s.NEXTVAL";
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
        super.sequenceNextValueFunction = "%s.NEXTVAL";
        super.sequenceCurrentValueFunction = "%s.CURRVAL";
    }

    @Override
    protected Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Informix";
    }

    @Override
    public Integer getDefaultPort() {
        return 1526;
    }

    @Override
    public void setConnection(final DatabaseConnection connection) {
        // TODO Verify connection requirement: DB_LOCALE is a Unicode locale
        // TODO Verify connection requirement: GL_DATE is set to GL_DATE=%iY-%m-%d
        super.setConnection(connection);
        if (!(connection instanceof OfflineConnection)) {
            try {
                /*
                 * TODO Maybe there is a better place for this.
                 * For each session this statement has to be executed,
                 * to allow newlines in quoted strings
                 */
                Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).execute(new RawParameterizedSqlStatement("EXECUTE PROCEDURE IFX_ALLOW_NEWLINE('T');"));
            } catch (Exception e) {
                throw new UnexpectedLiquibaseException("Could not allow newline characters in quoted strings with IFX_ALLOW_NEWLINE", e);
            }
        }
    }

    @Override
    public String getDefaultDriver(final String url) {
        if (url.startsWith("jdbc:informix-sqli")) {
            return "com.informix.jdbc.IfxDriver";
        }
        return null;
    }

    @Override
    public String getShortName() {
        return "informix";
    }

    @Override
    public boolean isCorrectDatabaseImplementation(final DatabaseConnection conn)
            throws DatabaseException {
        boolean correct = false;
        String name = conn.getDatabaseProductName();
        if (name != null && (name.equals(PRODUCT_NAME) || name.startsWith(PRODUCT_NAME_DB2JCC_PREFIX))) {
            correct = true;
        }
        return correct;
    }

    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return false;
    }

    /*
     * Informix calls them Dbspaces
     */
    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, final String viewName) throws DatabaseException {
        schema = schema.customize(this);
        List<Map<String, ?>> retList = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForList(new GetViewDefinitionStatement(schema.getCatalogName(), schema.getSchemaName(), viewName));
        // building the view definition from the multiple rows
        StringBuilder sb = new StringBuilder();
        for (Map rowMap : retList) {
            String s = (String) rowMap.get("VIEWTEXT");
            sb.append(s);
        }
        return CREATE_VIEW_AS_PATTERN.matcher(sb.toString()).replaceFirst("");
    }

    @Override
    public String getAutoIncrementClause(final BigInteger startWith, final BigInteger incrementBy, final String generationType, final Boolean defaultOnNull) {
        return "";
    }


    @Override
    public String getDateLiteral(final String isoDate) {
        if (isTimeOnly(isoDate)) {
            return "DATETIME (" + super.getDateLiteral(isoDate).replace("'", "") + ") " + TIME_FIELD_QUALIFIER;
        } else if (isDateOnly(isoDate)) {
            return super.getDateLiteral(isoDate);
        } else {
            return "DATETIME (" + super.getDateLiteral(isoDate).replace("'", "") + ") " + DATETIME_FIELD_QUALIFIER;
        }
    }

    @Override
    public boolean supportsRestrictForeignKeys() {
        // TODO dont know if this correct
        return false;
    }


    @Override
    public String escapeObjectName(final String catalogName, final String schemaName, final String objectName, final Class<? extends DatabaseObject> objectType) {
        String name = super.escapeObjectName(catalogName, schemaName, objectName, objectType);
        if (name == null) {
            return null;
        }
        if (name.matches(".*\\..*\\..*")) {
            name = name.replaceFirst("\\.", ":"); //informix uses : to separate catalog and schema. Like "catalog:schema.table"
        }
        return name;
    }

    @Override
    public String getSystemSchema() {
        return "informix";
    }

    @Override
    public String quoteObject(String objectName, Class<? extends DatabaseObject> objectType) {
        return objectName;
    }

    @Override
    protected String getConnectionSchemaName() {
        if ((getConnection() == null) || (getConnection() instanceof OfflineConnection)) {
            return null;
        }
        try {
            String schemaName = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", this).queryForObject(new RawParameterizedSqlStatement("select username from sysmaster:informix.syssessions where sid = dbinfo('sessionid')"), String.class);
            if (schemaName != null) {
                return schemaName.trim();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).info("Error getting connection schema", e);
        }
        return null;
    }

    @Override
    public boolean supportsCatalogInObjectName(final Class<? extends DatabaseObject> type) {
        return true;
    }

    @Override
    public boolean supportsCreateIfNotExists(Class<? extends DatabaseObject> type) {
        return type.isAssignableFrom(Table.class);
    }
}
