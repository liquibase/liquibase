package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL Statement for CREATE STAGE operations.
 * Uses generic property storage following professional pattern.
 */
public class CreateStageStatement extends AbstractSqlStatement {

    // Core required properties
    private String stageName;
    
    // PROFESSIONAL PATTERN: Generic property storage
    private Map<String, String> objectProperties = new HashMap<>();
    
    public CreateStageStatement() {
        super();
    }
    
    public CreateStageStatement(String stageName) {
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
    public String getUrl() { return objectProperties.get("url"); }
    public String getStorageIntegration() { return objectProperties.get("storageIntegration"); }
    public String getComment() { return objectProperties.get("comment"); }
    
    public Boolean getOrReplace() { 
        String val = objectProperties.get("orReplace"); 
        return val != null ? Boolean.valueOf(val) : null; 
    }
    
    public Boolean getIfNotExists() { 
        String val = objectProperties.get("ifNotExists"); 
        return val != null ? Boolean.valueOf(val) : null; 
    }
    
    public Boolean getTemporary() { 
        String val = objectProperties.get("temporary"); 
        return val != null ? Boolean.valueOf(val) : null; 
    }
}