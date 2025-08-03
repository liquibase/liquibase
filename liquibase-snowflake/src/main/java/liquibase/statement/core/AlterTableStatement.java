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
    private Integer setMaxDataExtensionTimeInDays;
    private String setDefaultDdlCollation;
    
    // Search optimization operations
    private String addSearchOptimization;
    private Boolean dropSearchOptimization;
    
    // Row access policy operations
    private String addRowAccessPolicy;
    private String dropRowAccessPolicy;
    
    // Aggregation policy operations
    private String setAggregationPolicy;
    private Boolean unsetAggregationPolicy;
    private Boolean forceAggregationPolicy;
    
    // Projection policy operations
    private String setProjectionPolicy;
    private Boolean unsetProjectionPolicy;
    private Boolean forceProjectionPolicy;
    
    // Tag operations
    private String setTag;
    private String unsetTag;
    
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
    
    public Integer getSetMaxDataExtensionTimeInDays() {
        return setMaxDataExtensionTimeInDays;
    }
    
    public void setSetMaxDataExtensionTimeInDays(Integer setMaxDataExtensionTimeInDays) {
        this.setMaxDataExtensionTimeInDays = setMaxDataExtensionTimeInDays;
    }
    
    public String getSetDefaultDdlCollation() {
        return setDefaultDdlCollation;
    }
    
    public void setSetDefaultDdlCollation(String setDefaultDdlCollation) {
        this.setDefaultDdlCollation = setDefaultDdlCollation;
    }
    
    // Search optimization operations getters and setters
    public String getAddSearchOptimization() {
        return addSearchOptimization;
    }
    
    public void setAddSearchOptimization(String addSearchOptimization) {
        this.addSearchOptimization = addSearchOptimization;
    }
    
    public Boolean getDropSearchOptimization() {
        return dropSearchOptimization;
    }
    
    public void setDropSearchOptimization(Boolean dropSearchOptimization) {
        this.dropSearchOptimization = dropSearchOptimization;
    }
    
    // Row access policy operations getters and setters
    public String getAddRowAccessPolicy() {
        return addRowAccessPolicy;
    }
    
    public void setAddRowAccessPolicy(String addRowAccessPolicy) {
        this.addRowAccessPolicy = addRowAccessPolicy;
    }
    
    public String getDropRowAccessPolicy() {
        return dropRowAccessPolicy;
    }
    
    public void setDropRowAccessPolicy(String dropRowAccessPolicy) {
        this.dropRowAccessPolicy = dropRowAccessPolicy;
    }
    
    // Aggregation policy operations getters and setters
    public String getSetAggregationPolicy() {
        return setAggregationPolicy;
    }
    
    public void setSetAggregationPolicy(String setAggregationPolicy) {
        this.setAggregationPolicy = setAggregationPolicy;
    }
    
    public Boolean getUnsetAggregationPolicy() {
        return unsetAggregationPolicy;
    }
    
    public void setUnsetAggregationPolicy(Boolean unsetAggregationPolicy) {
        this.unsetAggregationPolicy = unsetAggregationPolicy;
    }
    
    public Boolean getForceAggregationPolicy() {
        return forceAggregationPolicy;
    }
    
    public void setForceAggregationPolicy(Boolean forceAggregationPolicy) {
        this.forceAggregationPolicy = forceAggregationPolicy;
    }
    
    // Projection policy operations getters and setters
    public String getSetProjectionPolicy() {
        return setProjectionPolicy;
    }
    
    public void setSetProjectionPolicy(String setProjectionPolicy) {
        this.setProjectionPolicy = setProjectionPolicy;
    }
    
    public Boolean getUnsetProjectionPolicy() {
        return unsetProjectionPolicy;
    }
    
    public void setUnsetProjectionPolicy(Boolean unsetProjectionPolicy) {
        this.unsetProjectionPolicy = unsetProjectionPolicy;
    }
    
    public Boolean getForceProjectionPolicy() {
        return forceProjectionPolicy;
    }
    
    public void setForceProjectionPolicy(Boolean forceProjectionPolicy) {
        this.forceProjectionPolicy = forceProjectionPolicy;
    }
    
    // Tag operations getters and setters
    public String getSetTag() {
        return setTag;
    }
    
    public void setSetTag(String setTag) {
        this.setTag = setTag;
    }
    
    public String getUnsetTag() {
        return unsetTag;
    }
    
    public void setUnsetTag(String unsetTag) {
        this.unsetTag = unsetTag;
    }
}