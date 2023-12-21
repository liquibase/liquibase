package liquibase.integration.spring;

import liquibase.*;
import liquibase.configuration.ConfiguredValue;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.ui.UIServiceEnum;
import liquibase.util.StringUtil;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.sql.DataSource;
import java.io.*;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

/**
 * A Spring-ified wrapper for Liquibase.
 * <p/>
 * Example Configuration:
 * <p/>
 * <p/>
 * This Spring configuration example will cause liquibase to run automatically when the Spring context is
 * initialized. It will load <code>db-changelog.xml</code> from the classpath and apply it against
 * <code>myDataSource</code>.
 * <p/>
 * <p/>
 *
 * <pre>
 * &lt;bean id=&quot;myLiquibase&quot;
 *          class=&quot;liquibase.spring.SpringLiquibase&quot;
 *          &gt;
 *
 *      &lt;property name=&quot;dataSource&quot; ref=&quot;myDataSource&quot; /&gt;
 *
 *      &lt;property name=&quot;changeLog&quot; value=&quot;classpath:db-changelog.xml&quot; /&gt;
 *
 * &lt;/bean&gt;
 *
 * </pre>
 *
 * @author Rob Schoening
 */
public class SpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {

    protected final Logger log = Scope.getCurrentScope().getLog(SpringLiquibase.class);
    protected String beanName;

	protected ResourceLoader resourceLoader;

	protected DataSource dataSource;
	protected String changeLog;
	protected String contexts;
    protected String labelFilter;
    protected String tag;
	protected Map<String, String> parameters;
	protected String defaultSchema;
	protected String liquibaseSchema;
	protected String databaseChangeLogTable;
	protected String databaseChangeLogLockTable;
	protected String liquibaseTablespace;
	protected boolean dropFirst;
	protected boolean clearCheckSums;
	protected boolean shouldRun = true;
	protected File rollbackFile;
    protected UpdateSummaryEnum showSummary;
    protected UpdateSummaryOutputEnum showSummaryOutput = UpdateSummaryOutputEnum.LOG;

	protected boolean testRollbackOnUpdate = false;

    protected UIServiceEnum uiService = UIServiceEnum.LOGGER;

	public SpringLiquibase() {
		super();
	}

	public boolean isDropFirst() {
		return dropFirst;
	}

	public void setDropFirst(boolean dropFirst) {
		this.dropFirst = dropFirst;
	}

	public boolean isClearCheckSums() {
		return clearCheckSums;
	}

	public void setClearCheckSums(boolean clearCheckSums) {
		this.clearCheckSums = clearCheckSums;
	}

	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	@java.lang.SuppressWarnings("squid:S2095")
	public String getDatabaseProductName() throws DatabaseException {
        Connection connection = null;
        Database database = null;
        String name = "unknown";
        try {
            connection = getDataSource().getConnection();
            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            name = database.getDatabaseProductName();
        }
        catch (SQLException e) {
            throw new DatabaseException(e);
        }
        finally {
            if (database != null) {
                database.close();
            }
            else {
                if (connection != null) {
                    try {
                        if (!connection.getAutoCommit()) {
                            connection.rollback();
                        }
                        connection.close();
                    } catch (SQLException e) {
                        log.warning("problem closing connection", e);
                    }
                }
		    }
       }
       return name;
	}

	/**
	 * The DataSource that liquibase will use to perform the migration.
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * The DataSource that liquibase will use to perform the migration.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Returns a Resource that is able to resolve to a file or classpath resource.
	 */
	public String getChangeLog() {
		return changeLog;
	}

	/**
	 * Sets a Spring Resource that is able to resolve to a file or classpath resource.
	 * An example might be <code>classpath:db-changelog.xml</code>.
	 */
	public void setChangeLog(String dataModel) {

		this.changeLog = dataModel;
	}

	public String getContexts() {
		return contexts;
	}

	public void setContexts(String contexts) {
		this.contexts = contexts;
	}

    /**
     * @deprecated use {@link #getLabelFilter()}
     */
    public String getLabels() {
        return getLabelFilter();
    }

    /**
     * @deprecated use {@link #setLabelFilter(String)}
     */
    public void setLabels(String labels) {
        setLabelFilter(labels);
    }

    public String getLabelFilter() {
        return labelFilter;
    }

    public void setLabelFilter(String labelFilter) {
        this.labelFilter = labelFilter;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

    public String getLiquibaseTablespace() {
        return liquibaseTablespace;
    }

    public void setLiquibaseTablespace(String liquibaseTablespace) {
        this.liquibaseTablespace = liquibaseTablespace;
    }

    public String getLiquibaseSchema() {
        return liquibaseSchema;
    }

    public void setLiquibaseSchema(String liquibaseSchema) {
        this.liquibaseSchema = liquibaseSchema;
    }

    public String getDatabaseChangeLogTable() {
        return databaseChangeLogTable;
    }

    public void setDatabaseChangeLogTable(String databaseChangeLogTable) {
        this.databaseChangeLogTable = databaseChangeLogTable;
    }

    public String getDatabaseChangeLogLockTable() {
        return databaseChangeLogLockTable;
    }

	public void setDatabaseChangeLogLockTable(String databaseChangeLogLockTable) {
		this.databaseChangeLogLockTable = databaseChangeLogLockTable;
	}

	/**
	 * Returns whether a rollback should be tested at update time or not.
	 */
	public boolean isTestRollbackOnUpdate() {
		return testRollbackOnUpdate;
	}

	/**
	 * If testRollbackOnUpdate is set to true a rollback will be tested at update time.
	 * For doing so when the update is performed
	 * @param testRollbackOnUpdate
     */
	public void setTestRollbackOnUpdate(boolean testRollbackOnUpdate) {
		this.testRollbackOnUpdate = testRollbackOnUpdate;
	}

	/**
	 * Executed automatically when the bean is initialized.
	 */
	@Override
    public void afterPropertiesSet() throws LiquibaseException {
		final ConfiguredValue<Boolean> shouldRunProperty = LiquibaseCommandLineConfiguration.SHOULD_RUN.getCurrentConfiguredValue();

		if (!(Boolean) shouldRunProperty.getValue()) {
            Scope.getCurrentScope().getLog(getClass()).info("Liquibase did not run because " +shouldRunProperty.getProvidedValue().describe() + " was set to false");
            return;
		}
		if (!shouldRun) {
            Scope.getCurrentScope().getLog(getClass()).info("Liquibase did not run because 'shouldRun' " + "property was set " +
                "to false on " + getBeanName() + " Liquibase Spring bean.");
            return;
		}

        try {

            Scope.child(Scope.Attr.ui.name(), this.uiService.getUiServiceClass().getDeclaredConstructor().newInstance(),
                    () -> {
                Liquibase liquibase = null;
                try {
                    final Connection c = getDataSource().getConnection();
                    liquibase = createLiquibase(c);
                    generateRollbackFile(liquibase);
                    performUpdate(liquibase);
                } catch (SQLException e) {
                    throw new DatabaseException(e);
                } finally {
                    if (liquibase != null) {
                        liquibase.close();
                    }
                }
            });
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private void generateRollbackFile(Liquibase liquibase) throws LiquibaseException {
        if (rollbackFile != null) {

            try (
                    final  OutputStream outputStream = Files.newOutputStream(rollbackFile.toPath());
                    Writer output = new OutputStreamWriter(outputStream, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()) )
			{

                if (tag != null) {
                    liquibase.futureRollbackSQL(tag, new Contexts(getContexts()),
                        new LabelExpression(getLabelFilter()), output);
                } else {
                    liquibase.futureRollbackSQL(new Contexts(getContexts()), new LabelExpression(getLabelFilter()), output);
                }
            } catch (IOException e) {
                throw new LiquibaseException("Unable to generate rollback file.", e);
            }
       }
    }

    protected void performUpdate(Liquibase liquibase) throws LiquibaseException {
        if (isClearCheckSums()) {
            liquibase.clearCheckSums();
        }

        if (isTestRollbackOnUpdate()) {
            if (tag != null) {
                liquibase.updateTestingRollback(tag, new Contexts(getContexts()), new LabelExpression(getLabelFilter()));
            } else {
                liquibase.updateTestingRollback(new Contexts(getContexts()), new LabelExpression(getLabelFilter()));
            }
        } else {
            if (tag != null) {
                liquibase.update(tag, new Contexts(getContexts()), new LabelExpression(getLabelFilter()));
            } else {
                liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabelFilter()));
            }
        }
    }

	@java.lang.SuppressWarnings("squid:S2095")
	protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
		SpringResourceAccessor resourceAccessor = createResourceOpener();
		Liquibase liquibase = new Liquibase(getChangeLog(), resourceAccessor, createDatabase(c, resourceAccessor));
		if (parameters != null) {
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
			}
		}

		if (isDropFirst()) {
			liquibase.dropAll();
		}

        liquibase.setShowSummaryOutput(showSummaryOutput);
        liquibase.setShowSummary(showSummary);

        if (liquibase.getDatabase() instanceof DerbyDatabase) {
            ((DerbyDatabase) liquibase.getDatabase()).setShutdownEmbeddedDerby(false);
        }

		return liquibase;
	}

    /**
     * Subclasses may override this method to modify the database settings, such as the default schema, before returning the database object.
     *
     * @param c the connection to the database
     * @return a Database implementation retrieved from the {@link DatabaseFactory}
     * @throws DatabaseException if there is an error retrieving the database implementation
     */
	protected Database createDatabase(Connection c, ResourceAccessor resourceAccessor) throws DatabaseException {

        DatabaseConnection liquibaseConnection;
        if (c == null) {
            log.warning("Null connection returned by liquibase datasource. Using offline unknown database");
            liquibaseConnection = new OfflineConnection("offline:unknown", resourceAccessor);

        } else {
            liquibaseConnection = new JdbcConnection(c);
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(liquibaseConnection);
		if (StringUtil.trimToNull(this.defaultSchema) != null) {
            if (database.supportsSchemas()) {
                database.setDefaultSchemaName(this.defaultSchema);
            } else if (database.supportsCatalogs()) {
                database.setDefaultCatalogName(this.defaultSchema);
            }
        }
        if (StringUtil.trimToNull(this.liquibaseSchema) != null) {
            if (database.supportsSchemas()) {
                database.setLiquibaseSchemaName(this.liquibaseSchema);
            } else if (database.supportsCatalogs()) {
                database.setLiquibaseCatalogName(this.liquibaseSchema);
            }
        }
        if (StringUtil.trimToNull(this.liquibaseTablespace) != null && database.supportsTablespaces()) {
            database.setLiquibaseTablespaceName(this.liquibaseTablespace);
        }
        if (StringUtil.trimToNull(this.databaseChangeLogTable) != null) {
            database.setDatabaseChangeLogTableName(this.databaseChangeLogTable);
        }
        if (StringUtil.trimToNull(this.databaseChangeLogLockTable) != null) {
            database.setDatabaseChangeLogLockTableName(this.databaseChangeLogLockTable);
        }
		return database;
	}

	public void setChangeLogParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Create a new resourceOpener.
	 */
	protected SpringResourceAccessor createResourceOpener() {
		return new SpringResourceAccessor(resourceLoader);
	}

	/**
	 * Gets the Spring-name of this instance.
	 *
	 * @return
	 */
	public String getBeanName() {
		return beanName;
	}

    /**
     * Spring sets this automatically to the instance's configured bean name.
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

	public void setRollbackFile(File rollbackFile) {
		this.rollbackFile = rollbackFile;
    }

    public void setShowSummary(UpdateSummaryEnum showSummary) {
        this.showSummary = showSummary;
    }

    public boolean isIgnoreClasspathPrefix() {
        return true;
    }

	/**
	 * @deprecated Always ignoring classpath prefix
	 */
	public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {

	}

    public void setShowSummaryOutput(UpdateSummaryOutputEnum showSummaryOutput) {
        if (showSummaryOutput == null) {
            return;
        }
        this.showSummaryOutput = showSummaryOutput;
    }

    public void setUiService(UIServiceEnum uiService) {
        if (uiService == null) {
            return;
        }
        this.uiService = uiService;
    }

    public UIServiceEnum getUiService() {
        return uiService;
    }

    @Override
    public String toString() {
        return getClass().getName() + "(" + this.getResourceLoader().toString() + ")";
    }

}
