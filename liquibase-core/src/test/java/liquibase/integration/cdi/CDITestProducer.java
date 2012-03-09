package liquibase.integration.cdi;

import liquibase.integration.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.hsqldb.jdbc.jdbcDataSource;
import org.jboss.weld.resources.ClassLoaderResourceLoader;

import javax.enterprise.inject.Produces;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * A Test CDI Producer used for testing CDILiquibase
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDITestProducer {

    @Produces @LiquibaseType
    public CDILiquibaseConfig createConfig() {
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog("liquibase/parser/core/xml/simpleChangeLog.xml");
        return config;
    }

    @Produces @LiquibaseType
    public DataSource createDataSource() throws SQLException {
        jdbcDataSource ds = new jdbcDataSource();
        ds.setDatabase("jdbc:hsqldb:mem:test");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Produces @LiquibaseType
    public ResourceAccessor create() {
        return new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }

}
