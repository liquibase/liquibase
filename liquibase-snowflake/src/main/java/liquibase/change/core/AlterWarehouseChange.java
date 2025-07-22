package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterWarehouseStatement;

/**
 * Creates an alterWarehouse change.
 */
@DatabaseChange(
    name = "alterWarehouse",
    description = "Alters a warehouse",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "warehouse",
    since = "4.33"
)
public class AlterWarehouseChange extends AbstractChange {

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

    @DatabaseChangeProperty(description = "Name of the warehouse to alter", requiredForDatabase = "snowflake")
    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @DatabaseChangeProperty(description = "New name for the warehouse")
    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @DatabaseChangeProperty(description = "Size of the warehouse (XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE)")
    public String getWarehouseSize() {
        return warehouseSize;
    }

    public void setWarehouseSize(String warehouseSize) {
        this.warehouseSize = warehouseSize;
    }

    @DatabaseChangeProperty(description = "Type of the warehouse (STANDARD, SNOWPARK-OPTIMIZED)")
    public String getWarehouseType() {
        return warehouseType;
    }

    public void setWarehouseType(String warehouseType) {
        this.warehouseType = warehouseType;
    }

    @DatabaseChangeProperty(description = "Maximum number of clusters for multi-cluster warehouse")
    public Integer getMaxClusterCount() {
        return maxClusterCount;
    }

    public void setMaxClusterCount(Integer maxClusterCount) {
        this.maxClusterCount = maxClusterCount;
    }

    @DatabaseChangeProperty(description = "Minimum number of clusters for multi-cluster warehouse")
    public Integer getMinClusterCount() {
        return minClusterCount;
    }

    public void setMinClusterCount(Integer minClusterCount) {
        this.minClusterCount = minClusterCount;
    }

    @DatabaseChangeProperty(description = "Scaling policy for multi-cluster warehouse (STANDARD, ECONOMY)")
    public String getScalingPolicy() {
        return scalingPolicy;
    }

    public void setScalingPolicy(String scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    @DatabaseChangeProperty(description = "Number of seconds of inactivity after which the warehouse is automatically suspended")
    public Integer getAutoSuspend() {
        return autoSuspend;
    }

    public void setAutoSuspend(Integer autoSuspend) {
        this.autoSuspend = autoSuspend;
    }

    @DatabaseChangeProperty(description = "Whether the warehouse should auto-resume when queries are submitted")
    public Boolean getAutoResume() {
        return autoResume;
    }

    public void setAutoResume(Boolean autoResume) {
        this.autoResume = autoResume;
    }

    @DatabaseChangeProperty(description = "Resource monitor to assign to the warehouse")
    public String getResourceMonitor() {
        return resourceMonitor;
    }

    public void setResourceMonitor(String resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
    }

    @DatabaseChangeProperty(description = "Comment for the warehouse")
    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @DatabaseChangeProperty(description = "Enable query acceleration")
    public Boolean getEnableQueryAcceleration() {
        return enableQueryAcceleration;
    }

    public void setEnableQueryAcceleration(Boolean enableQueryAcceleration) {
        this.enableQueryAcceleration = enableQueryAcceleration;
    }

    @DatabaseChangeProperty(description = "Query acceleration max scale factor")
    public Integer getQueryAccelerationMaxScaleFactor() {
        return queryAccelerationMaxScaleFactor;
    }

    public void setQueryAccelerationMaxScaleFactor(Integer queryAccelerationMaxScaleFactor) {
        this.queryAccelerationMaxScaleFactor = queryAccelerationMaxScaleFactor;
    }

    @DatabaseChangeProperty(description = "Timeout in seconds for queued statements")
    public Long getStatementQueuedTimeoutInSeconds() {
        return statementQueuedTimeoutInSeconds;
    }

    public void setStatementQueuedTimeoutInSeconds(Long statementQueuedTimeoutInSeconds) {
        this.statementQueuedTimeoutInSeconds = statementQueuedTimeoutInSeconds;
    }

    @DatabaseChangeProperty(description = "Timeout in seconds for running statements")
    public Long getStatementTimeoutInSeconds() {
        return statementTimeoutInSeconds;
    }

    public void setStatementTimeoutInSeconds(Long statementTimeoutInSeconds) {
        this.statementTimeoutInSeconds = statementTimeoutInSeconds;
    }

    @DatabaseChangeProperty(description = "Tag value for the warehouse")
    public String getWarehouseTag() {
        return warehouseTag;
    }

    public void setWarehouseTag(String warehouseTag) {
        this.warehouseTag = warehouseTag;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[]{
            new AlterWarehouseStatement()
                .setWarehouseName(getWarehouseName())
                .setNewName(getNewName())
                .setWarehouseSize(getWarehouseSize())
                .setWarehouseType(getWarehouseType())
                .setMaxClusterCount(getMaxClusterCount())
                .setMinClusterCount(getMinClusterCount())
                .setScalingPolicy(getScalingPolicy())
                .setAutoSuspend(getAutoSuspend())
                .setAutoResume(getAutoResume())
                .setResourceMonitor(getResourceMonitor())
                .setComment(getComment())
                .setEnableQueryAcceleration(getEnableQueryAcceleration())
                .setQueryAccelerationMaxScaleFactor(getQueryAccelerationMaxScaleFactor())
                .setStatementQueuedTimeoutInSeconds(getStatementQueuedTimeoutInSeconds())
                .setStatementTimeoutInSeconds(getStatementTimeoutInSeconds())
                .setWarehouseTag(getWarehouseTag())
        };
    }

    @Override
    public String getConfirmationMessage() {
        return "Warehouse " + getWarehouseName() + " altered";
    }

    @Override
    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getWarehouseName() == null || getWarehouseName().trim().isEmpty()) {
            errors.addError("warehouseName is required");
        }
        
        // At least one alteration property must be specified
        boolean hasAlteration = getNewName() != null ||
                              getWarehouseSize() != null ||
                              getWarehouseType() != null ||
                              getMaxClusterCount() != null ||
                              getMinClusterCount() != null ||
                              getScalingPolicy() != null ||
                              getAutoSuspend() != null ||
                              getAutoResume() != null ||
                              getResourceMonitor() != null ||
                              getComment() != null ||
                              getEnableQueryAcceleration() != null ||
                              getQueryAccelerationMaxScaleFactor() != null ||
                              getStatementQueuedTimeoutInSeconds() != null ||
                              getStatementTimeoutInSeconds() != null ||
                              getWarehouseTag() != null;
                              
        if (!hasAlteration) {
            errors.addError("At least one alteration property must be specified");
        }
        
        return errors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}