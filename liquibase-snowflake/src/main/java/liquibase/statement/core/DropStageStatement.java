package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

/**
 * SQL Statement for DROP STAGE operations.
 * Simple implementation for straightforward drop operations.
 */
public class DropStageStatement extends AbstractSqlStatement {

    private String stageName;
    private String catalogName;
    private String schemaName;
    private Boolean ifExists;
    
    public DropStageStatement() {
        super();
    }
    
    public DropStageStatement(String stageName) {
        this.stageName = stageName;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public String getCatalogName() {
        return catalogName;
    }
    
    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }
    
    public String getSchemaName() {
        return schemaName;
    }
    
    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }
    
    public Boolean getIfExists() {
        return ifExists;
    }
    
    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
    }
}