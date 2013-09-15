/**
 * 
 */
package liquibase.integration.spring;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.sql.DataSource;

import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

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
 *	&lt;property name="jndiBase" value="java:comp/env/jdbc/db" /&gt;
 *	&lt;property name="changeLog" value="classpath:db/migration/db-changelog.xml" /&gt;	
 * &lt;/bean&gt;
 * </pre>
 * 
 * @see SpringLiquibase
 * 
 * @author ladislav.gazo
 */
public class MultiTenantSpringLiquibase implements InitializingBean, ResourceLoaderAware {
	private Logger log = LogFactory.getLogger(MultiTenantSpringLiquibase.class.getName());

	/** Defines the location of data sources suitable for multi-tenant environment. */
	private String jndiBase;
	private final List<DataSource> dataSources = new ArrayList<DataSource>();

	private ResourceLoader resourceLoader;
	
    private String changeLog;

    private String contexts;

    private Map<String, String> parameters;

    private String defaultSchema;

    private boolean dropFirst = false;

    private boolean shouldRun = true;

    private File rollbackFile;
	

	@Override
	public void afterPropertiesSet() throws Exception {
		resolveDataSources();
		runOnAllDataSources();
	}

	private void resolveDataSources() throws NamingException {
		Context context = new InitialContext();
		int lastIndexOf = jndiBase.lastIndexOf("/");
		String jndiRoot = jndiBase.substring(0, lastIndexOf);
		String jndiParent = jndiBase.substring(lastIndexOf + 1);
		Context base = (Context) context.lookup(jndiRoot);
		NamingEnumeration<NameClassPair> list = base.list(jndiParent);
		while(list.hasMoreElements()) {
			NameClassPair entry = list.nextElement();
			String name = entry.getName();
			String jndiUrl;
			if(entry.isRelative()) {
				jndiUrl = jndiBase + "/" + name;
			} else {
				jndiUrl = name;
			}
			
			Object lookup = context.lookup(jndiUrl);
			if(lookup instanceof DataSource) {
				dataSources.add((DataSource) lookup);
				log.debug("Added a data source at " + jndiUrl);
			} else {
				log.info("Skipping a resource " + jndiUrl + " not compatible with DataSource.");
			}
		}
	}

	private void runOnAllDataSources() throws LiquibaseException {
		for(DataSource dataSource : dataSources) {
			log.debug("Initializing Liquibase for data source " + dataSource);
			SpringLiquibase liquibase = new SpringLiquibase();
			liquibase.setChangeLog(changeLog);
			liquibase.setChangeLogParameters(parameters);
			liquibase.setContexts(contexts);
			liquibase.setDefaultSchema(defaultSchema);
			liquibase.setDropFirst(dropFirst);
			liquibase.setShouldRun(shouldRun);
			liquibase.setRollbackFile(rollbackFile);
			
			liquibase.setResourceLoader(resourceLoader);
			
			liquibase.setDataSource(dataSource);

			liquibase.afterPropertiesSet();
			log.info("Liquibase ran for data source " + dataSource);
		}
	}

	
	public String getJndiBase() {
		return jndiBase;
	}

	public void setJndiBase(String jndiBase) {
		this.jndiBase = jndiBase;
	}

	public String getChangeLog() {
		return changeLog;
	}

	public void setChangeLog(String changeLog) {
		this.changeLog = changeLog;
	}

	public String getContexts() {
		return contexts;
	}

	public void setContexts(String contexts) {
		this.contexts = contexts;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public String getDefaultSchema() {
		return defaultSchema;
	}

	public void setDefaultSchema(String defaultSchema) {
		this.defaultSchema = defaultSchema;
	}

	public boolean isDropFirst() {
		return dropFirst;
	}

	public void setDropFirst(boolean dropFirst) {
		this.dropFirst = dropFirst;
	}

	public boolean isShouldRun() {
		return shouldRun;
	}

	public void setShouldRun(boolean shouldRun) {
		this.shouldRun = shouldRun;
	}

	public File getRollbackFile() {
		return rollbackFile;
	}

	public void setRollbackFile(File rollbackFile) {
		this.rollbackFile = rollbackFile;
	}

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	
}
