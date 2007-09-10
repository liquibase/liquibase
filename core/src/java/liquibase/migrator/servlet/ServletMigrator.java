package liquibase.migrator.servlet;

import liquibase.migrator.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @deprecated use liquibase.servlet.ServletMigrator
 */
public class ServletMigrator extends liquibase.servlet.ServletMigrator {
}
