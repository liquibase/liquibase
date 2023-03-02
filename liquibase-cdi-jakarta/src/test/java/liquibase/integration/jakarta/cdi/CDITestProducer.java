package liquibase.integration.jakarta.cdi;

import javax.sql.DataSource;

import org.h2.jdbcx.JdbcDataSource;

import jakarta.enterprise.inject.Produces;
import liquibase.integration.jakarta.cdi.annotations.LiquibaseType;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;

/**
 * A Test CDI Producer used for testing CDILiquibase
 *
 * @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDITestProducer {

    @Produces
    @LiquibaseType
    public CDILiquibaseConfig createConfig() {
        CDILiquibaseConfig config = new CDILiquibaseConfig();
        config.setChangeLog("liquibase/parser/core/xml/simpleChangeLog.xml");
        boolean configShouldRun = Boolean.valueOf(System.getProperty("liquibase.config.shouldRun", "true"));
        config.setShouldRun(configShouldRun);
        return config;
    }

    @Produces
    @LiquibaseType
    public DataSource createDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        ds.setUser("sa");
        ds.setPassword("sa");
        return ds;
    }

    @Produces
    @LiquibaseType
    public ResourceAccessor create() {
        return new ClassLoaderResourceAccessor(getClass().getClassLoader());
    }

}
