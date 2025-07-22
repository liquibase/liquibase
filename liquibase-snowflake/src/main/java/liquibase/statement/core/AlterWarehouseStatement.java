package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AlterWarehouseStatement extends AbstractSqlStatement {
    
    private String warehouseName;
    private String newName;
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
}