package liquibase.integration.servlet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.NetUtil;
import liquibase.util.StringUtils;

/**
 * Servlet listener than can be added to web.xml to allow Liquibase to run on every application server startup.
 * Using this listener allows users to know that they always have the most up to date database, although it will
 * slow down application server startup slightly.
 * See the <a href="http://www.liquibase.org/documentation/servlet_listener.html">Liquibase documentation</a> for
 * more information.
 */
public class LiquibaseServletListener implements ServletContextListener {

    private static final String JAVA_COMP_ENV = "java:comp/env";
    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
    private static final String LIQUIBASE_CONTEXTS = "liquibase.contexts";
    private static final String LIQUIBASE_DATASOURCE = "liquibase.datasource";
    private static final String LIQUIBASE_HOST_EXCLUDES = "liquibase.host.excludes";
    private static final String LIQUIBASE_HOST_INCLUDES = "liquibase.host.includes";
    private static final String LIQUIBASE_ONERROR_FAIL = "liquibase.onerror.fail";
    private static final String LIQUIBASE_PARAMETER = "liquibase.parameter";
    private static final String LIQUIBASE_SCHEMA_DEFAULT = "liquibase.schema.default";

    private String changeLogFile;
    private String dataSourceName;
    private String contexts;
    private String defaultSchema;
    private String hostName;

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setContexts(String ctxt) {
        contexts = ctxt;
    }

    public String getContexts() {
        return contexts;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getDataSource() {
        return dataSourceName;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    /**
     * Sets the name of the data source.
     */
    public void setDataSource(String dataSource) {
        this.dataSourceName = dataSource;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        try {
            this.hostName = NetUtil.getLocalHost().getHostName();
        }
        catch (Exception e) {
            servletContext.log("Cannot find hostname: " + e.getMessage());
            return;
        }

        InitialContext ic = null;
        String failOnError = null;
        try {
            ic = new InitialContext();
            failOnError = getValue(LIQUIBASE_ONERROR_FAIL, servletContext, ic);
            if (checkPreconditions(servletContext, ic)) {
                executeUpdate(servletContext, ic);
            }

        } catch (Exception e) {
            if (!"false".equals(failOnError)) {
                throw new RuntimeException(e);
            }
        } finally {
            if (ic != null) {
                try {
                    ic.close();
                }
                catch (NamingException e) {
                    // ignore
                }
            }
        }
    }

    /**
     * Checks if the update is supposed to be executed. That depends on several conditions:
     * <ol>
     * <li>if {@value Liquibase#SHOULD_RUN_SYSTEM_PROPERTY} is <code>false</code> the update will not be executed.</li>
     * <li>if {@value LiquibaseServletListener#LIQUIBASE_HOST_INCLUDES} contains the current hostname, the the update will be executed.</li>
     * <li>if {@value LiquibaseServletListener#LIQUIBASE_HOST_EXCLUDES} contains the current hostname, the the update will not be executed.</li>
     * </ol>
     */
    private boolean checkPreconditions(ServletContext servletContext, InitialContext ic) {
        String shouldRunProperty = getValue(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY, servletContext, ic);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            LogFactory.getLogger().info(
                    "Liquibase did not run on " + hostName + " because '"
                            + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY
                            + "' system property was set to false");
            return false;
        }

        String machineIncludes = getValue(LIQUIBASE_HOST_INCLUDES, servletContext, ic);
        String machineExcludes = getValue(LIQUIBASE_HOST_EXCLUDES, servletContext, ic);

        boolean shouldRun = false;
        if (machineIncludes == null && machineExcludes == null) {
            shouldRun = true;
        } else if (machineIncludes != null) {
            for (String machine : machineIncludes.split(",")) {
                machine = machine.trim();
                if (hostName.equalsIgnoreCase(machine)) {
                    shouldRun = true;
                }
            }
        } else if (machineExcludes != null) {
            shouldRun = true;
            for (String machine : machineExcludes.split(",")) {
                machine = machine.trim();
                if (hostName.equalsIgnoreCase(machine)) {
                    shouldRun = false;
                }
            }
        }

        if (shouldRunProperty != null && Boolean.valueOf(shouldRunProperty)) {
            shouldRun = true;
            servletContext.log("ignoring " + LIQUIBASE_HOST_INCLUDES + " and "
                    + LIQUIBASE_HOST_EXCLUDES + ", since " + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY
                    + "=true");
        }
        if (!shouldRun) {
            servletContext.log("LiquibaseServletListener did not run due to "
                    + LIQUIBASE_HOST_INCLUDES + " and/or " + LIQUIBASE_HOST_EXCLUDES + "");
            return false;
        }
        return true;
    }

    /**
     * Executes the Liquibase update.
     */
    private void executeUpdate(ServletContext servletContext, InitialContext ic) throws NamingException, SQLException, LiquibaseException {
        setDataSource(getValue(LIQUIBASE_DATASOURCE, servletContext, ic));
        if (getDataSource() == null) {
            throw new RuntimeException("Cannot run Liquibase, " + LIQUIBASE_DATASOURCE + " is not set");
        }

        setChangeLogFile(getValue(LIQUIBASE_CHANGELOG, servletContext, ic));
        if (getChangeLogFile() == null) {
            throw new RuntimeException("Cannot run Liquibase, " + LIQUIBASE_CHANGELOG + " is not set");
        }

        setContexts(getValue(LIQUIBASE_CONTEXTS, servletContext, ic));
        this.defaultSchema = StringUtils.trimToNull(getValue(LIQUIBASE_SCHEMA_DEFAULT, servletContext, ic));

        Connection connection = null;
        try {
            DataSource dataSource = (DataSource) ic.lookup(this.dataSourceName);

            connection = dataSource.getConnection();

            Thread currentThread = Thread.currentThread();
            ClassLoader contextClassLoader = currentThread.getContextClassLoader();
            ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);

            ResourceAccessor clFO = new ClassLoaderResourceAccessor();
            ResourceAccessor fsFO = new FileSystemResourceAccessor();


            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(getDefaultSchema());
            Liquibase liquibase = new Liquibase(getChangeLogFile(), new CompositeResourceAccessor(clFO, fsFO, threadClFO), database);

            @SuppressWarnings("unchecked")
            Enumeration<String> initParameters = servletContext.getInitParameterNames();
            while (initParameters.hasMoreElements()) {
                String name = initParameters.nextElement().trim();
                if (name.startsWith(LIQUIBASE_PARAMETER + ".")) {
                    liquibase.setChangeLogParameter(name.substring(LIQUIBASE_PARAMETER.length()), getValue(name, servletContext, ic));
                }
            }

            liquibase.update(getContexts());
        }
        finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Try to read the value that is stored by the given key from
     * <ul>
     * <li>JNDI</li>
     * <li>the servlet context's init parameters</li>
     * <li>system properties</li>
     * </ul>
     */
    public String getValue(String key, ServletContext servletContext, InitialContext initialContext) {
        // Try to get value from JNDI
        try {
            Context envCtx = (Context) initialContext.lookup(JAVA_COMP_ENV);
            String valueFromJndi = (String) envCtx.lookup(key);
            return valueFromJndi;
        }
        catch (NamingException e) {
            // Ignore
        }

        // Return the value from the servlet context
        String valueFromServletContext = servletContext.getInitParameter(key);
        if (valueFromServletContext != null) {
            return valueFromServletContext;
        }

        // Otherwise: Return system property
        return System.getProperty(key);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

}