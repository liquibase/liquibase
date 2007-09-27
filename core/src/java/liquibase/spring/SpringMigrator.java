package liquibase.spring;

import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.migrator.Migrator;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.TimeZone;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A Spring-ified wrapper for the Liquibase Migrator.
 *
 * Example Configuration:
 * <p>
 * <p>
 * This Spring configuration example will cause the migrator to run
 * automatically when the Spring context is initialized. It will load
 * <code>db-changelog.xml</code> from the classpath and apply it against
 * <code>myDataSource</code>.
 * <p>
 *
 * <pre>
 * &lt;bean id=&quot;myMigrator&quot;
 *          class=&quot;liquibase.spring.SpringMigrator&quot;
 *          &gt;
 *
 *      &lt;property name=&quot;dataSource&quot; ref=&quot;myDataSource&quot; /&gt;
 *
 *      &lt;property name=&quot;changeLogResource&quot; value=&quot;classpath:db-changelog.xml&quot; /&gt;
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
 *
 */
public class SpringMigrator implements InitializingBean, BeanNameAware {


    public class SpringResourceOpener implements FileOpener {

        public InputStream getResourceAsStream(String arg0) throws IOException {
            return getChangeLogResource().getInputStream();
        }

        public Enumeration<URL> getResources(String arg0) throws IOException {
            Vector<URL> tmp = new Vector<URL>();
            tmp.add(getChangeLogResource().getURL());
            return tmp.elements();
        }

    }

    private String beanName;

    private DataSource dataSource;

    private boolean executeEnabled = true;

    private boolean writeSqlFileEnabled = true;

    private Logger log = Logger.getLogger(SpringMigrator.class.getName());

    private Resource changeLogResource;

    private File sqlOutputDir;

    public SpringMigrator() {
        super();
    }

    public String getDatabaseProductName() throws JDBCException {
        Connection connection = null;
        String name = "unknown";
        try {
            connection = getDataSource().getConnection();
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(dataSource.getConnection());
            name = database.getDatabaseProductName();
        } catch (SQLException e) {
            throw new JDBCException(e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    log.log(Level.WARNING, "problem closing connection", e);
                }
            }
        }
        return name;
    }

    /**
     * The DataSource that the migrator will use to perform the migration.
     *
     * @return
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * The DataSource that the migrator will use to perform the migration.
     */
    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Determines whether the migrator will actually execute DDL statements.
     *
     * @param executeSql
     */
    public void setExecuteEnabled(boolean executeSql) {
        this.executeEnabled = executeSql;
    }

    /**
     * Determines whether the migrator will actually execute DDL statements.
     */
    public boolean isExecuteEnabled() {
        return executeEnabled;
    }

    /**
     * Returns a warning message that will be written out to the log.  It is here so that it
     * can be customized.
     *
     * @return
     */
    public String getExecuteDisabledWarningMessage() {
        return "\n\nYou have set "
                + getBeanName()
                + ".executeEnabled=false, but there are \n"
                + "database change sets that need to be run in order to bring the database up to date.\n"
                + "The application may not behave properly until these changes are run.\n\n";

    }

    /**
     * Returns the output directory into which a SQL file will be written *if*
     * writeSqlFileEnabled is set to true.
     *
     * @return
     */
    public File getSqlOutputDir() {
        return sqlOutputDir;
    }

    /**
     * Sets the output directory into which a SQL file will be written if
     * writeSqlFileEnabled is also set to true.
     *
     * @param sqlOutputDir
     */
    public void setSqlOutputDir(File sqlOutputDir) {
        this.sqlOutputDir = sqlOutputDir;
    }

    /**
     * Returns a Resource that is able to resolve to a file or classpath resource.
     *
     * @return
     */
    public Resource getChangeLogResource() {
        return changeLogResource;
    }

    /**
     * Sets a Spring Resource that is able to resolve to a file or classpath resource.
     * An example might be <code>classpath:db-changelog.xml</code>.
     */
    public void setChangeLogResource(Resource dataModel) {

        this.changeLogResource = dataModel;
    }

    /**
     * Executed automatically when the bean is initialized.
     */
    public void afterPropertiesSet() throws LiquibaseException {
        Connection c = null;
        try {
            c = getDataSource().getConnection();
            Migrator migrator = createMigrator(c);

            migrator.init(c);

            // First, run patchChangeLog() which allows md5sum values to be set to NULL
            setup(migrator);

            // Now, write out the SQL file for the changes if so configured
            if (isWriteSqlFileEnabled() && getSqlOutputDir() != null) {
                if (migrator.listUnrunChangeSets().size() > 0) {
                    log.log(Level.WARNING, getExecuteDisabledWarningMessage());
                }
                writeSqlFile(migrator);
            }

            // Now execute the DDL, if so configured
            if (isExecuteEnabled()) {
                executeSql(migrator);
            }
        } catch (SQLException e) {
            throw new JDBCException(e);
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            if (c != null) {
                try {
                    c.close();
                } catch (SQLException e) {
                    ;
                }
            }
        }

    }

    /**
     * Subclasses can override this method and make calls to eraseChangeLogMD5Sum() as
     * necessary.
     *
     * @throws IOException
     * @throws SQLException
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private void setup(Migrator migrator) {

    }

    /**
     * If there are any un-run changesets, this method will write them out to a file, if
     * so configured.
     *
     * @param migrator
     * @throws SQLException
     * @throws IOException
     * @throws LiquibaseException
     */
    protected void writeSqlFile(Migrator migrator) throws LiquibaseException {
        FileWriter fw = null;
        try {

            File ddlDir = getSqlOutputDir();
            ddlDir.mkdirs();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String dateString = sdf.format(new java.util.Date());

            String fileString = (getDatabaseProductName() + "-" + dateString + ".sql").replace(" ", "-").toLowerCase();
            File upgradeFile = new File(ddlDir, fileString);
            fw = new FileWriter(upgradeFile);

            migrator.setMode(Migrator.Mode.OUTPUT_SQL_MODE);
            migrator.setOutputSQLWriter(fw);
            migrator.migrate();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (IOException e) {
                    log.log(Level.SEVERE, "error closing fileWriter", e);
                }
            }
        }
    }

    private Migrator createMigrator(Connection c) throws IOException, JDBCException {
        Migrator m = new Migrator(getChangeLogResource().getURL().toString(), new SpringResourceOpener());
        m.init(c);
        return m;
    }

    /**
     * This method will actually execute the changesets.
     *
     * @param migrator
     * @throws SQLException
     * @throws IOException
     */
    protected void executeSql(Migrator migrator) throws LiquibaseException {
        migrator.setMode(Migrator.Mode.EXECUTE_MODE);
        migrator.migrate();
    }

    /**
     * Boolean flag to determine whether generated SQL should be written to disk.
     *
     * @return
     */
    public boolean isWriteSqlFileEnabled() {
        return writeSqlFileEnabled;
    }

    /**
     * Boolean flag to determine whether generated SQL should be written to disk.
     */
    public void setWriteSqlFileEnabled(boolean writeSqlFile) {
        this.writeSqlFileEnabled = writeSqlFile;
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

}
