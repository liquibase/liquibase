package liquibase.integration.cdi;

import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.NetUtil;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * A CDI wrapper for Liquibase.
 * <p/>
 * Example Configuration:
 * <p/>
 * <p/>
 * This CDI configuration example will cause liquibase to run
 * automatically when the CDI container is initialized. It will load
 * <code>db-changelog.xml</code> from the classpath and apply it against
 * <code>myDataSource</code>.
 * <p/>
 * Various producers methods are required to resolve the dependencies
 * i.e.
 * <code>
 * public class CDILiquibaseProducer {
 *
 *  @Produces @LiquibaseType
 *  public CDILiquibaseConfig createConfig() {
 *     CDILiquibaseConfig config = new CDILiquibaseConfig();
 *     config.setChangeLog("liquibase/parser/core/xml/simpleChangeLog.xml");
 *     return config;
 *  }
 *
 *  @Produces @LiquibaseType
 *  public DataSource createDataSource() throws SQLException {
 *     jdbcDataSource ds = new jdbcDataSource();
 *     ds.setDatabase("jdbc:hsqldb:mem:test");
 *     ds.setUser("sa");
 *     ds.setPassword("");
 *     return ds;
 *  }
 *
 *  @Produces @LiquibaseType
 *  public ResourceAccessor create() {
 *     return new ClassLoaderResourceAccessor(getClass().getClassLoader());
 *  }
 *
 * }
 * </code>
 *
 * @author Aaron Walker (http://github.com/aaronwalker)
 */
@ApplicationScoped
public class CDILiquibase implements Extension {

    private Logger log = LogFactory.getLogger(CDILiquibase.class.getName());

    @Inject @LiquibaseType
    private CDILiquibaseConfig config;

    @Inject @LiquibaseType
    private DataSource dataSource;

    @Inject @LiquibaseType
    ResourceAccessor resourceAccessor;

    private boolean initialized = false;
    private boolean updateSuccessful = false;

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isUpdateSuccessful() {
        return updateSuccessful;
    }

    @PostConstruct
    public void onStartup() {
        log.info("Booting Liquibase " + LiquibaseUtil.getBuildVersion());
        String hostName;
        try {
            hostName = NetUtil.getLocalHostName();
        } catch (Exception e) {
            log.warning("Cannot find hostname: " + e.getMessage());
            log.debug("", e);
            return;
        }

        LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration.getInstance();
        if (!liquibaseConfiguration.getConfiguration(GlobalConfiguration.class).getShouldRun()) {
            log.info("Liquibase did not run on " + hostName + " because " + liquibaseConfiguration.describeValueLookupLogic(GlobalConfiguration.class, GlobalConfiguration.SHOULD_RUN) + " was set to false");
            return;
        }
        initialized = true;
        try {
            performUpdate();
        } catch (LiquibaseException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void performUpdate() throws LiquibaseException {
        Connection c = null;
        Liquibase liquibase = null;
        try {
            c = dataSource.getConnection();
            liquibase = createLiquibase(c);
            liquibase.update(config.getContexts());
            updateSuccessful = true;
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } catch (LiquibaseException ex) {
            updateSuccessful = false;
            throw ex;
        } finally {
            if (c != null) {
                try {
                    c.rollback();
                    c.close();
                } catch (SQLException e) {
                    //nothing to do
                }

            }

        }
    }

    protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
        Liquibase liquibase = new Liquibase(config.getChangeLog(), resourceAccessor, createDatabase(c));
        if (config.getParameters() != null) {
            for(Map.Entry<String, String> entry: config.getParameters().entrySet()) {
                liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
            }
        }

        if (config.isDropFirst()) {
            liquibase.dropAll();
        }

        return liquibase;
    }

    /**
     * Subclasses may override this method add change some database settings such as
     * default schema before returning the database object.
     * @param c
     * @return a Database implementation retrieved from the {@link liquibase.database.DatabaseFactory}.
     * @throws DatabaseException
     */
    protected Database createDatabase(Connection c) throws DatabaseException {
        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
        if (config.getDefaultSchema() != null) {
            database.setDefaultSchemaName(config.getDefaultSchema());
        }
        return database;
    }
}