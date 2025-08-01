package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a Snowflake database object.
 */
public class Database extends AbstractDatabaseObject {
    
    // Required properties
    private String name;
    
    // Optional configuration properties
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean transient_;
    private String defaultDdlCollation;
    private String resourceMonitor;
    
    // State properties (read-only, from SHOW DATABASES)
    private Date createdOn;
    private String origin;
    private String owner;
    private String ownerRoleType;
    private String retention_time;
    private String kind;
    private Boolean isTransient;
    private Boolean isCurrent;
    private Boolean isDefault;
    private String resourceMonitorName;
    private Date droppedOn;
    private Date lastAltered;
    private String budget;

    public Database() {
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
    public Database setName(String name) {
        if (StringUtil.isEmpty(name)) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        this.name = name.toUpperCase(); // Snowflake stores identifiers in uppercase
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        // Databases exist at account level, not within schemas
        return null;
    }

    @Override
    public String getObjectTypeName() {
        return "database";
    }

    @Override
    public Schema getSchema() {
        // Databases exist at account level, not within schemas
        return null;
    }

    @Override
    public boolean snapshotByDefault() {
        return true;
    }

    // Configuration Properties Getters and Setters

    public String getComment() {
        return comment;
    }

    public Database setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public Database setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
        return this;
    }

    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public Database setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
        return this;
    }

    public Boolean getTransient() {
        return transient_;
    }

    public Database setTransient(Boolean transient_) {
        this.transient_ = transient_;
        return this;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public Database setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
        return this;
    }

    public String getResourceMonitor() {
        return resourceMonitor;
    }

    public Database setResourceMonitor(String resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
        return this;
    }

    // State Properties Getters and Setters (read-only from database)

    public Date getCreatedOn() {
        return createdOn;
    }

    public Database setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
        return this;
    }

    public String getOrigin() {
        return origin;
    }

    public Database setOrigin(String origin) {
        this.origin = origin;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public Database setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public String getOwnerRoleType() {
        return ownerRoleType;
    }

    public Database setOwnerRoleType(String ownerRoleType) {
        this.ownerRoleType = ownerRoleType;
        return this;
    }

    public String getRetention_time() {
        return retention_time;
    }

    public Database setRetention_time(String retention_time) {
        this.retention_time = retention_time;
        return this;
    }

    public String getKind() {
        return kind;
    }

    public Database setKind(String kind) {
        this.kind = kind;
        return this;
    }

    public Boolean getIsTransient() {
        return isTransient;
    }

    public Database setIsTransient(Boolean isTransient) {
        this.isTransient = isTransient;
        return this;
    }

    public Boolean getIsCurrent() {
        return isCurrent;
    }

    public Database setIsCurrent(Boolean isCurrent) {
        this.isCurrent = isCurrent;
        return this;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public Database setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    public String getResourceMonitorName() {
        return resourceMonitorName;
    }

    public Database setResourceMonitorName(String resourceMonitorName) {
        this.resourceMonitorName = resourceMonitorName;
        return this;
    }

    public Date getDroppedOn() {
        return droppedOn;
    }

    public Database setDroppedOn(Date droppedOn) {
        this.droppedOn = droppedOn;
        return this;
    }

    public Date getLastAltered() {
        return lastAltered;
    }

    public Database setLastAltered(Date lastAltered) {
        this.lastAltered = lastAltered;
        return this;
    }

    public String getBudget() {
        return budget;
    }

    public Database setBudget(String budget) {
        this.budget = budget;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Database database = (Database) o;
        return Objects.equals(name, database.name) &&
               Objects.equals(comment, database.comment) &&
               Objects.equals(dataRetentionTimeInDays, database.dataRetentionTimeInDays) &&
               Objects.equals(maxDataExtensionTimeInDays, database.maxDataExtensionTimeInDays) &&
               Objects.equals(transient_, database.transient_) &&
               Objects.equals(defaultDdlCollation, database.defaultDdlCollation) &&
               Objects.equals(resourceMonitor, database.resourceMonitor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, dataRetentionTimeInDays, maxDataExtensionTimeInDays, 
                           transient_, defaultDdlCollation, resourceMonitor);
    }

    @Override
    public String toString() {
        return "Database{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", dataRetentionTimeInDays='" + dataRetentionTimeInDays + '\'' +
                ", maxDataExtensionTimeInDays='" + maxDataExtensionTimeInDays + '\'' +
                ", transient=" + transient_ +
                ", defaultDdlCollation='" + defaultDdlCollation + '\'' +
                ", resourceMonitor='" + resourceMonitor + '\'' +
                ", owner='" + owner + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }

    /**
     * Validates the database configuration properties.
     * Used for snapshot comparison and validation.
     */
    public boolean isValidConfiguration() {
        // Basic validation - name is required
        if (StringUtil.isEmpty(name)) {
            return false;
        }
        
        // Additional validation can be added here
        return true;
    }
}