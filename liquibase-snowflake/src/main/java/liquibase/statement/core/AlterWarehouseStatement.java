package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterWarehouseStatement extends AbstractSqlStatement {
    
    /**
     * Enum defining the type of ALTER WAREHOUSE operation to perform.
     * Each operation type has different valid properties and SQL generation logic.
     */
    public enum OperationType {
        /** RENAME operation: ALTER WAREHOUSE [IF EXISTS] name RENAME TO new_name */
        RENAME,
        /** SET operation: ALTER WAREHOUSE [IF EXISTS] name SET property = value, ... */
        SET,
        /** UNSET operation: ALTER WAREHOUSE [IF EXISTS] name UNSET property, ... */
        UNSET,
        /** SUSPEND operation: ALTER WAREHOUSE [IF EXISTS] name SUSPEND */
        SUSPEND,
        /** RESUME operation: ALTER WAREHOUSE [IF EXISTS] name RESUME [IF SUSPENDED] */
        RESUME,
        /** ABORT ALL QUERIES operation: ALTER WAREHOUSE name ABORT ALL QUERIES */
        ABORT_ALL_QUERIES
    }
    
    private OperationType operationType;
    private String warehouseName;
    private String newName;
    private Boolean ifExists;
    private Boolean ifSuspended; // For RESUME IF SUSPENDED
    private String warehouseSize;
    private String warehouseType;
    private Integer maxClusterCount;
    private Integer minClusterCount;
    private String scalingPolicy;
    private Integer autoSuspend;
    private Boolean autoResume;
    private String resourceMonitor;
    private String comment;
    private Boolean enableQueryAcceleration;
    private Integer queryAccelerationMaxScaleFactor;
    private Long statementQueuedTimeoutInSeconds;
    private Long statementTimeoutInSeconds;
    private String warehouseTag;
    private String action;
    private Boolean unsetResourceMonitor;
    private Boolean unsetComment;

    public String getWarehouseName() {
        return warehouseName;
    }

    public AlterWarehouseStatement setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
        return this;
    }

    public String getNewName() {
        return newName;
    }

    public AlterWarehouseStatement setNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public Boolean getIfExists() {
        return ifExists;
    }

    public AlterWarehouseStatement setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
        return this;
    }

    public String getWarehouseSize() {
        return warehouseSize;
    }

    public AlterWarehouseStatement setWarehouseSize(String warehouseSize) {
        this.warehouseSize = warehouseSize;
        return this;
    }

    public String getWarehouseType() {
        return warehouseType;
    }

    public AlterWarehouseStatement setWarehouseType(String warehouseType) {
        this.warehouseType = warehouseType;
        return this;
    }

    public Integer getMaxClusterCount() {
        return maxClusterCount;
    }

    public AlterWarehouseStatement setMaxClusterCount(Integer maxClusterCount) {
        this.maxClusterCount = maxClusterCount;
        return this;
    }

    public Integer getMinClusterCount() {
        return minClusterCount;
    }

    public AlterWarehouseStatement setMinClusterCount(Integer minClusterCount) {
        this.minClusterCount = minClusterCount;
        return this;
    }

    public String getScalingPolicy() {
        return scalingPolicy;
    }

    public AlterWarehouseStatement setScalingPolicy(String scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
        return this;
    }

    public Integer getAutoSuspend() {
        return autoSuspend;
    }

    public AlterWarehouseStatement setAutoSuspend(Integer autoSuspend) {
        this.autoSuspend = autoSuspend;
        return this;
    }

    public Boolean getAutoResume() {
        return autoResume;
    }

    public AlterWarehouseStatement setAutoResume(Boolean autoResume) {
        this.autoResume = autoResume;
        return this;
    }

    public String getResourceMonitor() {
        return resourceMonitor;
    }

    public AlterWarehouseStatement setResourceMonitor(String resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public AlterWarehouseStatement setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Boolean getEnableQueryAcceleration() {
        return enableQueryAcceleration;
    }

    public AlterWarehouseStatement setEnableQueryAcceleration(Boolean enableQueryAcceleration) {
        this.enableQueryAcceleration = enableQueryAcceleration;
        return this;
    }

    public Integer getQueryAccelerationMaxScaleFactor() {
        return queryAccelerationMaxScaleFactor;
    }

    public AlterWarehouseStatement setQueryAccelerationMaxScaleFactor(Integer queryAccelerationMaxScaleFactor) {
        this.queryAccelerationMaxScaleFactor = queryAccelerationMaxScaleFactor;
        return this;
    }

    public Long getStatementQueuedTimeoutInSeconds() {
        return statementQueuedTimeoutInSeconds;
    }

    public AlterWarehouseStatement setStatementQueuedTimeoutInSeconds(Long statementQueuedTimeoutInSeconds) {
        this.statementQueuedTimeoutInSeconds = statementQueuedTimeoutInSeconds;
        return this;
    }

    public Long getStatementTimeoutInSeconds() {
        return statementTimeoutInSeconds;
    }

    public AlterWarehouseStatement setStatementTimeoutInSeconds(Long statementTimeoutInSeconds) {
        this.statementTimeoutInSeconds = statementTimeoutInSeconds;
        return this;
    }

    public String getWarehouseTag() {
        return warehouseTag;
    }

    public AlterWarehouseStatement setWarehouseTag(String warehouseTag) {
        this.warehouseTag = warehouseTag;
        return this;
    }

    public String getAction() {
        return action;
    }

    public AlterWarehouseStatement setAction(String action) {
        this.action = action;
        return this;
    }

    public Boolean getUnsetResourceMonitor() {
        return unsetResourceMonitor;
    }

    public AlterWarehouseStatement setUnsetResourceMonitor(Boolean unsetResourceMonitor) {
        this.unsetResourceMonitor = unsetResourceMonitor;
        return this;
    }

    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public AlterWarehouseStatement setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
        return this;
    }

    // Enhanced API methods for sophisticated operation-type-driven architecture

    public OperationType getOperationType() {
        return operationType;
    }

    public AlterWarehouseStatement setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public Boolean getIfSuspended() {
        return ifSuspended;
    }

    public AlterWarehouseStatement setIfSuspended(Boolean ifSuspended) {
        this.ifSuspended = ifSuspended;
        return this;
    }

    /**
     * Alias for setNewName() to match enhanced Phase 2 requirements naming convention.
     */
    public AlterWarehouseStatement setNewWarehouseName(String newWarehouseName) {
        return setNewName(newWarehouseName);
    }

    /**
     * Alias for getNewName() to match enhanced Phase 2 requirements naming convention.
     */
    public String getNewWarehouseName() {
        return getNewName();
    }

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on the specified operation type and Snowflake constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (warehouseName == null || warehouseName.trim().isEmpty()) {
            result.addError("Warehouse name is required");
        }
        
        if (operationType == null) {
            // If no operation type specified, try to infer from properties
            operationType = inferOperationType();
            if (operationType == null) {
                result.addError("Operation type must be specified or inferable from properties");
            }
        }
        
        if (operationType != null) {
            validateOperationType(result);
        }
        
        return result;
    }

    private OperationType inferOperationType() {
        // Try to infer operation type from set properties (for backward compatibility)
        if (newName != null) {
            return OperationType.RENAME;
        }
        
        // Check for conflicts between different operation types
        boolean hasSetProperties = hasSetProperties();
        boolean hasUnsetProperties = hasUnsetProperties();
        boolean hasAction = action != null;
        
        // If multiple operation types are specified, cannot infer - this is an error
        int operationTypeCount = (hasSetProperties ? 1 : 0) + (hasUnsetProperties ? 1 : 0) + (hasAction ? 1 : 0);
        if (operationTypeCount > 1) {
            return null; // Cannot infer - multiple operation types specified
        }
        
        if (hasUnsetProperties) {
            return OperationType.UNSET;
        }
        if (hasSetProperties) {
            return OperationType.SET;
        }
        if ("SUSPEND".equals(action)) {
            return OperationType.SUSPEND;
        }
        if ("RESUME".equals(action)) {
            return OperationType.RESUME;
        }
        if ("ABORT ALL QUERIES".equals(action)) {
            return OperationType.ABORT_ALL_QUERIES;
        }
        
        return null;
    }

    private void validateOperationType(ValidationResult result) {
        switch (operationType) {
            case RENAME:
                validateRenameOperation(result);
                break;
            case SET:
                validateSetOperation(result);
                break;
            case UNSET:
                validateUnsetOperation(result);
                break;
            case SUSPEND:
            case RESUME:
            case ABORT_ALL_QUERIES:
                validateActionOperation(result);
                break;
        }
    }

    private void validateRenameOperation(ValidationResult result) {
        if (newName == null || newName.trim().isEmpty()) {
            result.addError("New warehouse name is required for RENAME operation");
        }
        // RENAME operations should not have other properties set
        if (hasSetProperties()) {
            result.addWarning("SET properties are ignored in RENAME operations");
        }
        if (hasUnsetProperties()) {
            result.addWarning("UNSET properties are ignored in RENAME operations");
        }
    }

    private void validateSetOperation(ValidationResult result) {
        if (!hasSetProperties()) {
            result.addError("At least one property must be specified for SET operation");
        }
        
        // Validate cluster count relationship
        if (minClusterCount != null && maxClusterCount != null) {
            if (minClusterCount > maxClusterCount) {
                result.addError("MIN_CLUSTER_COUNT (" + minClusterCount + ") must be <= MAX_CLUSTER_COUNT (" + maxClusterCount + ")");
            }
        }
        
        // Validate auto-suspend constraints
        if (autoSuspend != null && autoSuspend > 0 && autoSuspend < 60) {
            result.addError("AUTO_SUSPEND must be 0 (disabled), NULL (never), or >= 60 seconds");
        }
        
        // Validate query acceleration dependency
        if (queryAccelerationMaxScaleFactor != null && !Boolean.TRUE.equals(enableQueryAcceleration)) {
            result.addError("QUERY_ACCELERATION_MAX_SCALE_FACTOR requires ENABLE_QUERY_ACCELERATION = true");
        }
        
        // Warn about Enterprise Edition features
        if (maxClusterCount != null || minClusterCount != null || scalingPolicy != null) {
            result.addWarning("Multi-cluster warehouse features require Snowflake Enterprise Edition");
        }
        if (Boolean.TRUE.equals(enableQueryAcceleration)) {
            result.addWarning("Query acceleration requires Snowflake Enterprise Edition");
        }
    }

    private void validateUnsetOperation(ValidationResult result) {
        if (!hasUnsetProperties()) {
            result.addError("At least one property must be specified for UNSET operation");
        }
    }

    private void validateActionOperation(ValidationResult result) {
        if (operationType == OperationType.RESUME && Boolean.TRUE.equals(ifSuspended)) {
            // RESUME IF SUSPENDED is valid
        }
        if (hasSetProperties() || hasUnsetProperties()) {
            result.addWarning("SET/UNSET properties are ignored in " + operationType + " operations");
        }
    }

    private boolean hasSetProperties() {
        return warehouseSize != null || warehouseType != null || maxClusterCount != null || 
               minClusterCount != null || scalingPolicy != null || autoSuspend != null || 
               autoResume != null || resourceMonitor != null || comment != null || 
               enableQueryAcceleration != null || queryAccelerationMaxScaleFactor != null ||
               statementQueuedTimeoutInSeconds != null || statementTimeoutInSeconds != null ||
               warehouseTag != null;
    }

    private boolean hasUnsetProperties() {
        return Boolean.TRUE.equals(unsetResourceMonitor) || Boolean.TRUE.equals(unsetComment);
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