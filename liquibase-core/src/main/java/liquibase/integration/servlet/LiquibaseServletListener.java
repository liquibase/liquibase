package liquibase.integration.servlet;

import liquibase.*;
import liquibase.configuration.*;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.NetUtil;
import liquibase.util.StringUtil;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Servlet listener than can be added to web.xml to allow Liquibase to run on every application server startup.
 * Using this listener allows users to know that they always have the most up to date database, although it will
 * slow down application server startup slightly.
 * See the <a href="http://www.liquibase.org/documentation/servlet_listener.html">Liquibase documentation</a> for
 * more information.
 */
public class LiquibaseServletListener implements ServletContextListener {

    private static final String LIQUIBASE_CHANGELOG = "liquibase.changelog";
    private static final String LIQUIBASE_CONTEXTS = "liquibase.contexts";
    private static final String LIQUIBASE_LABELS = "liquibase.labels";
    private static final String LIQUIBASE_DATASOURCE = "liquibase.datasource";
    private static final String LIQUIBASE_HOST_EXCLUDES = "liquibase.host.excludes";
    private static final String LIQUIBASE_HOST_INCLUDES = "liquibase.host.includes";
    private static final String LIQUIBASE_ONERROR_FAIL = "liquibase.onerror.fail";
    private static final String LIQUIBASE_PARAMETER = "liquibase.parameter";
    private static final String LIQUIBASE_SCHEMA_DEFAULT = "liquibase.schema.default";

    private String changeLogFile;
    private String dataSourceName;
    private String contexts;
    private String labels;
    private String defaultSchema;
    private String hostName;

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String ctxt) {
        contexts = ctxt;
    }

    public String getLabels() {
        return labels;
    }

    public void setLabels(String labels) {
        this.labels = labels;
    }

    public String getDataSource() {
        return dataSourceName;
    }

    /**
     * Sets the name of the data source.
     */
    public void setDataSource(String dataSource) {
        this.dataSourceName = dataSource;
    }

    public String getDefaultSchema() {
        return defaultSchema;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServletContext servletContext = servletContextEvent.getServletContext();
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        try {
            this.hostName = NetUtil.getLocalHostName();
        } catch (Exception e) {
            servletContext.log("Cannot find hostname: " + e.getMessage());
            return;
        }

        InitialContext ic = null;
        String failOnError = null;
        final ServletConfigurationValueProvider servletConfigurationValueProvider = new ServletConfigurationValueProvider(servletContext, ic);
        try {
            ic = new InitialContext();

            liquibaseConfiguration.registerProvider(servletConfigurationValueProvider);

            failOnError = (String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_ONERROR_FAIL).getValue();
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
                } catch (NamingException e) {
                    // ignore
                }
            }
            liquibaseConfiguration.removeProvider(servletConfigurationValueProvider);


        }
    }

    /**
     * Checks if the update is supposed to be executed. That depends on several conditions:
     * <ol>
     * <li>if liquibase.shouldRun is <code>false</code> the update will not be executed.</li>
     * <li>if {@value LiquibaseServletListener#LIQUIBASE_HOST_INCLUDES} contains the current hostname, the the update will be executed.</li>
     * <li>if {@value LiquibaseServletListener#LIQUIBASE_HOST_EXCLUDES} contains the current hostname, the the update will not be executed.</li>
     * </ol>
     */
    private boolean checkPreconditions(ServletContext servletContext, InitialContext ic) {
        if (!liquibase.GlobalConfiguration.SHOULD_RUN.getCurrentValue()) {
            Scope.getCurrentScope().getLog(getClass()).info("Liquibase did not run on " + hostName
                    + " because " + liquibase.GlobalConfiguration.SHOULD_RUN.getKey()
                    + " was set to false");
            return false;
        }

        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        String machineIncludes = (String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_HOST_INCLUDES).getValue();
        String machineExcludes = (String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_HOST_EXCLUDES).getValue();

        boolean shouldRun = false;
        if ((machineIncludes == null) && (machineExcludes == null)) {
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

        final ConfiguredValue<Boolean> shouldRunValue = liquibase.GlobalConfiguration.SHOULD_RUN.getCurrentConfiguredValue();
        if (liquibase.GlobalConfiguration.SHOULD_RUN.getCurrentValue() && !ConfigurationDefinition.wasDefaultValueUsed(shouldRunValue)) {
            shouldRun = true;
            servletContext.log("ignoring " + LIQUIBASE_HOST_INCLUDES + " and "
                    + LIQUIBASE_HOST_EXCLUDES + ", since " + shouldRunValue.getProvidedValue().describe()
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
    @java.lang.SuppressWarnings("squid:S2095")
    private void executeUpdate(ServletContext servletContext, InitialContext ic) throws NamingException, SQLException, LiquibaseException {
        final LiquibaseConfiguration liquibaseConfiguration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class);

        setDataSource((String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_DATASOURCE).getValue());
        if (getDataSource() == null) {
            throw new RuntimeException("Cannot run Liquibase, " + LIQUIBASE_DATASOURCE + " is not set");
        }

        setChangeLogFile((String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_CHANGELOG).getValue());
        if (getChangeLogFile() == null) {
            throw new RuntimeException("Cannot run Liquibase, " + LIQUIBASE_CHANGELOG + " is not set");
        }

        setContexts((String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_CONTEXTS).getValue());
        setLabels((String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_LABELS).getValue());
        this.defaultSchema = StringUtil.trimToNull((String) liquibaseConfiguration.getCurrentConfiguredValue(LIQUIBASE_SCHEMA_DEFAULT).getValue());

        Connection connection = null;
        Database database = null;
        Liquibase liquibase = null;
        try {
            DataSource dataSource = (DataSource) ic.lookup(this.dataSourceName);

            connection = dataSource.getConnection();

            Thread currentThread = Thread.currentThread();
            ClassLoader contextClassLoader = currentThread.getContextClassLoader();
            ResourceAccessor threadClFO = new ClassLoaderResourceAccessor(contextClassLoader);

            ResourceAccessor clFO = new ClassLoaderResourceAccessor();
            ResourceAccessor fsFO = new FileSystemResourceAccessor();


            database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(getDefaultSchema());
            liquibase = new Liquibase(getChangeLogFile(), new CompositeResourceAccessor(clFO, fsFO, threadClFO), database);

            @SuppressWarnings("unchecked")
            Enumeration<String> initParameters = servletContext.getInitParameterNames();
            while (initParameters.hasMoreElements()) {
                String name = initParameters.nextElement().trim();
                if (name.startsWith(LIQUIBASE_PARAMETER + ".")) {
                    liquibase.setChangeLogParameter(name.substring(LIQUIBASE_PARAMETER.length() + 1), liquibaseConfiguration.getCurrentConfiguredValue(name));
                }
            }

            liquibase.update(new Contexts(getContexts()), new LabelExpression(getLabels()));
            if (database instanceof DerbyDatabase) {
                ((DerbyDatabase) database).setShutdownEmbeddedDerby(false);
            }
        } finally {
            if (liquibase != null) {
                liquibase.close();
            } else if (connection != null) {
                connection.close();
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

}
