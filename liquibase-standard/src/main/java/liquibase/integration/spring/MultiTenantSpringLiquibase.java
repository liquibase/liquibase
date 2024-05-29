/**
 *
 */
package liquibase.integration.spring;

import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.logging.Logger;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.naming.*;
import javax.sql.DataSource;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A wrapper of Liquibase suitable in multi-tenant environments where multiple
 * data sources represent tenants. It utilizes {@link SpringLiquibase} per each
 * data source. All the parameters are the same as for {@link SpringLiquibase}
 * except of the data source definition - in this case it is a list of data
 * sources available under specified JNDI subtree. You have to define the
 * subtree with {@link #jndiBase} property.<br/>
 * <br/>
 * The wrapper scans the subtree for all data sources and creates
 * {@link SpringLiquibase} instances.<br/>
 * <br/>
 * Example:<br/>
 * <br/><pre>
 * &lt;bean id="liquibase" class="liquibase.integration.spring.MultiTenantSpringLiquibase"&gt;
 * 	&lt;property name="jndiBase" value="java:comp/env/jdbc/db" /&gt;
 * 	&lt;property name="changeLog" value="classpath:db/migration/db-changelog.xml" /&gt;
 * &lt;/bean&gt;
 * </pre>
 *
 * @author ladislav.gazo
 * @see SpringLiquibase
 */
public class MultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {
    private final List<DataSource> dataSources = new ArrayList<>();

    /**
     * Defines the location of data sources suitable for multi-tenant environment.
     */
    @Getter
    @Setter
    private String jndiBase;

    /**
     * Defines a single data source and several schemas for a multi-tenant environment.
     */
    @Getter
    @Setter
    private DataSource dataSource;

    @Getter
    @Setter
    private List<String> schemas;

    private ResourceLoader resourceLoader;

    @Getter
    @Setter
    private String changeLog;

    @Getter
    @Setter
    private String contexts;

    @Getter
    @Setter
    private String labelFilter;

    @Getter
    @Setter
    private Map<String, String> parameters;

    @Getter
    @Setter
    private String defaultSchema;

    @Getter
    @Setter
    private String liquibaseSchema;

    @Getter
    @Setter
    private String liquibaseTablespace;

    @Getter
    @Setter
    private String databaseChangeLogTable;

    @Getter
    @Setter
    private String databaseChangeLogLockTable;

    @Getter
    @Setter
    private boolean dropFirst;

    @Getter
    @Setter
    private boolean clearCheckSums;

    @Getter
    @Setter
    private boolean shouldRun = true;

    @Getter
    @Setter
    private File rollbackFile;


    @Override
    public void afterPropertiesSet() throws Exception {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        if ((dataSource != null) || (schemas != null)) {
            if ((dataSource == null) && (schemas != null)) {
                throw new LiquibaseException("When schemas are defined you should also define a base dataSource");
            } else if (dataSource != null) {
                log.info("Schema based multitenancy enabled");
                if ((schemas == null) || schemas.isEmpty()) {
                    log.warning("Schemas not defined, using defaultSchema only");
                    schemas = new ArrayList<>();
                    schemas.add(defaultSchema);
                }
                runOnAllSchemas();
            }
        } else {
            log.info("DataSources based multitenancy enabled");
            resolveDataSources();
            runOnAllDataSources();
        }
    }

    private void resolveDataSources() throws NamingException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        Context context = new InitialContext();
        int lastIndexOf = jndiBase.lastIndexOf("/");
        String jndiRoot = jndiBase.substring(0, lastIndexOf);
        String jndiParent = jndiBase.substring(lastIndexOf + 1);
        Context base = (Context) context.lookup(jndiRoot);
        NamingEnumeration<NameClassPair> list = base.list(jndiParent);
        while (list.hasMoreElements()) {
            NameClassPair entry = list.nextElement();
            String name = entry.getName();
            String jndiUrl;
            if (entry.isRelative()) {
                jndiUrl = jndiBase + "/" + name;
            } else {
                jndiUrl = name;
            }

            Object lookup = context.lookup(jndiUrl);
            if (lookup instanceof DataSource) {
                dataSources.add((DataSource) lookup);
                log.fine("Added a data source at " + jndiUrl);
            } else {
                log.info("Skipping a resource " + jndiUrl + " not compatible with DataSource.");
            }
        }
    }

    private void runOnAllDataSources() throws LiquibaseException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        for (DataSource aDataSource : dataSources) {
            log.info("Initializing Liquibase for data source " + aDataSource);
            SpringLiquibase liquibase = getSpringLiquibase(aDataSource);
            liquibase.afterPropertiesSet();
            log.info("Liquibase ran for data source " + aDataSource);
        }
    }

    private void runOnAllSchemas() throws LiquibaseException {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        for (String schema : schemas) {
            if ("default".equals(schema)) {
                schema = null;
            }
            log.info("Initializing Liquibase for schema " + schema);
            SpringLiquibase liquibase = getSpringLiquibase(dataSource);
            liquibase.setDefaultSchema(schema);
            liquibase.afterPropertiesSet();
            log.info("Liquibase ran for schema " + schema);
        }
    }

    private SpringLiquibase getSpringLiquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setChangeLog(changeLog);
        liquibase.setChangeLogParameters(parameters);
        liquibase.setContexts(contexts);
        liquibase.setLabelFilter(labelFilter);
        liquibase.setDropFirst(dropFirst);
        liquibase.setClearCheckSums(clearCheckSums);
        liquibase.setShouldRun(shouldRun);
        liquibase.setRollbackFile(rollbackFile);
        liquibase.setResourceLoader(resourceLoader);
        liquibase.setDataSource(dataSource);
        liquibase.setDefaultSchema(defaultSchema);
        liquibase.setLiquibaseSchema(liquibaseSchema);
        liquibase.setLiquibaseTablespace(liquibaseTablespace);
        liquibase.setDatabaseChangeLogTable(databaseChangeLogTable);
        liquibase.setDatabaseChangeLogLockTable(databaseChangeLogLockTable);
        return liquibase;
    }

    /**
     * @deprecated use {@link #getLabelFilter()}
     */
    @Deprecated
    public String getLabels() {
        return getLabelFilter();
    }

    /**
     * @deprecated use {@link #setLabelFilter(String)}
     */
    @Deprecated
    public void setLabels(String labels) {
        setLabelFilter(labels);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

}
