package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.snowflake.CreateWarehouseStatement;

/**
 * Creates a new warehouse in Snowflake.
 */
@DatabaseChange(
    name = "createWarehouse",
    description = "Creates a warehouse",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "warehouse",
    since = "4.33"
)
public class CreateWarehouseChange extends AbstractChange {

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
    private String queryAccelerationMaxScaleFactor;
    private Integer maxConcurrencyLevel;
    private Integer statementQueuedTimeoutInSeconds;
    private Integer statementTimeoutInSeconds;

    @DatabaseChangeProperty(description = "Name of the warehouse to create", requiredForDatabase = "snowflake")
    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @DatabaseChangeProperty(description = "Size of the warehouse (e.g., XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE)")
    public String getWarehouseSize() {
        return warehouseSize;
    }

    public void setWarehouseSize(String warehouseSize) {
        this.warehouseSize = warehouseSize;
    }

    @DatabaseChangeProperty(description = "Type of the warehouse (STANDARD or SNOWPARK-OPTIMIZED)")
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

    @DatabaseChangeProperty(description = "Scaling policy for multi-cluster warehouse (STANDARD or ECONOMY)")
    public String getScalingPolicy() {
        return scalingPolicy;
    }

    public void setScalingPolicy(String scalingPolicy) {
        this.scalingPolicy = scalingPolicy;
    }

    @DatabaseChangeProperty(description = "Number of seconds of inactivity after which to automatically suspend the warehouse")
    public Integer getAutoSuspend() {
        return autoSuspend;
    }

    public void setAutoSuspend(Integer autoSuspend) {
        this.autoSuspend = autoSuspend;
    }

    @DatabaseChangeProperty(description = "Whether to automatically resume the warehouse when a query is submitted")
    public Boolean getAutoResume() {
        return autoResume;
    }

    public void setAutoResume(Boolean autoResume) {
        this.autoResume = autoResume;
    }

    @DatabaseChangeProperty(description = "Whether the warehouse should be initially suspended")
    public Boolean getInitiallySuspended() {
        return initiallySuspended;
    }

    public void setInitiallySuspended(Boolean initiallySuspended) {
        this.initiallySuspended = initiallySuspended;
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

    @DatabaseChangeProperty(description = "Whether to enable query acceleration")
    public Boolean getEnableQueryAcceleration() {
        return enableQueryAcceleration;
    }

    public void setEnableQueryAcceleration(Boolean enableQueryAcceleration) {
        this.enableQueryAcceleration = enableQueryAcceleration;
    }

    @DatabaseChangeProperty(description = "Query acceleration max scale factor")
    public String getQueryAccelerationMaxScaleFactor() {
        return queryAccelerationMaxScaleFactor;
    }

    public void setQueryAccelerationMaxScaleFactor(String queryAccelerationMaxScaleFactor) {
        this.queryAccelerationMaxScaleFactor = queryAccelerationMaxScaleFactor;
    }

    @DatabaseChangeProperty(description = "Maximum concurrency level")
    public Integer getMaxConcurrencyLevel() {
        return maxConcurrencyLevel;
    }

    public void setMaxConcurrencyLevel(Integer maxConcurrencyLevel) {
        this.maxConcurrencyLevel = maxConcurrencyLevel;
    }

    @DatabaseChangeProperty(description = "Statement queued timeout in seconds")
    public Integer getStatementQueuedTimeoutInSeconds() {
        return statementQueuedTimeoutInSeconds;
    }

    public void setStatementQueuedTimeoutInSeconds(Integer statementQueuedTimeoutInSeconds) {
        this.statementQueuedTimeoutInSeconds = statementQueuedTimeoutInSeconds;
    }

    @DatabaseChangeProperty(description = "Statement timeout in seconds")
    public Integer getStatementTimeoutInSeconds() {
        return statementTimeoutInSeconds;
    }

    public void setStatementTimeoutInSeconds(Integer statementTimeoutInSeconds) {
        this.statementTimeoutInSeconds = statementTimeoutInSeconds;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (warehouseName == null || warehouseName.trim().isEmpty()) {
            errors.addError("warehouseName is required");
        }
        
        // Validate warehouse size if provided
        if (warehouseSize != null) {
            String size = warehouseSize.toUpperCase();
            if (!isValidWarehouseSize(size)) {
                errors.addError("Invalid warehouse size: " + warehouseSize + 
                    ". Valid sizes are: XSMALL, SMALL, MEDIUM, LARGE, XLARGE, XXLARGE, XXXLARGE, X4LARGE, X5LARGE, X6LARGE");
            }
        }
        
        // Validate warehouse type if provided
        if (warehouseType != null) {
            String type = warehouseType.toUpperCase();
            if (!type.equals("STANDARD") && !type.equals("SNOWPARK-OPTIMIZED")) {
                errors.addError("Invalid warehouse type: " + warehouseType + 
                    ". Valid types are: STANDARD, SNOWPARK-OPTIMIZED");
            }
        }
        
        // Validate scaling policy if provided
        if (scalingPolicy != null) {
            String policy = scalingPolicy.toUpperCase();
            if (!policy.equals("STANDARD") && !policy.equals("ECONOMY")) {
                errors.addError("Invalid scaling policy: " + scalingPolicy + 
                    ". Valid policies are: STANDARD, ECONOMY");
            }
        }
        
        // Validate cluster counts
        if (minClusterCount != null && minClusterCount < 1) {
            errors.addError("minClusterCount must be at least 1");
        }
        if (maxClusterCount != null && maxClusterCount < 1) {
            errors.addError("maxClusterCount must be at least 1");
        }
        if (minClusterCount != null && maxClusterCount != null && minClusterCount > maxClusterCount) {
            errors.addError("minClusterCount cannot be greater than maxClusterCount");
        }
        
        return errors;
    }

    private boolean isValidWarehouseSize(String size) {
        return size.equals("XSMALL") || size.equals("SMALL") || size.equals("MEDIUM") || 
               size.equals("LARGE") || size.equals("XLARGE") || size.equals("XXLARGE") || 
               size.equals("XXXLARGE") || size.equals("X4LARGE") || size.equals("X5LARGE") || 
               size.equals("X6LARGE");
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        CreateWarehouseStatement statement = new CreateWarehouseStatement();
        
        statement.setWarehouseName(getWarehouseName());
        statement.setWarehouseSize(getWarehouseSize());
        statement.setWarehouseType(getWarehouseType());
        statement.setMaxClusterCount(getMaxClusterCount());
        statement.setMinClusterCount(getMinClusterCount());
        statement.setScalingPolicy(getScalingPolicy());
        statement.setAutoSuspend(getAutoSuspend());
        statement.setAutoResume(getAutoResume());
        statement.setInitiallySuspended(getInitiallySuspended());
        statement.setResourceMonitor(getResourceMonitor());
        statement.setComment(getComment());
        statement.setEnableQueryAcceleration(getEnableQueryAcceleration());
        statement.setQueryAccelerationMaxScaleFactor(getQueryAccelerationMaxScaleFactor());
        statement.setMaxConcurrencyLevel(getMaxConcurrencyLevel());
        statement.setStatementQueuedTimeoutInSeconds(getStatementQueuedTimeoutInSeconds());
        statement.setStatementTimeoutInSeconds(getStatementTimeoutInSeconds());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Warehouse " + getWarehouseName() + " created";
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}