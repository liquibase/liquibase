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
}