package liquibase.integration.spring;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.util.StringUtils;
import liquibase.util.file.FilenameUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
 * &lt;/bean&gt;
 * 
 * </pre>
 * 
 * @author Rob Schoening
 */
public class SpringLiquibase implements InitializingBean, BeanNameAware, ResourceLoaderAware {

    public class SpringResourceOpener extends ClassLoaderResourceAccessor {

        private String parentFile;
        public SpringResourceOpener(String parentFile) {
            this.parentFile = parentFile;
        }

        @Override
        protected void init() {
            super.init();
            try {
                Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(getResourceLoader()).getResources("");
				if (resources.length == 0 || resources.length == 1 && !resources[0].exists()) { //sometimes not able to look up by empty string, try all the liquibase packages
					Set<String> liquibasePackages = new HashSet<String>();
					for (Resource manifest : ResourcePatternUtils.getResourcePatternResolver(getResourceLoader()).getResources("META-INF/MANIFEST.MF")) {
						if (manifest.exists()) {
							InputStream inputStream = null;
							try {
								inputStream = manifest.getInputStream();
								Manifest manifestObj = new Manifest(inputStream);
								Attributes attributes = manifestObj.getAttributes("Liquibase-Package");
								if (attributes != null) {
									for (Object attr : attributes.values()) {
										String packages = "\\s*,\\s*";
										;
										for (String fullPackage : attr.toString().split(packages)) {
											liquibasePackages.add(fullPackage.split("\\.")[0]);
										}
                                    }
								}
							} finally {
								if (inputStream != null) {
									inputStream.close();
								}
							}
						}
					}

                    if (liquibasePackages.size() == 0) {
                        LogFactory.getInstance().getLog().warning("No Liquibase-Packages entry found in MANIFEST.MF. Using fallback of entire 'liquibase' package");
                        liquibasePackages.add("liquibase");
                    }

                    for (String foundPackage : liquibasePackages) {
						resources = ResourcePatternUtils.getResourcePatternResolver(getResourceLoader()).getResources(foundPackage);
						for (Resource res : resources) {
							addRootPath(res.getURL());
						}
					}
				} else {
					for (Resource res : resources) {
						addRootPath(res.getURL());
					}
				}
            } catch (IOException e) {
                LogFactory.getInstance().getLog().warning("Error initializing SpringLiquibase", e);
            }
        }

        @Override
        public Set<String> list(String relativeTo, String path, boolean includeFiles, boolean includeDirectories, boolean recursive) throws IOException {
            Set<String> returnSet = new HashSet<String>();
            
            String tempFile = FilenameUtils.concat(FilenameUtils.getFullPath(relativeTo), path);

			Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(getResourceLoader()).getResources(adjustClasspath(tempFile));

			for (Resource res : resources) {
				Set<String> list = super.list(null, res.getURL().toExternalForm(), includeFiles, includeDirectories, recursive);
				if (list != null) {
					returnSet.addAll(list);
				}
			}

            return returnSet;
		}

        @Override
        public Set<InputStream> getResourcesAsStream(String path) throws IOException {
            Set<InputStream> returnSet = new HashSet<InputStream>();
            Resource[] resources = ResourcePatternUtils.getResourcePatternResolver(getResourceLoader()).getResources(adjustClasspath(path));

            if (resources == null || resources.length == 0) {
                return null;
            }
            for (Resource resource : resources) {
            	URLConnection connection = resource.getURL().openConnection();
            	connection.setUseCaches(false);
            	returnSet.add(connection.getInputStream());
            }

            return returnSet;
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

	protected String beanName;

	protected ResourceLoader resourceLoader;

	protected DataSource dataSource;

	protected final Logger log = LogFactory.getLogger(SpringLiquibase.class.getName());

	protected String changeLog;

	protected String contexts;

    protected String labels;

	protected Map<String, String> parameters;

	protected String defaultSchema;

	protected boolean dropFirst = false;

	protected boolean shouldRun = true;

	protected File rollbackFile;
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
        Database database = null;
		String name = "unknown";
		try {
			connection = getDataSource().getConnection();
			database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
			name = database.getDatabaseProductName();
		} catch (SQLException e) {
			throw new DatabaseException(e);
		} finally {
            if (database != null) {
                database.close();
            } else if (connection != null) {
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

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
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
        ConfigurationProperty shouldRunProperty = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.SHOULD_RUN);

		if (!shouldRunProperty.getValue(Boolean.class)) {
			LogFactory.getLogger().info("Liquibase did not run because "+ LiquibaseConfiguration.getInstance().describeValueLookupLogic(shouldRunProperty)+" was set to false");
			return;
		}
		if (!shouldRun) {
			LogFactory.getLogger().info("Liquibase did not run because 'shouldRun' " + "property was set to false on " + getBeanName() + " Liquibase Spring bean.");
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
			Database database = null;
			if (liquibase != null) {
				database = liquibase.getDatabase();
			}
			if (database != null) {
                database.close();
            }
        }

	}

    private void generateRollbackFile(Liquibase liquibase) throws LiquibaseException {
        if (rollbackFile != null) {
            FileWriter output = null;
            try {
                output = new FileWriter(rollbackFile);
                liquibase.futureRollbackSQL(null, new Contexts(getContexts()), new LabelExpression(getLabels()), output);
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
		liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()));
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

        DatabaseConnection liquibaseConnection;
        if (c == null) {
            log.warning("Null connection returned by liquibase datasource. Using offline unknown database");
            liquibaseConnection = new OfflineConnection("offline:unknown");

        } else {
            liquibaseConnection = new JdbcConnection(c);
        }

        Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(liquibaseConnection);
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
