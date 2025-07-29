package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterTableStatement extends AbstractSqlStatement {
    
    private String catalogName;
    private String schemaName;
    private String tableName;
    
    // Clustering operations (mutually exclusive)
    private String clusterBy;
    private Boolean dropClusteringKey;
    private Boolean suspendRecluster;
    private Boolean resumeRecluster;
    
    // Property settings (can be combined)
    private Integer setDataRetentionTimeInDays;
    private Boolean setChangeTracking;
    private Boolean setEnableSchemaEvolution;
    
    public AlterTableStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }
    
    // Catalog/Schema/Table name getters and setters
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
    
    public String getTableName() {
        return tableName;
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    // Clustering operations getters and setters
    public String getClusterBy() {
        return clusterBy;
    }
    
    public void setClusterBy(String clusterBy) {
        this.clusterBy = clusterBy;
    }
    
    public Boolean getDropClusteringKey() {
        return dropClusteringKey;
    }
    
    public void setDropClusteringKey(Boolean dropClusteringKey) {
        this.dropClusteringKey = dropClusteringKey;
    }
    
    public Boolean getSuspendRecluster() {
        return suspendRecluster;
    }
    
    public void setSuspendRecluster(Boolean suspendRecluster) {
        this.suspendRecluster = suspendRecluster;
    }
    
    public Boolean getResumeRecluster() {
        return resumeRecluster;
    }
    
    public void setResumeRecluster(Boolean resumeRecluster) {
        this.resumeRecluster = resumeRecluster;
    }
    
    // Property settings getters and setters
    public Integer getSetDataRetentionTimeInDays() {
        return setDataRetentionTimeInDays;
    }
    
    public void setSetDataRetentionTimeInDays(Integer setDataRetentionTimeInDays) {
        this.setDataRetentionTimeInDays = setDataRetentionTimeInDays;
    }
    
    public Boolean getSetChangeTracking() {
        return setChangeTracking;
    }
    
    public void setSetChangeTracking(Boolean setChangeTracking) {
        this.setChangeTracking = setChangeTracking;
    }
    
    public Boolean getSetEnableSchemaEvolution() {
        return setEnableSchemaEvolution;
    }
    
    public void setSetEnableSchemaEvolution(Boolean setEnableSchemaEvolution) {
        this.setEnableSchemaEvolution = setEnableSchemaEvolution;
    }
}