package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Snowflake warehouse (compute cluster) database object.
 */
public class Warehouse extends AbstractDatabaseObject {
    
    // Valid warehouse sizes in Snowflake UI form
    private static final List<String> VALID_SIZES = Arrays.asList(
        "XSMALL", "SMALL", "MEDIUM", "LARGE", "XLARGE",
        "XXLARGE", "XXXLARGE", "X4LARGE", "X5LARGE", "X6LARGE"
    );
    
    private static final List<String> VALID_TYPES = Arrays.asList(
        "STANDARD", "SNOWPARK-OPTIMIZED"
    );
    
    private static final List<String> VALID_SCALING_POLICIES = Arrays.asList(
        "STANDARD", "ECONOMY"
    );
    
    private static final List<String> VALID_RESOURCE_CONSTRAINTS = Arrays.asList(
        "STANDARD_GEN_1", "STANDARD_GEN_2", "MEMORY_1X", "MEMORY_1X_x86", 
        "MEMORY_16X", "MEMORY_16X_x86", "MEMORY_64X", "MEMORY_64X_x86"
    );
    
    // Required properties
    private String name;
    
    // Optional configuration properties
    private String type;
    private String size;
    private Integer minClusterCount;
    private Integer maxClusterCount;
    private Integer autoSuspend;
    private Boolean autoResume;
    private String resourceMonitor;
    private String comment;
    private Boolean enableQueryAcceleration;
    private Integer queryAccelerationMaxScaleFactor;
    private String scalingPolicy;
    private String resourceConstraint;
    
    // State properties (read-only, from SHOW WAREHOUSES)
    private String state;
    private Integer startedClusters;
    private Integer running;
    private Integer queued;
    private Boolean isDefault;
    private Boolean isCurrent;
    private Float available;
    private Float provisioning;
    private Float quiescing;
    private Float other;
    private Date createdOn;
    private Date resumedOn;
    private Date updatedOn;
    private String owner;
    private String ownerRoleType;

    public Warehouse() {
        // Empty constructor
    }

    @Override
    public String getSnapshotId() {
        return getName();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Warehouse setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Warehouse name cannot be null or empty");
        }
        this.name = name.toUpperCase(); // Snowflake stores identifiers in uppercase
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        // Warehouses exist at account level, not within schemas
        return null;
    }

    @Override
    public Schema getSchema() {
        // Warehouses don't belong to schemas in Snowflake
        return null;
    }

    // Optional Configuration Properties

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null && !VALID_TYPES.contains(type.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid warehouse type: " + type + 
                ". Valid types are: " + VALID_TYPES
            );
        }
        this.type = type != null ? type.toUpperCase() : null;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        if (size != null) {
            String upperSize = size.toUpperCase();
            // Normalize for validation (remove hyphens for comparison)
            String normalizedSize = upperSize.replace("-", "");
            if (!VALID_SIZES.contains(normalizedSize)) {
                throw new IllegalArgumentException(
                    "Invalid warehouse size: " + size + 
                    ". Valid sizes are: " + VALID_SIZES
                );
            }
            // Store the original format from Snowflake
            this.size = upperSize;
        } else {
            this.size = size;
        }
    }

    public Integer getMinClusterCount() {
        return minClusterCount;
    }

    public void setMinClusterCount(Integer minClusterCount) {
        if (minClusterCount != null && minClusterCount < 1) {
            throw new IllegalArgumentException("min_cluster_count must be at least 1");
        }
        this.minClusterCount = minClusterCount;
    }

    public Integer getMaxClusterCount() {
        return maxClusterCount;
    }

    public void setMaxClusterCount(Integer maxClusterCount) {
        if (maxClusterCount != null && minClusterCount != null && maxClusterCount < minClusterCount) {
            throw new IllegalArgumentException("max_cluster_count cannot be less than min_cluster_count");
        }
        this.maxClusterCount = maxClusterCount;
    }

    public Integer getAutoSuspend() {
        return autoSuspend;
    }

    public void setAutoSuspend(Integer autoSuspend) {
        if (autoSuspend != null && autoSuspend < 0) {
            throw new IllegalArgumentException("auto_suspend cannot be negative");
        }
        this.autoSuspend = autoSuspend;
    }

    public Boolean getAutoResume() {
        return autoResume;
    }

    public void setAutoResume(Boolean autoResume) {
        this.autoResume = autoResume;
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
        if (queryAccelerationMaxScaleFactor != null && 
            (queryAccelerationMaxScaleFactor < 0 || queryAccelerationMaxScaleFactor > 100)) {
            throw new IllegalArgumentException(
                "query_acceleration_max_scale_factor must be between 0 and 100"
            );
        }
        this.queryAccelerationMaxScaleFactor = queryAccelerationMaxScaleFactor;
    }

    public String getScalingPolicy() {
        return scalingPolicy;
    }

    public void setScalingPolicy(String scalingPolicy) {
        if (scalingPolicy != null && !VALID_SCALING_POLICIES.contains(scalingPolicy.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid scaling policy: " + scalingPolicy + 
                ". Valid policies are: " + VALID_SCALING_POLICIES
            );
        }
        this.scalingPolicy = scalingPolicy != null ? scalingPolicy.toUpperCase() : null;
    }

    public String getResourceConstraint() {
        return resourceConstraint;
    }

    public void setResourceConstraint(String resourceConstraint) {
        if (resourceConstraint != null && !VALID_RESOURCE_CONSTRAINTS.contains(resourceConstraint.toUpperCase())) {
            throw new IllegalArgumentException(
                "Invalid resource constraint: " + resourceConstraint + 
                ". Valid constraints are: " + VALID_RESOURCE_CONSTRAINTS
            );
        }
        this.resourceConstraint = resourceConstraint != null ? resourceConstraint.toUpperCase() : null;
    }

    // State Properties (Read-Only)

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getStartedClusters() {
        return startedClusters;
    }

    public void setStartedClusters(Integer startedClusters) {
        this.startedClusters = startedClusters;
    }

    public Integer getRunning() {
        return running;
    }

    public void setRunning(Integer running) {
        this.running = running;
    }

    public Integer getQueued() {
        return queued;
    }

    public void setQueued(Integer queued) {
        this.queued = queued;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
    }

    public Float getAvailable() {
        return available;
    }

    public void setAvailable(Float available) {
        this.available = available;
    }

    public Float getProvisioning() {
        return provisioning;
    }

    public void setProvisioning(Float provisioning) {
        this.provisioning = provisioning;
    }

    public Float getQuiescing() {
        return quiescing;
    }

    public void setQuiescing(Float quiescing) {
        this.quiescing = quiescing;
    }

    public Float getOther() {
        return other;
    }

    public void setOther(Float other) {
        this.other = other;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Date getResumedOn() {
        return resumedOn;
    }

    public void setResumedOn(Date resumedOn) {
        this.resumedOn = resumedOn;
    }

    public Date getUpdatedOn() {
        return updatedOn;
    }

    public void setUpdatedOn(Date updatedOn) {
        this.updatedOn = updatedOn;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwnerRoleType() {
        return ownerRoleType;
    }

    public void setOwnerRoleType(String ownerRoleType) {
        this.ownerRoleType = ownerRoleType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        Warehouse warehouse = (Warehouse) o;
        return Objects.equals(name, warehouse.name) &&
               Objects.equals(type, warehouse.type) &&
               Objects.equals(size, warehouse.size) &&
               Objects.equals(minClusterCount, warehouse.minClusterCount) &&
               Objects.equals(maxClusterCount, warehouse.maxClusterCount) &&
               Objects.equals(autoSuspend, warehouse.autoSuspend) &&
               Objects.equals(autoResume, warehouse.autoResume) &&
               Objects.equals(resourceMonitor, warehouse.resourceMonitor) &&
               Objects.equals(comment, warehouse.comment) &&
               Objects.equals(enableQueryAcceleration, warehouse.enableQueryAcceleration) &&
               Objects.equals(queryAccelerationMaxScaleFactor, warehouse.queryAccelerationMaxScaleFactor) &&
               Objects.equals(scalingPolicy, warehouse.scalingPolicy) &&
               Objects.equals(resourceConstraint, warehouse.resourceConstraint);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, size, minClusterCount, maxClusterCount,
                          autoSuspend, autoResume, resourceMonitor, comment, enableQueryAcceleration,
                          queryAccelerationMaxScaleFactor, scalingPolicy, resourceConstraint);
    }
}