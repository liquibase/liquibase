package liquibase.database.object;

import liquibase.structure.AbstractDatabaseObject;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.util.Date;
import java.util.Objects;
import java.util.Map;
import java.util.HashMap;

/**
 * Database object representing a Snowflake Stage.
 * Schema-level object implementation following professional patterns.
 */
public class Stage extends AbstractDatabaseObject {
    
    // REQUIRED: Primary identifier
    private String stageName;
    
    // REQUIRED: Schema reference for schema-level objects
    private Schema schema;
    
    // Configuration properties (included in diff comparison)
    private String url;               // External stage URL (s3://, gcs://, azure://)
    private String stageType;         // "Internal Named" or "External Named"
    private String stageRegion;       // Cloud region
    private String storageIntegration; // Storage integration name
    private String comment;           // User comment
    
    // State properties (excluded from diff comparison)
    private String owner;             // System-managed ownership
    private Date created;            // Creation timestamp
    private Date lastAltered;        // Last modification timestamp
    
    // Operational properties (from SHOW STAGES)
    private Boolean hasCredentials;      // Whether stage has credentials configured
    private Boolean hasEncryptionKey;   // Whether stage has encryption key
    private String cloud;               // Cloud provider (AWS/GCP/AZURE)
    private Boolean directoryEnabled;   // Whether directory table is enabled
    
    // Enhanced properties (from ACCOUNT_USAGE.STAGES)
    private Long stageId;               // System-generated unique ID
    
    // Tag properties (from ACCOUNT_USAGE.TAG_REFERENCES)
    private Map<String, String> tags;   // Tag metadata (TAG_NAME -> TAG_VALUE)
    
    // Detailed configuration (from DESCRIBE STAGE - optional)
    private Map<String, String> detailedConfiguration; // Detailed per-stage configuration
    
    // REQUIRED: Default constructor
    public Stage() {
        super();
    }
    
    // REQUIRED: Constructor with name
    public Stage(String stageName) {
        this();
        this.stageName = stageName;
    }
    
    // REQUIRED: Object type name for Liquibase framework
    @Override
    public String getObjectTypeName() {
        return "stage";
    }
    
    // REQUIRED: Name getter (used by framework)
    @Override
    public String getName() {
        return stageName;
    }
    
    // REQUIRED: Name setter (used by framework)
    @Override
    public Stage setName(String name) {
        this.stageName = name;
        return this;
    }
    
    // REQUIRED: Schema-level objects should be snapshotted by default
    @Override
    public boolean snapshotByDefault() {
        return true;
    }
    
    // REQUIRED: Container objects for schema-level objects
    @Override
    public DatabaseObject[] getContainingObjects() {
        return schema != null ? new DatabaseObject[] { schema } : null;
    }
    
    // REQUIRED: Primary identifier getter/setter
    public String getStageName() { 
        return stageName; 
    }
    
    public void setStageName(String stageName) { 
        this.stageName = stageName; 
    }
    
    // REQUIRED: Schema getter/setter for schema-level objects
    public Schema getSchema() { 
        return schema; 
    }
    
    public void setSchema(Schema schema) { 
        this.schema = schema; 
    }
    
    // Configuration property getters/setters
    public String getUrl() { 
        return url; 
    }
    
    public void setUrl(String url) { 
        this.url = url; 
    }
    
    public String getStageType() { 
        return stageType; 
    }
    
    public void setStageType(String stageType) { 
        this.stageType = stageType; 
    }
    
    public String getStageRegion() { 
        return stageRegion; 
    }
    
    public void setStageRegion(String stageRegion) { 
        this.stageRegion = stageRegion; 
    }
    
    public String getStorageIntegration() { 
        return storageIntegration; 
    }
    
    public void setStorageIntegration(String storageIntegration) { 
        this.storageIntegration = storageIntegration; 
    }
    
    public String getComment() { 
        return comment; 
    }
    
    public void setComment(String comment) { 
        this.comment = comment; 
    }
    
    // State property getters/setters (excluded from diff comparison)
    public String getOwner() { 
        return owner; 
    }
    
    public void setOwner(String owner) { 
        this.owner = owner; 
    }
    
    public Date getCreated() { 
        return created; 
    }
    
    public void setCreated(Date created) { 
        this.created = created; 
    }
    
    public Date getLastAltered() { 
        return lastAltered; 
    }
    
    public void setLastAltered(Date lastAltered) { 
        this.lastAltered = lastAltered; 
    }
    
    // Operational property getters/setters
    public Boolean getHasCredentials() { 
        return hasCredentials; 
    }
    
    public void setHasCredentials(Boolean hasCredentials) { 
        this.hasCredentials = hasCredentials; 
    }
    
    public Boolean getHasEncryptionKey() { 
        return hasEncryptionKey; 
    }
    
    public void setHasEncryptionKey(Boolean hasEncryptionKey) { 
        this.hasEncryptionKey = hasEncryptionKey; 
    }
    
    public String getCloud() { 
        return cloud; 
    }
    
    public void setCloud(String cloud) { 
        this.cloud = cloud; 
    }
    
    public Boolean getDirectoryEnabled() { 
        return directoryEnabled; 
    }
    
    public void setDirectoryEnabled(Boolean directoryEnabled) { 
        this.directoryEnabled = directoryEnabled; 
    }
    
    // Enhanced property getters/setters
    public Long getStageId() {
        return stageId;
    }
    
    public void setStageId(Long stageId) {
        this.stageId = stageId;
    }
    
    // Tag property getters/setters
    public Map<String, String> getTags() {
        return tags;
    }
    
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
    
    public void addTag(String tagName, String tagValue) {
        if (this.tags == null) {
            this.tags = new HashMap<>();
        }
        this.tags.put(tagName, tagValue);
    }
    
    public String getTag(String tagName) {
        return tags != null ? tags.get(tagName) : null;
    }
    
    // Detailed configuration getters/setters
    public Map<String, String> getDetailedConfiguration() {
        return detailedConfiguration;
    }
    
    public void setDetailedConfiguration(Map<String, String> detailedConfiguration) {
        this.detailedConfiguration = detailedConfiguration;
    }
    
    public void addDetailedConfigurationProperty(String property, String value) {
        if (this.detailedConfiguration == null) {
            this.detailedConfiguration = new HashMap<>();
        }
        this.detailedConfiguration.put(property, value);
    }
    
    public String getDetailedConfigurationProperty(String property) {
        return detailedConfiguration != null ? detailedConfiguration.get(property) : null;
    }
    
    // REQUIRED: equals() and hashCode() based ONLY on identity fields
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Stage that = (Stage) obj;
        
        if (stageName != null ? !stageName.equals(that.stageName) : that.stageName != null) return false;
        if (schema != null ? !schema.equals(that.schema) : that.schema != null) return false;
        
        return true;
    }
    
    @Override
    public int hashCode() {
        int result = stageName != null ? stageName.hashCode() : 0;
        result = 31 * result + (schema != null ? schema.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "Stage{" +
               "stageName='" + stageName + '\'' +
               ", schema=" + schema +
               ", stageType='" + stageType + '\'' +
               ", url='" + url + '\'' +
               '}';
    }
}