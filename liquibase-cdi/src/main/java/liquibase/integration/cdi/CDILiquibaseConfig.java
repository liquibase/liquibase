package liquibase.integration.cdi;

import liquibase.resource.ResourceAccessor;

import java.util.Map;

/**
 * Holds the configuration for Liquibase
 *
 *  @author Aaron Walker (http://github.com/aaronwalker)
 */
public class CDILiquibaseConfig {

    private String contexts;
    private String changeLog;
    private Map<String,String> parameters;
    private boolean dropFirst = false;
    private String defaultSchema;

    public String getContexts() {
        return contexts;
    }

    public void setContexts(String contexts) {
        this.contexts = contexts;
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
}
