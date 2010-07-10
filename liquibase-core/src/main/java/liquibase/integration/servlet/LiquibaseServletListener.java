package liquibase.integration.servlet;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.NetUtil;
import liquibase.util.StringUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Enumeration;

/**
 * Servlet listener than can be added to web.xml to allow Liquibase to run on every application server startup.
 * Using this listener allows users to know that they always have the most up to date database, although it will
 * slow down application server startup slightly.
 * See the <a href="http://www.liquibase.org/manual/latest/servlet_listener_migrator.html">Liquibase documentation</a> for
 * more information.
 */
public class LiquibaseServletListener implements ServletContextListener {

    private String changeLogFile;
    private String dataSource;
    private String contexts;
    private String defaultSchema;


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
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        String hostName;
        try {
            hostName = NetUtil.getLocalHost().getHostName();
        } catch (Exception e) {
            servletContextEvent.getServletContext().log("Cannot find hostname: " + e.getMessage());
            return;
        }

        String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            LogFactory.getLogger().info("Liquibase did not run on " + hostName + " because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

        String machineIncludes = servletContextEvent.getServletContext().getInitParameter("liquibase.host.includes");
        String machineExcludes = servletContextEvent.getServletContext().getInitParameter("liquibase.host.excludes");
        String failOnError = servletContextEvent.getServletContext().getInitParameter("liquibase.onerror.fail");

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

        if (!shouldRun) {
            servletContextEvent.getServletContext().log("LiquibaseServletListener did not run due to liquibase.host.includes and/or liquibase.host.excludes");
            return;
        }


        setDataSource(servletContextEvent.getServletContext().getInitParameter("liquibase.datasource"));
        setChangeLogFile(servletContextEvent.getServletContext().getInitParameter("liquibase.changelog"));
        setContexts(servletContextEvent.getServletContext().getInitParameter("liquibase.contexts"));
        this.defaultSchema = StringUtils.trimToNull(servletContextEvent.getServletContext().getInitParameter("liquibase.schema.default"));
        if (getChangeLogFile() == null) {
            throw new RuntimeException("Cannot run Liquibase, liquibase.changelog is not set");
        }
        if (getDataSource() == null) {
            throw new RuntimeException("Cannot run Liquibase, liquibase.datasource is not set");
        }

        try {
            Context ic = null;
            Connection connection = null;
            try {
                ic = new InitialContext();
                DataSource dataSource = (DataSource) ic.lookup(this.dataSource);

                connection = dataSource.getConnection();

                ResourceAccessor clFO = new ClassLoaderResourceAccessor();
                ResourceAccessor fsFO = new FileSystemResourceAccessor();


                Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));
                database.setDefaultSchemaName(this.defaultSchema);
                Liquibase liquibase = new Liquibase(getChangeLogFile(), new CompositeResourceAccessor(clFO,fsFO), database);

                Enumeration<String> initParameters = servletContextEvent.getServletContext().getInitParameterNames();
                while (initParameters.hasMoreElements()) {
                    String name = initParameters.nextElement().trim();
                    if (name.startsWith("liquibase.parameter.")) {
                        liquibase.setChangeLogParameter(name.substring("liquibase.parameter".length()), servletContextEvent.getServletContext().getInitParameter(name));
                    }
                }

                liquibase.update(getContexts());
            } finally {
                if (ic != null) {
                    ic.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

        } catch (Exception e) {
            if (!"false".equals(failOnError)) {
                throw new RuntimeException(e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

}
