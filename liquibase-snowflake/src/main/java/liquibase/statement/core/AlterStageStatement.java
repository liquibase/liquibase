package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL Statement for ALTER STAGE operations.
 * Uses generic property storage following professional pattern.
 */
public class AlterStageStatement extends AbstractSqlStatement {

    // Core required properties
    private String stageName;
    
    // PROFESSIONAL PATTERN: Generic property storage
    private Map<String, String> objectProperties = new HashMap<>();
    
    public AlterStageStatement() {
        super();
    }
    
    public AlterStageStatement(String stageName) {
        this.stageName = stageName;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    // PROFESSIONAL PATTERN: Generic property methods
    public void setObjectProperty(String propertyName, String propertyValue) {
        if (propertyValue != null) {
            objectProperties.put(propertyName, propertyValue);
        }
    }
    
    public String getObjectProperty(String propertyName) {
        return objectProperties.get(propertyName);
    }
    
    public void setObjectProperties(Map<String, String> properties) {
        if (properties != null) {
            this.objectProperties = new HashMap<>(properties);
        }
    }
    
    public Map<String, String> getObjectProperties() {
        return new HashMap<>(objectProperties);
    }
    
    // Convenience getters for common properties
    public String getCatalogName() { return objectProperties.get("catalogName"); }
    public String getSchemaName() { return objectProperties.get("schemaName"); }
    public Boolean getIfExists() { 
        String val = objectProperties.get("ifExists"); 
        return val != null ? Boolean.valueOf(val) : null; 
    }
    public String getRenameTo() { return objectProperties.get("renameTo"); }
    
    // Convenience setters for common properties
    public void setIfExists(Boolean ifExists) {
        if (ifExists != null) {
            objectProperties.put("ifExists", ifExists.toString());
        }
    }
    
    // Operation type detection helpers
    public boolean isRenameOperation() {
        return objectProperties.containsKey("renameTo");
    }
    
    public boolean hasSetOperations() {
        return objectProperties.entrySet().stream()
            .anyMatch(entry -> {
                String key = entry.getKey();
                // Check for actual property names that are NOT skipped in appendSetOperations
                return !key.startsWith("unset") && 
                       !key.equals("renameTo") && 
                       !key.equals("refreshDirectory") && 
                       !key.equals("refreshSubpath") && 
                       !key.equals("catalogName") && 
                       !key.equals("schemaName") && 
                       !key.equals("ifExists") && 
                       !key.equals("tagName") && 
                       !key.equals("tagValue");
            });
    }
    
    public boolean hasUnsetOperations() {
        return objectProperties.entrySet().stream()
            .anyMatch(entry -> entry.getKey().startsWith("unset"));
    }
    
    public boolean hasRefreshOperations() {
        return objectProperties.containsKey("refreshDirectory");
    }
    
    public boolean hasTagOperations() {
        return objectProperties.containsKey("tagName") || 
               objectProperties.containsKey("unsetTagName");
    }
}