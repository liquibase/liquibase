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
    
    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake ALTER TABLE constraints and operation types.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (tableName == null || tableName.trim().isEmpty()) {
            result.addError("Table name is required");
        } else if (tableName.length() > 255 || !tableName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid table name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + tableName);
        }
        
        // Validate clustering operations are mutually exclusive
        int clusteringOperations = 0;
        if (clusterBy != null) clusteringOperations++;
        if (Boolean.TRUE.equals(dropClusteringKey)) clusteringOperations++;
        if (Boolean.TRUE.equals(suspendRecluster)) clusteringOperations++;
        if (Boolean.TRUE.equals(resumeRecluster)) clusteringOperations++;
        
        if (clusteringOperations > 1) {
            result.addError("Clustering operations are mutually exclusive. Only one of: clusterBy, dropClusteringKey, suspendRecluster, resumeRecluster can be specified");
        }
        
        // Validate search optimization operations are mutually exclusive
        if (addSearchOptimization != null && Boolean.TRUE.equals(dropSearchOptimization)) {
            result.addError("Cannot add and drop search optimization simultaneously");
        }
        
        // Validate row access policy operations
        if (addRowAccessPolicy != null && dropRowAccessPolicy != null) {
            result.addError("Cannot add and drop row access policy simultaneously");
        }
        
        // Validate aggregation policy operations
        if (setAggregationPolicy != null && Boolean.TRUE.equals(unsetAggregationPolicy)) {
            result.addError("Cannot set and unset aggregation policy simultaneously");
        }
        
        // Validate projection policy operations
        if (setProjectionPolicy != null && Boolean.TRUE.equals(unsetProjectionPolicy)) {
            result.addError("Cannot set and unset projection policy simultaneously");
        }
        
        // Validate tag operations
        if (setTag != null && unsetTag != null) {
            result.addError("Cannot set and unset tags simultaneously");
        }
        
        // Validate data retention range
        if (setDataRetentionTimeInDays != null) {
            if (setDataRetentionTimeInDays < 0) {
                result.addError("DATA_RETENTION_TIME_IN_DAYS cannot be negative, got: " + setDataRetentionTimeInDays);
            } else if (setDataRetentionTimeInDays > 90) {
                result.addWarning("DATA_RETENTION_TIME_IN_DAYS > 90 days requires Snowflake Enterprise Edition, got: " + setDataRetentionTimeInDays);
                if (setDataRetentionTimeInDays > 365) {
                    result.addError("DATA_RETENTION_TIME_IN_DAYS cannot exceed 365 days, got: " + setDataRetentionTimeInDays);
                }
            }
        }
        
        // Validate max data extension time range  
        if (setMaxDataExtensionTimeInDays != null) {
            if (setMaxDataExtensionTimeInDays < 0) {
                result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot be negative, got: " + setMaxDataExtensionTimeInDays);
            } else if (setMaxDataExtensionTimeInDays > 14) {
                result.addError("MAX_DATA_EXTENSION_TIME_IN_DAYS cannot exceed 14 days, got: " + setMaxDataExtensionTimeInDays);
            }
        }
        
        // Warn about Enterprise Edition features
        if (Boolean.TRUE.equals(setChangeTracking)) {
            result.addWarning("Change tracking requires Snowflake Enterprise Edition for streams functionality");
        }
        
        if (Boolean.TRUE.equals(setEnableSchemaEvolution)) {
            result.addWarning("Schema evolution for semi-structured data requires appropriate account settings");
        }
        
        if (addSearchOptimization != null) {
            result.addWarning("Search optimization may incur additional compute and storage costs");
        }
        
        if (addRowAccessPolicy != null || setAggregationPolicy != null || setProjectionPolicy != null) {
            result.addWarning("Security policies require Snowflake Enterprise Edition and proper governance setup");
        }
        
        // Validate operation count - at least one operation must be specified
        int totalOperations = countOperations();
        if (totalOperations == 0) {
            result.addError("At least one ALTER TABLE operation must be specified");
        }
        
        return result;
    }
    
    private int countOperations() {
        int count = 0;
        
        // Clustering operations
        if (clusterBy != null) count++;
        if (Boolean.TRUE.equals(dropClusteringKey)) count++;
        if (Boolean.TRUE.equals(suspendRecluster)) count++;
        if (Boolean.TRUE.equals(resumeRecluster)) count++;
        
        // Property settings
        if (setDataRetentionTimeInDays != null) count++;
        if (setChangeTracking != null) count++;
        if (setEnableSchemaEvolution != null) count++;
        if (setMaxDataExtensionTimeInDays != null) count++;
        if (setDefaultDdlCollation != null) count++;
        
        // Search optimization
        if (addSearchOptimization != null) count++;
        if (Boolean.TRUE.equals(dropSearchOptimization)) count++;
        
        // Policy operations
        if (addRowAccessPolicy != null) count++;
        if (dropRowAccessPolicy != null) count++;
        if (setAggregationPolicy != null) count++;
        if (Boolean.TRUE.equals(unsetAggregationPolicy)) count++;
        if (Boolean.TRUE.equals(forceAggregationPolicy)) count++;
        if (setProjectionPolicy != null) count++;
        if (Boolean.TRUE.equals(unsetProjectionPolicy)) count++;
        if (Boolean.TRUE.equals(forceProjectionPolicy)) count++;
        
        // Tag operations
        if (setTag != null) count++;
        if (unsetTag != null) count++;
        
        return count;
    }
    
    /**
     * Simple validation result container
     */
    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();
        
        public boolean hasErrors() { return !errors.isEmpty(); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }
        public java.util.List<String> getErrors() { return errors; }
        public java.util.List<String> getWarnings() { return warnings; }
        
        public void addError(String error) { errors.add(error); }
        public void addWarning(String warning) { warnings.add(warning); }
    }
}