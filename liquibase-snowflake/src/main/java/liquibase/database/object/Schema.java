package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;

/**
 * Represents a Snowflake SCHEMA database object with complete configuration and state properties.
 * Supports snapshot and diff operations for schema management.
 * 
 * ADDRESSES_CORE_ISSUE: Complete schema object implementation with all properties for snapshot/diff.
 */
public class Schema extends AbstractDatabaseObject {
    
    // Configuration Properties (mutable via ALTER SCHEMA)
    private String comment;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private String defaultDdlCollation;
    private String cloneFrom;
    private Boolean isTransient;
    private Boolean isManagedAccess;
    private Boolean withTag;
    
    // State Properties (immutable runtime state - excluded from diffs)
    private String createdOn;
    private String owner;
    private String ownerRoleType;
    private Boolean isDefault;
    private Boolean isCurrent;
    private String retentionTime;
    private String droppedOn;
    private String kind;
    private String resourceMonitorName;
    private String budget;
    private String lastAltered;
    private String origin;
    private String databaseName;

    public Schema() {
        setName(null);
    }

    public Schema(String schemaName) {
        setName(schemaName);
    }

    @Override
    public String getObjectTypeName() {
        return "schema";
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        return null;
    }

    @Override
    public String getName() {
        return getAttribute("name", String.class);
    }

    @Override
    public Schema setName(String name) {
        this.setAttribute("name", name);
        return this;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_SNAPSHOT_NAMESPACE;
    }

    @Override
    public boolean snapshotByDefault() {
        return true;
    }

    @Override
    public liquibase.structure.core.Schema getSchema() {
        return null; // This IS the schema object
    }

    // Configuration Properties (included in diffs)

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public void setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public void setDefaultDdlCollation(String defaultDdlCollation) {
        this.defaultDdlCollation = defaultDdlCollation;
    }

    public String getCloneFrom() {
        return cloneFrom;
    }

    public void setCloneFrom(String cloneFrom) {
        this.cloneFrom = cloneFrom;
    }

    public Boolean getTransient() {
        return isTransient;
    }

    public void setTransient(Boolean isTransient) {
        this.isTransient = isTransient;
    }

    public Boolean getManagedAccess() {
        return isManagedAccess;
    }

    public void setManagedAccess(Boolean managedAccess) {
        isManagedAccess = managedAccess;
    }

    public Boolean getWithTag() {
        return withTag;
    }

    public void setWithTag(Boolean withTag) {
        this.withTag = withTag;
    }

    // State Properties (excluded from diffs)

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
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

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getCurrent() {
        return isCurrent;
    }

    public void setCurrent(Boolean current) {
        isCurrent = current;
    }

    public String getRetentionTime() {
        return retentionTime;
    }

    public void setRetentionTime(String retentionTime) {
        this.retentionTime = retentionTime;
    }

    public String getDroppedOn() {
        return droppedOn;
    }

    public void setDroppedOn(String droppedOn) {
        this.droppedOn = droppedOn;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getResourceMonitorName() {
        return resourceMonitorName;
    }

    public void setResourceMonitorName(String resourceMonitorName) {
        this.resourceMonitorName = resourceMonitorName;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public String getLastAltered() {
        return lastAltered;
    }

    public void setLastAltered(String lastAltered) {
        this.lastAltered = lastAltered;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    public int compareTo(Object other) {
        Schema o = (Schema) other;
        return this.getName().compareTo(o.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schema schema = (Schema) o;

        return getName() != null ? getName().equals(schema.getName()) : schema.getName() == null;
    }

    @Override
    public int hashCode() {
        return getName() != null ? getName().hashCode() : 0;
    }

    @Override
    public String toString() {
        return getName();
    }
}