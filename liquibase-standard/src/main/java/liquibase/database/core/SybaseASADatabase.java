/**
 * 
 */
package liquibase.database.core;

import liquibase.CatalogAndSchema;
import liquibase.database.AbstractJdbcDatabase;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.jvm.SybaseASAConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RawParameterizedSqlStatement;
import liquibase.structure.core.Index;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Support for SAP (formerly: Sybase) AS (Adapter Server) Anywhere embedded/mobile database.
 *
 * @author otaranenko
 *
 */
public class SybaseASADatabase extends AbstractJdbcDatabase {

    private static final Set<String> systemTablesAndViews;

    static {
        systemTablesAndViews = new HashSet<>();
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
        super.setCurrentDateTimeFunction("now()");
        super.unmodifiableDataTypes.addAll(Arrays.asList("integer", "bigint"));

    }

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#getDefaultDriver(java.lang.String)
     */
    @Override
    public String getDefaultDriver(String url) {
        if (url.startsWith("jdbc:sqlanywhere")) {
            return "sap.jdbc4.sqlanywhere.IDriver";
        } else if (url.startsWith("jdbc:sybase")) {
            return "com.sybase.jdbc4.jdbc.SybDriver";
        } else if (url.startsWith("jdbc:ianywhere")) {
            return "ianywhere.ml.jdbcodbc.jdbc3.IDriver";
        } else {
            return null;
        }
    }

    @Override
    public Integer getDefaultPort() {
        return 2638;
    }

    @Override
    protected String getDefaultDatabaseProductName() {
        return "Sybase Anywhere";
    }

    /* (non-Javadoc)
    * @see liquibase.database.Database#getShortName()
    */
    @Override
    public String getShortName() {
        return "asany";
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#isCorrectDatabaseImplementation(java.sql.Connection)
     */
    @Override
    public boolean isCorrectDatabaseImplementation(DatabaseConnection conn) throws DatabaseException {
        return "Adaptive Server Anywhere".equalsIgnoreCase(conn.getDatabaseProductName())
                || "SQL Anywhere".equalsIgnoreCase(conn.getDatabaseProductName())
                || "Adaptive Server IQ".equalsIgnoreCase(conn.getDatabaseProductName())
                || "Sybase IQ".equalsIgnoreCase(conn.getDatabaseProductName());
    }

    @Override
    public String getDefaultCatalogName() {
        try {
            DatabaseConnection connection = getConnection();
            if (connection == null) {
                return null;
            }
            return connection.getCatalog();
        } catch (DatabaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    protected String getConnectionSchemaName() {
        try {
            if (getConnection() instanceof OfflineConnection) {
                return "dba";
            } else {
                Connection connection = ((JdbcConnection) getConnection()).getWrappedConnection();
                return ((connection == null) ? null : connection.getMetaData().getUserName());
            }
        } catch (SQLException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public String getViewDefinition(CatalogAndSchema schema, String viewName)
            throws DatabaseException {
        return super.getViewDefinition(schema, viewName);
    }

    /* (non-Javadoc)
     * @see liquibase.database.Database#supportsInitiallyDeferrableColumns()
     */
    @Override
    public boolean supportsInitiallyDeferrableColumns() {
        return true;
    }

    @Override
    public boolean supportsDropTableCascadeConstraints() {
        return false;
    }

    /* (non-Javadoc)
         * @see liquibase.database.Database#supportsTablespaces()
         */
    @Override
    public boolean supportsTablespaces() {
        return true;
    }

    @Override
    public Set<String> getSystemViews() {
        return systemTablesAndViews;
    }

    @Override
    public boolean supportsSequences() {
        return true;
    }

    /* (non-Javadoc)
     * @see liquibase.database.AbstractJdbcDatabase#getAutoIncrementClause()
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
    public String getJdbcCatalogName(CatalogAndSchema schema) {
        return "";
    }

    @Override
    public String escapeIndexName(String catalogName, String schemaName, String indexName) {
        /*
         * https://help.sap.com/viewer/40c01c3500744c85a02db71276495de5/17.0/en-US/816be9016ce210148874e67c83ca6c67.html
         * "There is no way of specifying the index owner in the CREATE INDEX statement. Indexes are always owned by
         * the owner of the table or materialized view."
         *
         * As a consequence, we will always remove the index owner.
         */
        return escapeObjectName(indexName, Index.class);
    }

    @Override
    public void setConnection(DatabaseConnection conn) {
        DatabaseConnection dbConn;
        if (conn instanceof JdbcConnection) {
            // If conn is a real connection (JDBC), wrap it to prevent a driver bug
            // (see SysbaseASAConnection for details)
            dbConn = new SybaseASAConnection(((JdbcConnection) conn).getWrappedConnection());
        } else {
            dbConn = conn;
        }
        super.setConnection(dbConn);
    }

    @Override
    public boolean requiresExplicitNullForColumns() {
        /* SAP Adaptive Server Enterprise and, by extension, SQL Anywhere in ASE compatibility mode have the
         * strange requirement of setting the nullability of a column to NOT NULL if neither NULL nor
         * NOT NULL are specified. See:
         * http://dcx.sap.com/index.html#sqla170/en/html/819378356ce21014a17f8d51529119ee.html
         */
        return true;
    }

    @Override
    public int getMaxFractionalDigitsForTimestamp() {
        /*
         * SQL Anywhere statically uses exactly 6 decimal places for the fraction.
         * See: https://help.sap.com/docs/SAP_SQL_Anywhere/93079d4ba8e44920ae63ffb4def91f5b/81fe344c6ce21014a4f29d9e0af358b9.html
         */
        return 6;
    }

    @Override
    public void afterUpdate() throws LiquibaseException {
        /*
         * SQL Anywhere needs recompilation of views after an update, when the underlying tables have changed.
         * It is safe to always recompile as long as we ignore any compilation failures.
         */
        this.execute(new SqlStatement[] {new RawParameterizedSqlStatement("sa_recompile_views(1)")}, null);
    }

}
