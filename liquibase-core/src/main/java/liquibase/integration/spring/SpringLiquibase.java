package liquibase.integration.spring;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtils;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A Spring-ified wrapper for Liquibase.
 * <p/>
 * Example Configuration:
 * <p/>
 * <p/>
 * This Spring configuration example will cause liquibase to run automatically when the Spring context is initialized. It will load
 * <code>db-changelog.xml</code> from the classpath and apply it against <code>myDataSource</code>.
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
 *      &lt;!-- The following configuration options are optional --&gt;
 * 
 *      &lt;property name=&quot;executeEnabled&quot; value=&quot;true&quot; /&gt;
 * 
 *      &lt;!--
 *      If set to true, writeSqlFileEnabled will write the generated
 *      SQL to a file before executing it.
 *      --&gt;
 *      &lt;property name=&quot;writeSqlFileEnabled&quot; value=&quot;true&quot; /&gt;
 * 
 *      &lt;!--
 *      sqlOutputDir specifies the directory into which the SQL file
 *      will be written, if so configured.
 *      --&gt;
 *      &lt;property name=&quot;sqlOutputDir&quot; value=&quot;c:\sql&quot; /&gt;
 * 
 * &lt;/bean&gt;
 * 
 * </pre>
 * 
 * @author Rob Schoening
 */
public class SpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {

    public class SpringResourceOpener implements ResourceAccessor {

        private String parentFile;
        public SpringResourceOpener(String parentFile) {
            this.parentFile = parentFile;
        }

        @Override
        public Enumeration<URL> getResources(String packageName) throws IOException {
            List<URL> tmp = new ArrayList<URL>();

			Resource[] resources = org.springframework.core.io.support.ResourcePatternUtils.getResourcePatternResolver(getResourceLoader())
					.getResources(adjustClasspath(packageName));

			for (Resource res : resources) {
				tmp.add(res.getURL());
			}

            return Collections.enumeration(tmp);
		}

		@Override
        public InputStream getResourceAsStream(String file) throws IOException {
            Enumeration<URL> resources = getResources(file);
            // take first found resource from classpath
			if (resources.hasMoreElements()) {
				return resources.nextElement().openStream();
			} else {
				return null;
			}
		}

		public Resource getResource(String file) {
			return getResourceLoader().getResource(adjustClasspath(file));
		}

		private String adjustClasspath(String file) {
			return isPrefixPresent(parentFile) && !isPrefixPresent(file) ? ResourceLoader.CLASSPATH_URL_PREFIX + file : file;
		}

		public boolean isPrefixPresent(String file) {
			if (file.startsWith("classpath") || file.startsWith("file:") || file.startsWith("url:")) {
				return true;
			}
			return false;
		}

		@Override
        public ClassLoader toClassLoader() {
			return getResourceLoader().getClassLoader();
		}
	}

	private String beanName;

	private ResourceLoader resourceLoader;

	private DataSource dataSource;

	private final Logger log = LogFactory.getLogger(SpringLiquibase.class.getName());

	private String changeLog;

	private String contexts;

	private Map<String, String> parameters;

	private String defaultSchema;

	private boolean dropFirst = false;

	private boolean shouldRun = true;

	private File rollbackFile;
    /**
     * Ignores classpath prefix during changeset comparison.
     * This is particularly useful if Liquibase is run in different ways.
     *
     * For instance, if Maven plugin is used to run changesets, as in:
     * <code>
     *      &lt;configuration&gt;
     *          ...
     *          &lt;changeLogFile&gt;path/to/changelog&lt;/changeLogFile&gt;
     *      &lt;/configuration&gt;
     * </code>
     *
     * And {@link SpringLiquibase} is configured like:
     * <code>
     *     SpringLiquibase springLiquibase = new SpringLiquibase();
     *     springLiquibase.setChangeLog("classpath:path/to/changelog");
     * </code>
     *
     * or, in equivalent XML configuration:
     * <code>
     *     &lt;bean id="springLiquibase" class="liquibase.integration.spring.SpringLiquibase"&gt;
     *         &lt;property name="changeLog" value="path/to/changelog" /&gt;
     *      &lt;/bean&gt;
     * </code>
     *
     * {@link Liquibase#listUnrunChangeSets(String)} will
     * always, by default, return changesets, regardless of their
     * execution by Maven.
     * Maven-executed changeset path name are not be prepended by
     * "classpath:" whereas the ones parsed via SpringLiquibase are.
     *
     * To avoid this issue, just set ignoreClasspathPrefix to true.
     */
    private boolean ignoreClasspathPrefix = true;

	public SpringLiquibase() {
		super();
	}

	public boolean isDropFirst() {
		return dropFirst;
	}

	public void setDropFirst(boolean dropFirst) {
		this.dropFirst = dropFirst;
	}

	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public String getDatabaseProductName() throws DatabaseException {
		Connection connection = null;
		String name = "unknown";
		try {
			connection = getDataSource().getConnection();
			Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
					new JdbcConnection(dataSource.getConnection()));
			name = database.getDatabaseProductName();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (connection != null) {
				try {
					if (!connection.getAutoCommit()) {
						connection.rollback();
					}
					connection.close();
				} catch (Exception e) {
					log.warning("problem closing connection", e);
				}
			}
		}
		return name;
	}

	/**
	 * The DataSource that liquibase will use to perform the migration.
	 * 
	 * @return
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
	 * 
	 * @return
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

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	/**
	 * Executed automatically when the bean is initialized.
	 */
	@Override
    public void afterPropertiesSet() throws LiquibaseException {
		String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
		if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
			LogFactory.getLogger().info(
					"Liquibase did not run because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
			return;
		}
		if (!shouldRun) {
			LogFactory.getLogger().info(
					"Liquibase did not run because 'shouldRun' " + "property was set to false on " + getBeanName()
							+ " Liquibase Spring bean.");
			return;
		}

		Connection c = null;
		Liquibase liquibase = null;
		try {
			c = getDataSource().getConnection();
			liquibase = createLiquibase(c);
			generateRollbackFile(liquibase);
			performUpdate(liquibase);
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
			if (c != null) {
				try {
					if (!c.getAutoCommit()) {
						c.rollback();
					}
				} catch (SQLException e) {
					// nothing to do
				}
				try {
					c.close();
				} catch (SQLException e) {
					// nothing to do
				}
			}
		}

	}

    private void generateRollbackFile(Liquibase liquibase) throws LiquibaseException {
        if (rollbackFile != null) {
            FileWriter output = null;
            try {
                output = new FileWriter(rollbackFile);
                liquibase.futureRollbackSQL(getContexts(), output);
            } catch (IOException e) {
                throw new LiquibaseException("Unable to generate rollback file.", e);
            } finally {
                try {
                    if (output != null) {
                        output.close();
                    }
                } catch (IOException e) {
                    log.severe("Error closing output", e);
                }
            }
        }
    }

	protected void performUpdate(Liquibase liquibase) throws LiquibaseException {
		liquibase.update(getContexts());
	}

	protected Liquibase createLiquibase(Connection c) throws LiquibaseException {
		Liquibase liquibase = new Liquibase(getChangeLog(), createResourceOpener(), createDatabase(c));
        liquibase.setIgnoreClasspathPrefix(isIgnoreClasspathPrefix());
		if (parameters != null) {
			for (Map.Entry<String, String> entry : parameters.entrySet()) {
				liquibase.setChangeLogParameter(entry.getKey(), entry.getValue());
			}
		}

		if (isDropFirst()) {
			liquibase.dropAll();
		}

		return liquibase;
	}

	/**
	 * Subclasses may override this method add change some database settings such as
	 * default schema before returning the database object.
	 * 
	 * @param c
	 * @return a Database implementation retrieved from the {@link DatabaseFactory}.
	 * @throws DatabaseException
	 */
	protected Database createDatabase(Connection c) throws DatabaseException {
		Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c));
		if (StringUtils.trimToNull(this.defaultSchema) != null) {
			database.setDefaultSchemaName(this.defaultSchema);
		}
		return database;
	}

	public void setChangeLogParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	/**
	 * Create a new resourceOpener.
	 */
	protected SpringResourceOpener createResourceOpener() {
		return new SpringResourceOpener(getChangeLog());
	}

	/**
	 * Spring sets this automatically to the instance's configured bean name.
	 */
	@Override
    public void setBeanName(String name) {
		this.beanName = name;
	}

	/**
	 * Gets the Spring-name of this instance.
	 * 
	 * @return
	 */
	public String getBeanName() {
		return beanName;
	}

	@Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	public void setRollbackFile(File rollbackFile) {
		this.rollbackFile = rollbackFile;
    }

    public boolean isIgnoreClasspathPrefix() {
        return ignoreClasspathPrefix;
    }

    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + this.getResourceLoader().toString() + ")";
	}
}
