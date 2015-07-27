package liquibase.integration.hibernate;

import java.util.Map;

/**
 * Holds the configuration for Liquibase
 * 
 * @author Tobias Soloschenko
 *
 */
public class LiquibaseHibernateIntegratorConfig {

    private String contexts;

    private String labels;

    private String changeLog;

    private Map<String, String> parameters;

    private boolean dropFirst;

    private String defaultSchema;

    private boolean dropAtShutdown;

    private boolean multiProjectSetup;

    public String getContexts() {
	return contexts;
    }

    public void setContexts(String contexts) {
	this.contexts = contexts;
    }

    public String getLabels() {
	return labels;
    }

    public void setLabels(String labels) {
	this.labels = labels;
    }

    public String getChangeLog() {
	return changeLog;
    }

    public void setChangeLog(String changeLog) {
	this.changeLog = changeLog;
    }

    public Map<String, String> getParameters() {
	return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
	this.parameters = parameters;
    }

    public boolean isDropFirst() {
	return dropFirst;
    }

    public void setDropFirst(boolean dropFirst) {
	this.dropFirst = dropFirst;
    }

    public String getDefaultSchema() {
	return defaultSchema;
    }

    public void setDefaultSchema(String defaultSchema) {
	this.defaultSchema = defaultSchema;
    }

    public boolean isDropAtShutdown() {
	return dropAtShutdown;
    }

    public void setDropAtShutdown(boolean dropAtShutdown) {
	this.dropAtShutdown = dropAtShutdown;
    }

    public void setMultiProjectSetup(boolean multiProjectSetup) {
	this.multiProjectSetup = multiProjectSetup;
    }

    public boolean isMultiProjectSetup() {
	return multiProjectSetup;
    }
}
