package liquibase.migrator.servlet;

import liquibase.migrator.ClassLoaderFileOpener;
import liquibase.migrator.Migrator;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.sql.Connection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;
import java.util.logging.LogRecord;
import java.util.logging.Handler;

public class ServletMigrator implements ServletContextListener {

    private String migrationFile;
    private String dataSource;
    private String contexts;


    public String getMigrationFile() {
        return migrationFile;
    }

    public void setContexts(String ctxt) {
        contexts = ctxt;
    }

    public String getContexts() {
        return contexts;
    }

    public void setMigrationFile(String migrationFile) {
        this.migrationFile = migrationFile;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Logger.getLogger(Migrator.DEFAULT_LOG_NAME).addHandler(new Handler() {
            public synchronized void publish(LogRecord record) {
                MigratorStatusServlet.logMessage(record);
            }

            public void flush() {
                ;
            }

            public void close() throws SecurityException {
                ;
            }
        });
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            servletContextEvent.getServletContext().log("Cannot find hostname: " + e.getMessage());
            return;
        }

        String shouldRunProperty = System.getProperty(Migrator.SHOULD_RUN_SYSTEM_PROPERTY);
        if (shouldRunProperty != null && !Boolean.valueOf(shouldRunProperty)) {
            Logger.getLogger(Migrator.DEFAULT_LOG_NAME).info("Migrator did not run on " + hostName + " because '" + Migrator.SHOULD_RUN_SYSTEM_PROPERTY + "' system property was set to false");
            return;
        }

        String machineIncludes = servletContextEvent.getServletContext().getInitParameter("MIGRATOR_HOST_INCLUDES");
        String machineExcludes = servletContextEvent.getServletContext().getInitParameter("MIGRATOR_HOST_EXCLUDES");
        String failOnError = servletContextEvent.getServletContext().getInitParameter("MIGRATOR_FAIL_ON_ERROR");

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
            for (String machine : machineIncludes.split(",")) {
                machine = machine.trim();
                if (hostName.equalsIgnoreCase(machine)) {
                    shouldRun = false;
                }
            }
        }

        if (!shouldRun) {
            servletContextEvent.getServletContext().log("ServletMigrator did not run due to MIGRATOR_HOST_INCLUDES and/or MIGRATOR_HOST_EXCLUDES");
            return;
        }


        setDataSource(servletContextEvent.getServletContext().getInitParameter("MIGRATOR_DATA_SOURCE"));
        setMigrationFile(servletContextEvent.getServletContext().getInitParameter("MIGRATOR_FILE"));
        setContexts(servletContextEvent.getServletContext().getInitParameter("MIGRATOR_CONTEXTS"));
        if (getMigrationFile() == null) {
            throw new RuntimeException("Cannot run migrator, MIGRATOR_FILE is not set");
        }
        if (getDataSource() == null) {
            throw new RuntimeException("Cannot run migrator, MIGRATOR_DATA_SOURCE is not set");
        }

        try {
            Context ic = null;
            Connection connection = null;
            try {
                ic = new InitialContext();
                DataSource dataSource = (DataSource) ic.lookup(this.dataSource);

                connection = dataSource.getConnection();
                Migrator migrator = new Migrator(getMigrationFile(), new ClassLoaderFileOpener());
                migrator.init(connection);
                migrator.setContexts(getContexts());
                migrator.migrate();
            } finally {
                if (ic != null) {
                    ic.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }

        } catch (Exception e) {
            if ("false".equals(failOnError)) {
                return;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

}