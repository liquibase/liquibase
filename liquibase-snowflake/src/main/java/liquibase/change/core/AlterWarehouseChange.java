package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;import liquibase.exception.ValidationErrors;
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

    private String operationType; // Enhanced: explicit operation type
    private String warehouseName;
    private String newName;
    private Boolean ifExists;
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
    private String action; // SUSPEND, RESUME, ABORT ALL QUERIES
    private Boolean unsetResourceMonitor;
    private Boolean unsetComment;

    @DatabaseChangeProperty(description = "Name of the warehouse to alter", requiredForDatabase = "snowflake")
    public String getWarehouseName() {
        return warehouseName;
    }

    public void setWarehouseName(String warehouseName) {
        this.warehouseName = warehouseName;
    }

    @DatabaseChangeProperty(description = "New name for the warehouse")
    public String getNewWarehouseName() {
        return newName;
    }

    public void setNewWarehouseName(String newName) {
        this.newName = newName;
    }

    @DatabaseChangeProperty(description = "Only alter if warehouse exists")
    public Boolean getIfExists() {
        return ifExists;
    }

    public void setIfExists(Boolean ifExists) {
        this.ifExists = ifExists;
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

    @DatabaseChangeProperty(description = "Action to perform (SUSPEND, RESUME, ABORT ALL QUERIES)")
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @DatabaseChangeProperty(description = "Unset resource monitor")
    public Boolean getUnsetResourceMonitor() {
        return unsetResourceMonitor;
    }

    public void setUnsetResourceMonitor(Boolean unsetResourceMonitor) {
        this.unsetResourceMonitor = unsetResourceMonitor;
    }

    @DatabaseChangeProperty(description = "Unset comment")
    public Boolean getUnsetComment() {
        return unsetComment;
    }

    public void setUnsetComment(Boolean unsetComment) {
        this.unsetComment = unsetComment;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterWarehouseStatement statement = new AlterWarehouseStatement()
            .setWarehouseName(getWarehouseName())
            .setIfExists(getIfExists())
            .setNewName(getNewWarehouseName())
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
            .setAction(getAction()) // Maintain backward compatibility
            .setUnsetResourceMonitor(getUnsetResourceMonitor())
            .setUnsetComment(getUnsetComment());

        // Enhanced Phase 2 API: Set explicit operation type if provided
        if (getOperationType() != null && !getOperationType().trim().isEmpty()) {
            try {
                AlterWarehouseStatement.OperationType opType = 
                    AlterWarehouseStatement.OperationType.valueOf(getOperationType().toUpperCase());
                statement.setOperationType(opType);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid operation type: " + getOperationType() + 
                    ". Valid types are: RENAME, SET, UNSET, SUSPEND, RESUME, ABORT_ALL_QUERIES");
            }
        }

        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        return "Warehouse " + getWarehouseName() + " altered";
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override    public boolean supportsRollback(Database database) {
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        if (getWarehouseName() == null || getWarehouseName().trim().isEmpty()) {
            errors.addError("warehouseName is required");
        }
        
        // At least one alteration property must be specified
        boolean hasAlteration = getNewWarehouseName() != null ||
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
                              getWarehouseTag() != null ||
                              getAction() != null ||
                              Boolean.TRUE.equals(getUnsetResourceMonitor()) ||
                              Boolean.TRUE.equals(getUnsetComment());
                              
        if (!hasAlteration) {
            errors.addError("At least one alteration property must be specified");
        }
        
        // Validate warehouse size if provided
        if (getWarehouseSize() != null && !isValidWarehouseSize(getWarehouseSize())) {
            errors.addError("Invalid warehouse size: " + getWarehouseSize());
        }
        
        // Validate warehouse type if provided
        if (getWarehouseType() != null) {
            String type = getWarehouseType().toUpperCase();
            if (!type.equals("STANDARD") && !type.equals("SNOWPARK-OPTIMIZED")) {
                errors.addError("Invalid warehouse type: " + getWarehouseType());
            }
        }
        
        // Validate scaling policy if provided
        if (getScalingPolicy() != null) {
            String policy = getScalingPolicy().toUpperCase();
            if (!policy.equals("STANDARD") && !policy.equals("ECONOMY")) {
                errors.addError("Invalid scaling policy: " + getScalingPolicy());
            }
        }
        
        // Validate action if provided
        if (getAction() != null) {
            String act = getAction().toUpperCase();
            if (!act.equals("SUSPEND") && !act.equals("RESUME") && !act.equals("ABORT ALL QUERIES")) {
                errors.addError("Invalid action: " + getAction() + ". Valid actions are: SUSPEND, RESUME, ABORT ALL QUERIES");
            }
        }
        
        // Validate cluster counts
        if (getMinClusterCount() != null && getMinClusterCount() < 1) {
            errors.addError("minClusterCount must be at least 1");
        }
        if (getMaxClusterCount() != null && getMaxClusterCount() < 1) {
            errors.addError("maxClusterCount must be at least 1");
        }
        if (getMinClusterCount() != null && getMaxClusterCount() != null && getMinClusterCount() > getMaxClusterCount()) {
            errors.addError("minClusterCount cannot be greater than maxClusterCount");
        }
        if (getMaxClusterCount() != null && getMaxClusterCount() > 10) {
            errors.addError("maxClusterCount cannot exceed 10");
        }
        
        // Validate autoSuspend
        if (getAutoSuspend() != null && getAutoSuspend() != 0 && getAutoSuspend() < 60) {
            errors.addError("autoSuspend must be 0 (never suspend) or at least 60 seconds");
        }
        
        // Validate queryAccelerationMaxScaleFactor
        if (getQueryAccelerationMaxScaleFactor() != null && 
            (getQueryAccelerationMaxScaleFactor() < 0 || getQueryAccelerationMaxScaleFactor() > 100)) {
            errors.addError("queryAccelerationMaxScaleFactor must be between 0 and 100");
        }
        
        // Validate mutual exclusivity
        if (getResourceMonitor() != null && Boolean.TRUE.equals(getUnsetResourceMonitor())) {
            errors.addError("Cannot both set and unset resourceMonitor");
        }
        if (getComment() != null && Boolean.TRUE.equals(getUnsetComment())) {
            errors.addError("Cannot both set and unset comment");
        }
        
        // Validate that action cannot be combined with other operations
        if (getAction() != null && hasNonActionAlterations()) {
            errors.addError("Action (" + getAction() + ") cannot be combined with other alterations");
        }
        
        return errors;
    }
    
    private boolean isValidWarehouseSize(String size) {
        String upperSize = size.toUpperCase();
        return upperSize.equals("XSMALL") || upperSize.equals("SMALL") || upperSize.equals("MEDIUM") || 
               upperSize.equals("LARGE") || upperSize.equals("XLARGE") || upperSize.equals("XXLARGE") || 
               upperSize.equals("XXXLARGE") || upperSize.equals("X4LARGE") || upperSize.equals("X5LARGE") || 
               upperSize.equals("X6LARGE");
    }
    
    private boolean hasNonActionAlterations() {
        return getNewWarehouseName() != null ||
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
               getWarehouseTag() != null ||
               Boolean.TRUE.equals(getUnsetResourceMonitor()) ||
               Boolean.TRUE.equals(getUnsetComment());
    }

    // Enhanced Phase 2 API: Explicit operation type support

    @DatabaseChangeProperty(description = "Type of ALTER WAREHOUSE operation (RENAME, SET, UNSET, SUSPEND, RESUME, ABORT_ALL_QUERIES)")
    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}