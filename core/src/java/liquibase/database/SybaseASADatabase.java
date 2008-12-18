/**
 * 
 */
package liquibase.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import liquibase.database.sql.RawSqlStatement;
import liquibase.database.sql.SqlStatement;
import liquibase.database.structure.DatabaseSnapshot;
import liquibase.database.structure.SybaseASADatabaseSnapshot;
import liquibase.diff.DiffStatusListener;
import liquibase.exception.JDBCException;

/**
 * @author otaranenko
 *
 */
public class SybaseASADatabase extends AbstractDatabase {

    public static final String PRODUCT_NAME = "Adaptive Server Anywhere";
    private static final Set<String> systemTablesAndViews;
    static {
    	systemTablesAndViews = new HashSet<String>();
		systemTablesAndViews.add("dummy");
		systemTablesAndViews.add("sysarticle");
		systemTablesAndViews.add("sysarticlecol");
		systemTablesAndViews.add("sysarticlecols");
		systemTablesAndViews.add("sysarticles");
		systemTablesAndViews.add("sysattribute");
		systemTablesAndViews.add("sysattributename");
		systemTablesAndViews.add("syscapabilities");
		systemTablesAndViews.add("syscapability");
		systemTablesAndViews.add("syscapabilityname");
		systemTablesAndViews.add("syscatalog");
		systemTablesAndViews.add("syscolauth");
		systemTablesAndViews.add("syscollation");
		systemTablesAndViews.add("syscollationmappings");
		systemTablesAndViews.add("syscolperm");
		systemTablesAndViews.add("syscolstat");
		systemTablesAndViews.add("syscolstats");
		systemTablesAndViews.add("syscolumn");
		systemTablesAndViews.add("syscolumns");
		systemTablesAndViews.add("sysdomain");
		systemTablesAndViews.add("sysevent");
		systemTablesAndViews.add("syseventtype");
		systemTablesAndViews.add("sysextent");
		systemTablesAndViews.add("sysexternlogins");
		systemTablesAndViews.add("sysfile");
		systemTablesAndViews.add("sysfkcol");
		systemTablesAndViews.add("sysforeignkey");
		systemTablesAndViews.add("sysforeignkeys");
		systemTablesAndViews.add("sysgroup");
		systemTablesAndViews.add("sysgroups");
		systemTablesAndViews.add("sysindex");
		systemTablesAndViews.add("sysindexes");
		systemTablesAndViews.add("sysinfo");
		systemTablesAndViews.add("sysixcol");
		systemTablesAndViews.add("sysjar");
		systemTablesAndViews.add("sysjarcomponent");
		systemTablesAndViews.add("sysjavaclass");
		systemTablesAndViews.add("syslogin");
		systemTablesAndViews.add("sysoptblock");
		systemTablesAndViews.add("sysoption");
		systemTablesAndViews.add("sysoptions");
		systemTablesAndViews.add("sysoptjoinstrategy");
		systemTablesAndViews.add("sysoptorder");
		systemTablesAndViews.add("sysoptorders");
		systemTablesAndViews.add("sysoptplans");
		systemTablesAndViews.add("sysoptquantifier");
		systemTablesAndViews.add("sysoptrequest");
		systemTablesAndViews.add("sysoptrewrite");
		systemTablesAndViews.add("sysoptstat");
		systemTablesAndViews.add("sysoptstrategies");
		systemTablesAndViews.add("sysprocauth");
		systemTablesAndViews.add("sysprocedure");
		systemTablesAndViews.add("sysprocparm");
		systemTablesAndViews.add("sysprocparms");
		systemTablesAndViews.add("sysprocperm");
		systemTablesAndViews.add("syspublication");
		systemTablesAndViews.add("syspublications");
		systemTablesAndViews.add("sysremoteoption");
		systemTablesAndViews.add("sysremoteoptions");
		systemTablesAndViews.add("sysremoteoptiontype");
		systemTablesAndViews.add("sysremotetype");
		systemTablesAndViews.add("sysremotetypes");
		systemTablesAndViews.add("sysremoteuser");
		systemTablesAndViews.add("sysremoteusers");
		systemTablesAndViews.add("sysschedule");
		systemTablesAndViews.add("sysservers");
		systemTablesAndViews.add("syssqlservertype");
		systemTablesAndViews.add("syssubscription");
		systemTablesAndViews.add("syssubscriptions");
		systemTablesAndViews.add("syssync");
		systemTablesAndViews.add("syssyncdefinitions");
		systemTablesAndViews.add("syssyncpublicationdefaults");
		systemTablesAndViews.add("syssyncs");
		systemTablesAndViews.add("syssyncsites");
		systemTablesAndViews.add("syssyncsubscriptions");
		systemTablesAndViews.add("syssynctemplates");
		systemTablesAndViews.add("syssyncusers");
		systemTablesAndViews.add("systabauth");
		systemTablesAndViews.add("systable");
		systemTablesAndViews.add("systableperm");
		systemTablesAndViews.add("systrigger");
		systemTablesAndViews.add("systriggers");
		systemTablesAndViews.add("systypemap");
		systemTablesAndViews.add("sysuserauth");
		systemTablesAndViews.add("sysuserlist");
		systemTablesAndViews.add("sysusermessages");
		systemTablesAndViews.add("sysuseroptions");
		systemTablesAndViews.add("sysuserperm");
		systemTablesAndViews.add("sysuserperms");
		systemTablesAndViews.add("sysusertype");
		systemTablesAndViews.add("sysviews");
    }
	
	/**
	 * 
	 */
	public SybaseASADatabase() {
		super();
	}

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#createDatabaseSnapshot(java.lang.String, java.util.Set)
	 */
	@Override
	public DatabaseSnapshot createDatabaseSnapshot(String schema,
			Set<DiffStatusListener> statusListeners) throws JDBCException {
		
		return new SybaseASADatabaseSnapshot(this, statusListeners, schema);
	}

	@Override
	public String escapeIndexName(String schema, String indexName) {
		return escapeName(indexName);
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getBlobType()
	 */
	public String getBlobType() {
		
		return "LONG BINARY";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getBooleanType()
	 */
	public String getBooleanType() {
		
		return "BIT";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getClobType()
	 */
	public String getClobType() {
		return "LONG VARCHAR";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getCurrencyType()
	 */
	public String getCurrencyType() {
		return "MONEY";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getCurrentDateTimeFunction()
	 */
	public String getCurrentDateTimeFunction() {
		return "now()";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getDateTimeType()
	 */
	public String getDateTimeType() {
		return "DATETIME";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
	 */
	public String getDefaultDriver(String url) {
		return "com.sybase.jdbc3.jdbc.SybDriver";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getProductName()
	 */
	public String getProductName() {
		
		return "Sybase ASAny";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getTypeName()
	 */
	public String getTypeName() {
		
		return "asany";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getUUIDType()
	 */
	public String getUUIDType() {
        return "UNIQUEIDENTIFIER";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#isCorrectDatabaseImplementation(java.sql.Connection)
	 */
	public boolean isCorrectDatabaseImplementation(Connection conn)
			throws JDBCException {
		return PRODUCT_NAME.equalsIgnoreCase(getDatabaseProductName(conn));
	}

	@Override
	public String getDefaultCatalogName() throws JDBCException {
        try {
            return getConnection().getCatalog();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
	}

	@Override
	protected String getDefaultDatabaseSchemaName() throws JDBCException {
		return null;
	}

	@Override
	public String convertRequestedSchemaToSchema(String requestedSchema)
			throws JDBCException {
        if (requestedSchema == null) {
            return "DBA";
        }
        return requestedSchema;
	}

	@Override
	public String getDefaultSchemaName() {
		// TODO Auto-generated method stub
		return super.getDefaultSchemaName();
	}

	@Override
	public String escapeColumnName(String schemaName, String tableName,
			String columnName) {
        return "[" + columnName + "]";
	}

	@Override
	public String getViewDefinition(String schemaName, String viewName)
			throws JDBCException {
		// TODO Auto-generated method stub
		return super.getViewDefinition(schemaName, viewName);
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#supportsInitiallyDeferrableColumns()
	 */
	public boolean supportsInitiallyDeferrableColumns() {
		return false;
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#supportsTablespaces()
	 */
	public boolean supportsTablespaces() {
		return true;
	}

	@Override
	public String convertRequestedSchemaToCatalog(String requestedSchema)
			throws JDBCException {
		// like in MS SQL
        return getDefaultCatalogName();
        
	}

	@Override
    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

	@Override
    public String getTrueBooleanValue() {
        return "1";
    }

	@Override
    public String getFalseBooleanValue() {
        return "0";
    }

    public boolean supportsSequences() {
        return false;
    }

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementClause()
	 */
	@Override
	public String getAutoIncrementClause() {
		return "default autoincrement";
	}
	
	@Override
    public SqlStatement getViewDefinitionSql(String schemaName, String viewName) throws JDBCException {
        String sql = "select viewtext from sysviews where upper(viewname)='" + viewName.toUpperCase() + "' and upper(vcreator) = '" + schemaName.toUpperCase() + '\'';
        return new RawSqlStatement(sql);
    }

	private String escapeName(String indexName) {
		return '[' + indexName + ']';
	}

}
