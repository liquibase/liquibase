package liquibase.servlet;

import liquibase.log.LogFactory;
import liquibase.*;
import liquibase.util.NetUtil;
import liquibase.database.DatabaseFactory;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;

/**
 * Servlet listener than can be added to web.xml to allow LiquiBase to run on every application server startup.
 * Using this listener allows users to know that they always have the most up to date database, although it will
 * slow down application server startup slightly.
 * See the <a href="http://www.liquibase.org/manual/latest/servlet_listener_migrator.html">LiquiBase documentation</a> for
 * more information.
 */
public class LiquibaseServletListener implements ServletContextListener {

    private String changeLogFile;
    private String dataSource;
    private String contexts;


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
        LogFactory.getLogger().addHandler(new Handler() {
            public synchronized void publish(LogRecord record) {
                LiquibaseStatusServlet.logMessage(record);
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        });
        String hostName;
        try {
            hostName = NetUtil.getLocalHost().getHostName();
        } catch (Exception e) {
            servletContextEvent.getServletContext().log("Cannot find hostname: " + e.getMessage());
            return;
        }

        String shouldRunProperty = System.getProperty(Liquibase.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            LogFactory.getLogger().info("LiquiBase did not run on " + hostName + " because '" + Liquibase.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

        String machineIncludes = servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_HOST_INCLUDES");
        String machineExcludes = servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_HOST_EXCLUDES");
        String failOnError = servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_FAIL_ON_ERROR");

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
            servletContextEvent.getServletContext().log("LiquibaseServletListener did not run due to LIQUIBASE_HOST_INCLUDES and/or LIQUIBASE_HOST_EXCLUDES");
            return;
        }


        setDataSource(servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_DATA_SOURCE"));
        setChangeLogFile(servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_CHANGELOG"));
        setContexts(servletContextEvent.getServletContext().getInitParameter("LIQUIBASE_CONTEXTS"));
        if (getChangeLogFile() == null) {
            throw new RuntimeException("Cannot run LiquiBase, LIQUIBASE_CHANGELOG is not set");
        }
        if (getDataSource() == null) {
            throw new RuntimeException("Cannot run LiquiBase, LIQUIBASE_DATA_SOURCE is not set");
        }

        try {
            Context ic = null;
            Connection connection = null;
            try {
                ic = new InitialContext();
                DataSource dataSource = (DataSource) ic.lookup(this.dataSource);

                connection = dataSource.getConnection();

                FileOpener clFO = new ClassLoaderFileOpener();
                FileOpener fsFO = new FileSystemFileOpener();


                Liquibase liquibase = new Liquibase(getChangeLogFile(), new CompositeFileOpener(clFO,fsFO), DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection));
                liquibase.update(getContexts());
            } finally {
                if (ic != null) {
                    ic.close();
                }
                if (connection != null) {
                    connection.close();
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
