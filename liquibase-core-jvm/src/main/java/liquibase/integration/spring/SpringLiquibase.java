package liquibase.integration.spring;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Vector;

import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.resource.ResourceAccessor;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 * A Spring-ified wrapper for Liquibase.
 *
 * Example Configuration:
 * <p>
 * <p>
 * This Spring configuration example will cause liquibase to run
 * automatically when the Spring context is initialized. It will load
 * <code>db-changelog.xml</code> from the classpath and apply it against
 * <code>myDataSource</code>.
 * <p>
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


        public InputStream getResourceAsStream(String file) throws IOException {
            try {
                Resource resource = getResource(file);
                return resource.getInputStream();
            }
            catch ( FileNotFoundException ex ) {
                return null;
            }
        }

        public Enumeration<URL> getResources(String packageName) throws IOException {
            Vector<URL> tmp = new Vector<URL>();

            tmp.add(getResource(packageName).getURL());

            return tmp.elements();
        }

        public Resource getResource(String file) {
            return getResourceLoader().getResource(adjustClasspath(file));
        }

        private String adjustClasspath(String file) {
            return isClasspathPrefixPresent(parentFile) && !isClasspathPrefixPresent(file)
                    ? ResourceLoader.CLASSPATH_URL_PREFIX + file
                    : file;
        }

        public boolean isClasspathPrefixPresent(String file) {
            return file.startsWith(ResourceLoader.CLASSPATH_URL_PREFIX);
        }

        public ClassLoader toClassLoader() {
            return getResourceLoader().getClassLoader();
        }
    }

    private String beanName;

    private ResourceLoader resourceLoader;

    private DataSource dataSource;

    private Logger log = LogFactory.getLogger(SpringLiquibase.class.getName());

    private String changeLog;

    private String contexts;

    public SpringLiquibase() {
        super();
    }

    public String getDatabaseProductName() throws DatabaseException {
        Connection connection = null;
        String name = "unknown";
        try {
            connection = getDataSource().getConnection();
            Database database =
                    DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(dataSource.getConnection()));
            name = database.getDatabaseProductName();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        } finally {
            if (connection != null) {
                try {
                    if (!connection.getAutoCommit())
                    {
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

    /**
     * Executed automatically when the bean is initialized.
     */
    public void afterPropertiesSet() throws LiquibaseException {
        String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            System.out.println("LiquiBase did not run because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

        Connection c = null;
        try {
            c = getDataSource().getConnection();
            Liquibase liquibase = createLiquibase(c);

            liquibase.update(getContexts());
        } catch (SQLException e) {
            throw new DatabaseException(e);
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
        return new Liquibase(getChangeLog(), createResourceOpener(), createDatabase(c));
    }

    /**
     * Subclasses may override this method add change some database settings such as
     * default schema before returning the database object.
     * @param c
     * @return a Database implementation retrieved from the {@link DatabaseFactory}.
     * @throws DatabaseException
     */
    protected Database createDatabase(Connection c) throws DatabaseException {
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(c) );
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

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    @Override
    public String toString() {
        return getClass().getName()+"("+this.getResourceLoader().toString()+")";
    }
}
