package liquibase.integration.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.NetUtil;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;

/**
 * <p>
 * The abstract liquibase hibernate integrator is used to run liquibase before
 * hibernate validation (hibernate.hbm2ddl.auto -&gt; validate). You have to use
 * (DataSource) InitialContext.doLookup("java:jboss/datasources/myDS") to get
 * the datasource / System.getProperty("myProperty") for system properties. If
 * you have a multi module project use
 * <b>LiquibaseHibernateIntegratorConfig.setMultiProjectSetup(boolean)</b>, if
 * you want to drop the database schema afterwards
 * <b>LiquibaseHibernateIntegratorConfig.setDropAtShutdown(boolean)</b>
 * </p>
 * Example:
 * 
 * <pre>
 * <code>
 * <b>Java-Class:</b>
 * public class LiquibaseIntegrator extends AbstractHibernateLiquibaseIntegrator {
 * 
 *     {@literal @}Override
 *     public ResourceAccessor create(configuration, metaDataImplementor, sessionFactoryImplementor, sessionFactoryServiceRegistry) {
 *          return new ClassLoaderResourceAccessor(getClass().getClassLoader());
 *     }
 * 
 *     {@literal @}Override
 *     public DataSource createDataSource(configuration, metaDataImplementor, sessionFactoryImplementor, sessionFactoryServiceRegistry) {
 *          try {
 *               return (DataSource) InitialContext.doLookup("java:jboss/datasources/myDS");
 *          } catch (NamingException e) {
 *               throw new IllegalStateException("The datasource couldn't be looked up!");
 *          }
 *     }
 * 
 *     {@literal @}Override
 *     public LiquibaseHibernateIntegratorConfig createConfig(configuration, metaDataImplementor, sessionFactoryImplementor, sessionFactoryServiceRegistry) {
 *          LiquibaseHibernateIntegratorConfig config = new LiquibaseHibernateIntegratorConfig();
 *          config.setChangeLog("my/package/db.changelog.xml");
 *          config.setDefaultSchema(System.getProperty("schema"));
 *          return config;
 *     }
 * }
 * 
 * <b>Others:</b>
 * Create folder <b>META-INF\services</b> and add the file <b>org.hibernate.integrator.spi.Integrator</b> 
 * and add the LiquibaseIntegrator as a line in that file (org.package.LiquibaseIntegrator)
 * 
 * </code>
 * </pre>
 * 
 * <b>!!!!!! Note that you can't use any dependency injections via {@literal @}
 * Inject !!!!!!</b><br>
 * <br>
 *
 * @author Tobias Soloschenko
 *
 */
public abstract class AbstractHibernateLiquibaseIntegrator implements
	Integrator {

    private static final Logger log = Logger
	    .getLogger(AbstractHibernateLiquibaseIntegrator.class.getName());

    private LiquibaseHibernateIntegratorConfig config;

    private DataSource dataSource;

    private ResourceAccessor resourceAccessor;

    private boolean initalized;

    private Liquibase liquibase;

    private Connection lockConnection;

    private Statement databaseMultiProjectSetupLockStatement;

    /**
     * Runs the liquibase setup
     */
    @Override
    public void integrate(Configuration configuration,
	    SessionFactoryImplementor sessionFactory,
	    SessionFactoryServiceRegistry serviceRegistry) {
	init(configuration, null, sessionFactory, serviceRegistry);
    }

    /**
     * Runs the liquibase setup
     */
    @Override
    public void integrate(MetadataImplementor metadata,
	    SessionFactoryImplementor sessionFactory,
	    SessionFactoryServiceRegistry serviceRegistry) {
	init(null, metadata, sessionFactory, serviceRegistry);
    }

    /**
     * Runs the liquibase tear down
     */
    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
	    SessionFactoryServiceRegistry serviceRegistry) {
	destroy();
	if (initalized && config.isDropAtShutdown()) {
	    try {
		liquibase.dropAll();
	    } catch (DatabaseException e) {
		// NOOP - maybe an error if the schema has been droped already
	    } catch (LockException e) {
		// NOOP - maybe an error if the schema has been droped already
	    }
	}
    }

    /**
     * If updates based on liquibase should be performed
     * 
     * @return if updates should be performed
     */
    protected boolean isAutoUpdateActive() {
	return true;
    }

    /**
     * Creates the resource accessor
     * 
     * @param configuration
     *            the hibernate configuration or null if init is invoked from
     *            integrate with metaDataImplementor
     * @param metaDataImplementor
     *            the hibernate meta data configuration or null if init is
     *            invoked from integrate with configuration
     * @param sessionFactoryServiceRegistry
     *            the hibernate session factory service registry
     * @param sessionFactoryImplementor
     *            the hibernate session factory implementor
     * 
     * @return the resource accessor
     * 
     * @see {@link liquibase.resource.ResourceAccessor}
     */
    protected abstract ResourceAccessor create(Configuration configuration,
	    MetadataImplementor metaDataImplementor,
	    SessionFactoryImplementor sessionFactoryImplementor,
	    SessionFactoryServiceRegistry sessionFactoryServiceRegistry);

    /**
     * Creates the data source
     * 
     * @param configuration
     *            the hibernate configuration or null if init is invoked from
     *            integrate with metaDataImplementor
     * @param metaDataImplementor
     *            the hibernate meta data configuration or null if init is
     *            invoked from integrate with configuration
     * @param sessionFactoryServiceRegistry
     *            the hibernate session factory service registry
     * @param sessionFactoryImplementor
     *            the hibernate session factory implementor
     * 
     * @return the datasource
     * 
     * @see {@link javax.sql.DataSource}
     */
    protected abstract DataSource createDataSource(Configuration configuration,
	    MetadataImplementor metaDataImplementor,
	    SessionFactoryImplementor sessionFactoryImplementor,
	    SessionFactoryServiceRegistry sessionFactoryServiceRegistry);

    /**
     * Creates the liquibase hibernate integrator config
     * 
     * @param configuration
     *            the hibernate configuration or null if init is invoked from
     *            integrate with metaDataImplementor
     * @param metaDataImplementor
     *            the hibernate meta data configuration or null if init is
     *            invoked from integrate with configuration
     * @param sessionFactoryServiceRegistry
     *            the hibernate session factory service registry
     * @param sessionFactoryImplementor
     *            the hibernate session factory implementor
     * 
     * @return the liquibase hibernate integrator config
     * 
     * @see {@link liquibase.integration.hibernate.LiquibaseHibernateIntegratorConfig}
     */
    protected abstract LiquibaseHibernateIntegratorConfig createConfig(
	    Configuration configuration,
	    MetadataImplementor metaDataImplementor,
	    SessionFactoryImplementor sessionFactoryImplementor,
	    SessionFactoryServiceRegistry sessionFactoryServiceRegistry);

    /**
     * Initializes the liquibase context
     * 
     * @param configuration
     *            the hibernate configuration or null if init is invoked from
     *            integrate with metaDataImplementor
     * @param metaDataImplementor
     *            the hibernate meta data configuration or null if init is
     *            invoked from integrate with configuration
     * @param sessionFactoryServiceRegistry
     *            the hibernate session factory service registry
     * @param sessionFactoryImplementor
     *            the hibernate session factory implementor
     */
    protected void init(Configuration configuration,
	    MetadataImplementor metaDataImplementor,
	    SessionFactoryImplementor sessionFactoryImplementor,
	    SessionFactoryServiceRegistry sessionFactoryServiceRegistry) {
	if (!isAutoUpdateActive()) {
	    return;
	}
	config = createConfig(configuration, metaDataImplementor,
		sessionFactoryImplementor, sessionFactoryServiceRegistry);
	dataSource = createDataSource(configuration, metaDataImplementor,
		sessionFactoryImplementor, sessionFactoryServiceRegistry);
	resourceAccessor = create(configuration, metaDataImplementor,
		sessionFactoryImplementor, sessionFactoryServiceRegistry);
	initLock();
	runLiquibase();
	releaseLock();
    }

    /**
     * Executed if desintegrate is invoked
     */
    protected void destroy() {
    }

    /**
     * Initializes a lock for multi project setup
     */
    private void initLock() {
	if (config.isMultiProjectSetup()) {
	    try {
		lockConnection = dataSource.getConnection();
		lockConnection.setAutoCommit(false);
		Statement databaseMultiProjectSetupCreateTableStatement = lockConnection
			.createStatement();
		try {
		    databaseMultiProjectSetupCreateTableStatement
			    .executeUpdate("CREATE TABLE DATABASEMULTIPROJECTSETUPLOCK (locked int)");
		    lockConnection.commit();
		} catch (SQLException e) {
		    log.log(Level.WARNING,
			    "Execution of create table database multi setup lock failed - it may there already");
		} finally {
		    try {
			if (databaseMultiProjectSetupCreateTableStatement != null) {
			    databaseMultiProjectSetupCreateTableStatement
				    .close();
			}
		    } catch (SQLException e) {
			log.log(Level.WARNING,
				"Error while closing the statement of create table database multi setup lock",
				e);
		    }
		}

		databaseMultiProjectSetupLockStatement = lockConnection
			.createStatement();
		databaseMultiProjectSetupLockStatement
			.execute("LOCK TABLE DATABASEMULTIPROJECTSETUPLOCK IN EXCLUSIVE MODE");
	    } catch (SQLException e) {
		try {
		    if (lockConnection != null) {
			lockConnection.rollback();
		    }
		} catch (SQLException e1) {
		    log.log(Level.WARNING,
			    "Error while rolling back the transaction of multi project setup",
			    e);
		}
		log.log(Level.SEVERE,
			"Error while aquiring the lock of multi project setup",
			e);
	    }
	}
    }

    /**
     * Releases the lock of the multi setup environment
     */
    private void releaseLock() {
	if (config.isMultiProjectSetup()) {
	    try {
		lockConnection.commit();
	    } catch (SQLException e) {
		log.log(Level.SEVERE,
			"Error while committing the lock connection", e);
	    } finally {
		if (lockConnection != null) {
		    try {
			lockConnection.close();
		    } catch (SQLException e) {
			log.log(Level.SEVERE,
				"Error while closing the connection while committing the release of the lock of multi project setup",
				e);
		    }
		}
		if (databaseMultiProjectSetupLockStatement != null) {
		    try {
			databaseMultiProjectSetupLockStatement.close();
		    } catch (SQLException e) {
			log.log(Level.SEVERE,
				"Error while closing the databaseMultiProjectSetupLockStatement while committing the release of the lock of multi project setup",
				e);
		    }
		}
	    }
	}
    }

    /**
     * Runs the liquibase setup
     */
    protected void runLiquibase() {
	log.log(Level.INFO,
		"Booting Liquibase " + LiquibaseUtil.getBuildVersion());
	String hostName;
	try {
	    hostName = NetUtil.getLocalHostName();
	} catch (Exception e) {
	    log.log(Level.SEVERE, "Cannot find hostname: " + e.getMessage(), e);
	    return;
	}

	LiquibaseConfiguration liquibaseConfiguration = LiquibaseConfiguration
		.getInstance();
	if (!liquibaseConfiguration.getConfiguration(GlobalConfiguration.class)
		.getShouldRun()) {
	    log.info("Liquibase did not run on "
		    + hostName
		    + " because "
		    + liquibaseConfiguration.describeValueLookupLogic(
			    GlobalConfiguration.class,
			    GlobalConfiguration.SHOULD_RUN)
		    + " was set to false");
	    return;
	}
	try {
	    performUpdate();
	} catch (LiquibaseException e) {
	    log.log(Level.SEVERE, "Error while performing liquibase update", e);
	}
    }

    /**
     * Performs the changelog updates on the database
     * 
     * @throws LiquibaseException
     *             if an error occured during the update
     */
    protected void performUpdate() throws LiquibaseException {
	Connection c = null;
	try {
	    c = dataSource.getConnection();
	    liquibase = createLiquibase(c);
	    liquibase.getDatabase();
	    liquibase.update(new Contexts(config.getContexts()),
		    new LabelExpression(config.getLabels()));
	    initalized = true;
	} catch (SQLException e) {
	    initalized = false;
	    throw new DatabaseException(e);
	} catch (LiquibaseException ex) {
	    initalized = false;
	    throw ex;
	} finally {
	    if (liquibase != null && liquibase.getDatabase() != null) {
		liquibase.getDatabase().close();
	    } else if (c != null) {
		try {
		    c.rollback();
		    c.close();
		} catch (SQLException e) {
		    // nothing to do
		}

	    }

	}
    }

    /**
     * Creates liquibase on the given connection
     * 
     * @param connection
     *            the connection
     * @return liquibase
     * @throws LiquibaseException
     */
    protected Liquibase createLiquibase(Connection connection)
	    throws LiquibaseException {
	Liquibase liquibase = new Liquibase(config.getChangeLog(),
		resourceAccessor, createDatabase(connection));
	if (config.getParameters() != null) {
	    for (Map.Entry<String, String> entry : config.getParameters()
		    .entrySet()) {
		liquibase.setChangeLogParameter(entry.getKey(),
			entry.getValue());
	    }
	}

	if (config.isDropFirst()) {
	    liquibase.dropAll();
	}

	return liquibase;
    }

    /**
     * Creates the liquibase database on the given connection
     * 
     * @param connection
     *            the connection
     * @return the database
     * @throws DatabaseException
     *             if the database couldn't be created
     */
    protected Database createDatabase(Connection connection)
	    throws DatabaseException {
	Database database = DatabaseFactory.getInstance()
		.findCorrectDatabaseImplementation(
			new JdbcConnection(connection));
	if (config.getDefaultSchema() != null) {
	    database.setDefaultSchemaName(config.getDefaultSchema());
	}
	return database;
    }
}
