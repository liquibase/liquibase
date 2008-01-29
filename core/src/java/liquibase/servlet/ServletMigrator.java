package liquibase.servlet;

import liquibase.*;
import liquibase.database.DatabaseFactory;
import liquibase.log.LogFactory;
import liquibase.Liquibase;

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

/**
 * @deprecated in favor of LiqubaseServletListener
 */
public class ServletMigrator extends LiquibaseServletListener {

}