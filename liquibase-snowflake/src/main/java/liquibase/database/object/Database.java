package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.structure.core.Schema;
import liquibase.util.StringUtil;

import java.util.Date;
import java.util.Objects;

/**
 * Represents a Snowflake database object.
 * Based on snowflake_database_snapshot_diff_requirements.md
 */
public class Database extends AbstractDatabaseObject {
    
    // Required properties
    private String name;
    
    // Optional configuration properties (XSD attributes)
    private String comment;
    private Integer dataRetentionTimeInDays;
    private Integer maxDataExtensionTimeInDays;
    private Boolean transient_;
    private String defaultDdlCollation;
    private String tag;
    
    // Iceberg database attributes
    private String externalVolume;
    private String catalog;
    private String storageSerializationPolicy;
    
    // Creation-only operational attributes
    private Boolean orReplace;
    private Boolean ifNotExists;
    
    // State properties (read-only, from database queries - excluded from diff)
    private String owner;
    private String databaseType;
    private Date created;
    private Date lastAltered;
    private String ownerRoleType;
    
    // Catalog relationship
    private Catalog catalogObject;

    public Database() {
        setSnapshotId("name");
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
        if (StringUtil.isEmpty(name) || (name != null && name.trim().isEmpty())) {
            throw new IllegalArgumentException("Database name cannot be null or empty");
        }
        this.name = name.toUpperCase(); // Snowflake stores identifiers in uppercase
        return this;
    }

    @Override
    public DatabaseObject[] getContainingObjects() {
        // Databases exist at account level, no containing objects
        return new DatabaseObject[0];
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
        // Treat empty strings as null for consistency
        if (comment != null && comment.trim().isEmpty()) {
            comment = null;
        }
        this.comment = comment;
        return this;
    }

    public Integer getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public Database setDataRetentionTimeInDays(Integer dataRetentionTimeInDays) {
        if (dataRetentionTimeInDays != null && (dataRetentionTimeInDays < 0 || dataRetentionTimeInDays > 90)) {
            throw new IllegalArgumentException("Data retention time must be between 0 and 90 days for permanent databases");
        }
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
        return this;
    }

    public Integer getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public Database setMaxDataExtensionTimeInDays(Integer maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
        return this;
    }

    public Boolean getTransient() {
        return transient_ != null ? transient_ : false;
    }

    public Database setTransient(Boolean transient_) {
        this.transient_ = transient_;
        return this;
    }

    public String getDefaultDdlCollation() {
        return defaultDdlCollation;
    }

    public Database setDefaultDdlCollation(String defaultDdlCollation) {
        if (defaultDdlCollation != null && defaultDdlCollation.trim().isEmpty()) {
            defaultDdlCollation = null;
        }
        this.defaultDdlCollation = defaultDdlCollation;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Database setTag(String tag) {
        this.tag = tag;
        return this;
    }
    
    // Iceberg database attributes
    public String getExternalVolume() {
        return externalVolume;
    }

    public Database setExternalVolume(String externalVolume) {
        this.externalVolume = externalVolume;
        return this;
    }
    
    public String getCatalogString() {
        return catalog;
    }

    public Database setCatalogString(String catalog) {
        this.catalog = catalog;
        return this;
    }
    
    public String getStorageSerializationPolicy() {
        return storageSerializationPolicy;
    }

    public Database setStorageSerializationPolicy(String storageSerializationPolicy) {
        this.storageSerializationPolicy = storageSerializationPolicy;
        return this;
    }
    
    // Creation-only operational attributes
    public Boolean getOrReplace() {
        return orReplace != null ? orReplace : false;
    }

    public Database setOrReplace(Boolean orReplace) {
        this.orReplace = orReplace;
        return this;
    }
    
    public Boolean getIfNotExists() {
        return ifNotExists != null ? ifNotExists : false;
    }

    public Database setIfNotExists(Boolean ifNotExists) {
        this.ifNotExists = ifNotExists;
        return this;
    }

    // State Properties Getters and Setters (read-only from database - excluded from diff)
    
    public Date getCreated() {
        return created;
    }

    public Database setCreated(Date created) {
        this.created = created;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public Database setOwner(String owner) {
        this.owner = owner;
        return this;
    }
    
    public String getDatabaseType() {
        return databaseType;
    }

    public Database setDatabaseType(String databaseType) {
        this.databaseType = databaseType;
        return this;
    }

    public String getOwnerRoleType() {
        return ownerRoleType;
    }

    public Database setOwnerRoleType(String ownerRoleType) {
        this.ownerRoleType = ownerRoleType;
        return this;
    }
    
    public Date getLastAltered() {
        return lastAltered;
    }

    public Database setLastAltered(Date lastAltered) {
        this.lastAltered = lastAltered;
        return this;
    }
    
    // Catalog relationship
    public Catalog getCatalog() {
        return catalogObject;
    }

    public Database setCatalog(Catalog catalog) {
        this.catalogObject = catalog;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Database database = (Database) o;
        return Objects.equals(name, database.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Database{" +
                "name='" + name + '\'' +
                ", comment='" + comment + '\'' +
                ", dataRetentionTimeInDays=" + dataRetentionTimeInDays +
                ", transient=" + transient_ +
                ", owner='" + owner + '\'' +
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