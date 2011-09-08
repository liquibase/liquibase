/**
 * 
 */
package liquibase.database.core;

import liquibase.database.AbstractDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

/**
 * @author otaranenko
 *
 */
public class SybaseASADatabase extends AbstractDatabase {

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

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }
    
	/* (non-Javadoc)
	 * @see liquibase.database.Database#getCurrentDateTimeFunction()
	 */
	public String getCurrentDateTimeFunction() {
        if (currentDateTimeFunction != null) {
            return currentDateTimeFunction;
        }
        
		return "now()";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
	 */
	public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc3.jdbc.SybDriver";
        } else {
            return null;
        }
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#getTypeName()
	 */
	public String getTypeName() {
		
		return "asany";
	}

	/* (non-Javadoc)
	 * @see liquibase.database.Database#isCorrectDatabaseImplementation(java.sql.Connection)
	 */
	public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
		return "Adaptive Server Anywhere".equalsIgnoreCase(conn.getDatabaseProductName())
                || "SQL Anywhere".equalsIgnoreCase(conn.getDatabaseProductName());
	}

	@Override
	public String getDefaultCatalogName() throws DatabaseException {
            return getConnection().getCatalog();
	}

	@Override
	protected String getDefaultDatabaseSchemaName() throws DatabaseException {
		return null;
	}

	@Override
	public String convertRequestedSchemaToSchema(String requestedSchema)
			throws DatabaseException {
        if (requestedSchema == null) {
            requestedSchema = getDefaultDatabaseSchemaName();
        }

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
	public String getViewDefinition(String schemaName, String viewName)
			throws DatabaseException {
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
			throws DatabaseException {
		// like in MS SQL
        return getDefaultCatalogName();
        
	}

	@Override
    public Set<String> getSystemTablesAndViews() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsSequences() {
        return false;
    }

	/* (non-Javadoc)
	 * @see liquibase.database.AbstractDatabase#getAutoIncrementClause()
	 */
	@Override
	protected String getAutoIncrementClause() {
		return "DEFAULT AUTOINCREMENT";
	}
	
	@Override
	protected boolean generateAutoIncrementStartWith(BigInteger startWith) {
		// not supported
		return false;
	}
	
	@Override
	protected boolean generateAutoIncrementBy(BigInteger incrementBy) {
		// not supported
		return false;
	}
	
	@Override
	public void setAutoCommit(boolean b) throws DatabaseException {
		// workaround for strange Sybase bug.
		// In some circumstances tds-driver thrown exception 
		// JZ016: The AutoCommit option is already set to false.
    	if (b || super.isAutoCommit()) {
    		super.setAutoCommit(b);
        }
	}

    @Override
    public String escapeDatabaseObject(String objectName) {
        return "\""+objectName+"\"";
    }
    
}
