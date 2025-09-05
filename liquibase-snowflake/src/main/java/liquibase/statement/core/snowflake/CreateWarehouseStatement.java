package liquibase.statement.core.snowflake;

import liquibase.statement.AbstractSqlStatement;

public class CreateWarehouseStatement extends AbstractSqlStatement {
    
    private String warehouseName;
    private String warehouseSize;
    private String warehouseType;
    private Integer maxClusterCount;
    private Integer minClusterCount;
    private String scalingPolicy;
    private Integer autoSuspend;
    private Boolean autoResume;
    private Boolean initiallySuspended;
    private String resourceMonitor;
    private String comment;
    private Boolean enableQueryAcceleration;
    private Integer queryAccelerationMaxScaleFactor;
    private Integer maxConcurrencyLevel;
    private Integer statementQueuedTimeoutInSeconds;
    private Integer statementTimeoutInSeconds;
    private Boolean orReplace;
    private Boolean ifNotExists;
    private String resourceConstraint;

    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    public String getWarehouseSize() {
        return warehouseSize;
    }

    public void setWarehouseSize(String warehouseSize) {
        this.warehouseSize = warehouseSize;
    }

    public String getWarehouseType() {
        return warehouseType;
    }

    public void setWarehouseType(String warehouseType) {
        this.warehouseType = warehouseType;
    }

    public Integer getMaxClusterCount() {
        return maxClusterCount;
    }

    public void setMaxClusterCount(Integer maxClusterCount) {
        this.maxClusterCount = maxClusterCount;
    }

    public Integer getMinClusterCount() {
        return minClusterCount;
    }

    public void setMinClusterCount(Integer minClusterCount) {
        this.minClusterCount = minClusterCount;
    }

    public String getScalingPolicy() {
        return scalingPolicy;
    }

    public void setScalingPolicy(String scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    public Integer getAutoSuspend() {
        return autoSuspend;
    }

    public void setAutoSuspend(Integer autoSuspend) {
        this.autoSuspend = autoSuspend;
    }

    public Boolean getAutoResume() {
        return autoResume;
    }

    public void setAutoResume(Boolean autoResume) {
        this.autoResume = autoResume;
    }

    public Boolean getInitiallySuspended() {
        return initiallySuspended;
    }

    public void setInitiallySuspended(Boolean initiallySuspended) {
        this.initiallySuspended = initiallySuspended;
    }

    public String getResourceMonitor() {
        return resourceMonitor;
    }

    public void setResourceMonitor(String resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Boolean getEnableQueryAcceleration() {
        return enableQueryAcceleration;
    }

    public void setEnableQueryAcceleration(Boolean enableQueryAcceleration) {
        this.enableQueryAcceleration = enableQueryAcceleration;
    }

    public Integer getQueryAccelerationMaxScaleFactor() {
        return queryAccelerationMaxScaleFactor;
    }

    public void setQueryAccelerationMaxScaleFactor(Integer queryAccelerationMaxScaleFactor) {
        this.queryAccelerationMaxScaleFactor = queryAccelerationMaxScaleFactor;
    }

    public Integer getMaxConcurrencyLevel() {
        return maxConcurrencyLevel;
    }

    public void setMaxConcurrencyLevel(Integer maxConcurrencyLevel) {
        this.maxConcurrencyLevel = maxConcurrencyLevel;
    }

    public Integer getStatementQueuedTimeoutInSeconds() {
        return statementQueuedTimeoutInSeconds;
    }

    public void setStatementQueuedTimeoutInSeconds(Integer statementQueuedTimeoutInSeconds) {
        this.statementQueuedTimeoutInSeconds = statementQueuedTimeoutInSeconds;
    }

    public Integer getStatementTimeoutInSeconds() {
        return statementTimeoutInSeconds;
    }

    public void setStatementTimeoutInSeconds(Integer statementTimeoutInSeconds) {
        this.statementTimeoutInSeconds = statementTimeoutInSeconds;
    }

    public Boolean getOrReplace() {
        return orReplace;
    }

    public void setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
    }

    public Boolean getIfNotExists() {
        return ifNotExists;
    }

    public void setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
    }

    public String getResourceConstraint() {
        return resourceConstraint;
    }

    public void setResourceConstraint(String resourceConstraint) {
        this.resourceConstraint = resourceConstraint;
    }

    /**
     * Enhanced validation method that ensures the statement configuration is valid
     * based on Snowflake CREATE WAREHOUSE constraints.
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        
        // Basic validation
        if (warehouseName == null || warehouseName.trim().isEmpty()) {
            result.addError("Warehouse name is required");
        } else if (warehouseName.length() > 255 || !warehouseName.matches("^[A-Za-z_][A-Za-z0-9_$]*$")) {
            result.addError("Invalid warehouse name format. Must start with letter/underscore, contain only letters/numbers/underscores/dollar signs, max 255 characters: " + warehouseName);
        }
        
        // Validate OR REPLACE vs IF NOT EXISTS mutual exclusivity
        if (Boolean.TRUE.equals(orReplace) && Boolean.TRUE.equals(ifNotExists)) {
            result.addError("OR REPLACE and IF NOT EXISTS cannot be used together");
        }
        
        // Validate warehouse size enumeration
        if (warehouseSize != null) {
            String[] validSizes = {"XSMALL", "SMALL", "MEDIUM", "LARGE", "XLARGE", "XXLARGE", "XXXLARGE", "X4LARGE", "X5LARGE", "X6LARGE"};
            boolean validSize = false;
            for (String validSize2 : validSizes) {
                if (validSize2.equalsIgnoreCase(warehouseSize)) {
                    validSize = true;
                    break;
                }
            }
            if (!validSize) {
                result.addError("Invalid warehouse size '" + warehouseSize + "'. Valid sizes: XSMALL through X6LARGE");
            }
        }
        
        // Validate warehouse type enumeration
        if (warehouseType != null) {
            if (!"STANDARD".equalsIgnoreCase(warehouseType) && !"SNOWPARK-OPTIMIZED".equalsIgnoreCase(warehouseType)) {
                result.addError("Invalid warehouse type '" + warehouseType + "'. Valid types: STANDARD, SNOWPARK-OPTIMIZED");
            }
        }
        
        // Validate scaling policy enumeration
        if (scalingPolicy != null) {
            if (!"STANDARD".equalsIgnoreCase(scalingPolicy) && !"ECONOMY".equalsIgnoreCase(scalingPolicy)) {
                result.addError("Invalid scaling policy '" + scalingPolicy + "'. Valid policies: STANDARD, ECONOMY");
            }
        }
        
        // Validate cluster count ranges and relationship
        if (minClusterCount != null) {
            if (minClusterCount < 1 || minClusterCount > 10) {
                result.addError("MIN_CLUSTER_COUNT must be between 1 and 10, got: " + minClusterCount);
            }
        }
        
        if (maxClusterCount != null) {
            if (maxClusterCount < 1 || maxClusterCount > 10) {
                result.addError("MAX_CLUSTER_COUNT must be between 1 and 10, got: " + maxClusterCount);
            }
        }
        
        if (minClusterCount != null && maxClusterCount != null) {
            if (minClusterCount > maxClusterCount) {
                result.addError("MIN_CLUSTER_COUNT (" + minClusterCount + ") must be <= MAX_CLUSTER_COUNT (" + maxClusterCount + ")");
            }
        }
        
        // Validate auto-suspend constraints
        if (autoSuspend != null) {
            if (autoSuspend < 0) {
                result.addError("AUTO_SUSPEND cannot be negative, got: " + autoSuspend);
            } else if (autoSuspend > 0 && autoSuspend < 60) {
                result.addError("AUTO_SUSPEND must be 0 (disabled), NULL (never), or >= 60 seconds, got: " + autoSuspend);
            }
        }
        
        // Validate query acceleration dependency and range
        if (queryAccelerationMaxScaleFactor != null) {
            if (!Boolean.TRUE.equals(enableQueryAcceleration)) {
                result.addError("QUERY_ACCELERATION_MAX_SCALE_FACTOR requires ENABLE_QUERY_ACCELERATION = true");
            }
            if (queryAccelerationMaxScaleFactor < 1 || queryAccelerationMaxScaleFactor > 100) {
                result.addError("QUERY_ACCELERATION_MAX_SCALE_FACTOR must be between 1 and 100, got: " + queryAccelerationMaxScaleFactor);
            }
        }
        
        // Validate timeout ranges
        if (statementQueuedTimeoutInSeconds != null) {
            if (statementQueuedTimeoutInSeconds < 0 || statementQueuedTimeoutInSeconds > 604800) { // 7 days max
                result.addError("STATEMENT_QUEUED_TIMEOUT_IN_SECONDS must be between 0 and 604800 (7 days), got: " + statementQueuedTimeoutInSeconds);
            }
        }
        
        if (statementTimeoutInSeconds != null) {
            if (statementTimeoutInSeconds < 0 || statementTimeoutInSeconds > 604800) { // 7 days max
                result.addError("STATEMENT_TIMEOUT_IN_SECONDS must be between 0 and 604800 (7 days), got: " + statementTimeoutInSeconds);
            }
        }
        
        // Validate max concurrency level range
        if (maxConcurrencyLevel != null) {
            if (maxConcurrencyLevel < 1 || maxConcurrencyLevel > 8) {
                result.addError("MAX_CONCURRENCY_LEVEL must be between 1 and 8, got: " + maxConcurrencyLevel);
            }
        }
        
        // Warn about Enterprise Edition features
        if (maxClusterCount != null || minClusterCount != null || scalingPolicy != null) {
            result.addWarning("Multi-cluster warehouse features require Snowflake Enterprise Edition");
        }
        if (Boolean.TRUE.equals(enableQueryAcceleration)) {
            result.addWarning("Query acceleration requires Snowflake Enterprise Edition");
        }
        
        return result;
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