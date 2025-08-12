package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterStageStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * Professional implementation using generic property storage pattern.
 * Handles ALTER STAGE operations including SET, UNSET, RENAME, and REFRESH.
 */
@DatabaseChange(
    name = "alterStage",
    description = "Alters a stage",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "stage",
    since = "4.33"
)
public class AlterStageChange extends AbstractChange {

    // PROFESSIONAL PATTERN: Generic property storage
    private Map<String, String> objectProperties = new HashMap<>();
    private String stageName; // Core required property
    
    @DatabaseChangeProperty(
        description = "Name of the stage to alter", 
        requiredForDatabase = "snowflake"
    )
    public void setStageName(String stageName) {
        this.stageName = stageName;
    }
    
    public String getStageName() {
        return stageName;
    }
    
    // PROFESSIONAL PATTERN: Generic property storage methods
    public void setObjectProperty(String propertyName, String propertyValue) {
        if (propertyValue != null) {
            objectProperties.put(propertyName, propertyValue);
        } else {
            // Remove the property if setting to null
            objectProperties.remove(propertyName);
        }
    }
    
    public String getObjectProperty(String propertyName) {
        return objectProperties.get(propertyName);
    }
    
    public Map<String, String> getAllObjectProperties() {
        return new HashMap<>(objectProperties);
    }
    
    // Core properties
    public void setCatalogName(String catalogName) { setObjectProperty("catalogName", catalogName); }
    public void setSchemaName(String schemaName) { setObjectProperty("schemaName", schemaName); }
    public void setIfExists(Boolean ifExists) { setObjectProperty("ifExists", ifExists != null ? ifExists.toString() : null); }
    
    // Rename operation
    public void setRenameTo(String renameTo) { setObjectProperty("renameTo", renameTo); }
    
    // SET operations (for setting properties) - Store with plain names for retrieval
    public void setUrl(String url) { setObjectProperty("url", url); }
    public void setStorageIntegration(String storageIntegration) { setObjectProperty("storageIntegration", storageIntegration); }
    public void setComment(String comment) { setObjectProperty("comment", comment); }
    public void setEncryption(String encryption) { setObjectProperty("encryption", encryption); }
    public void setFileFormat(String fileFormat) { setObjectProperty("fileFormat", fileFormat); }
    public void setDirectoryEnable(Boolean directoryEnable) { setObjectProperty("directoryEnable", directoryEnable != null ? directoryEnable.toString() : null); }
    
    // SET credentials operations
    public void setAwsKeyId(String awsKeyId) { setObjectProperty("awsKeyId", awsKeyId); }
    public void setAwsSecretKey(String awsSecretKey) { setObjectProperty("awsSecretKey", awsSecretKey); }
    public void setAwsToken(String awsToken) { setObjectProperty("awsToken", awsToken); }
    public void setAwsRole(String awsRole) { setObjectProperty("awsRole", awsRole); }
    public void setGcsServiceAccountKey(String gcsKey) { setObjectProperty("gcsServiceAccountKey", gcsKey); }
    public void setAzureAccountName(String azureAccount) { setObjectProperty("azureAccountName", azureAccount); }
    public void setAzureAccountKey(String azureKey) { setObjectProperty("azureAccountKey", azureKey); }
    public void setAzureSasToken(String azureSas) { setObjectProperty("azureSasToken", azureSas); }
    
    // SET TAG operations
    public void setTagName(String tagName) { setObjectProperty("tagName", tagName); }
    public void setTagValue(String tagValue) { setObjectProperty("tagValue", tagValue); }
    
    // UNSET operations (for removing properties)
    public void setUnsetUrl(Boolean unsetUrl) { setObjectProperty("unsetUrl", unsetUrl != null ? unsetUrl.toString() : null); }
    public void setUnsetStorageIntegration(Boolean unsetStorageIntegration) { setObjectProperty("unsetStorageIntegration", unsetStorageIntegration != null ? unsetStorageIntegration.toString() : null); }
    public void setUnsetCredentials(Boolean unsetCredentials) { setObjectProperty("unsetCredentials", unsetCredentials != null ? unsetCredentials.toString() : null); }
    public void setUnsetEncryption(Boolean unsetEncryption) { setObjectProperty("unsetEncryption", unsetEncryption != null ? unsetEncryption.toString() : null); }
    public void setUnsetFileFormat(Boolean unsetFileFormat) { setObjectProperty("unsetFileFormat", unsetFileFormat != null ? unsetFileFormat.toString() : null); }
    public void setUnsetComment(Boolean unsetComment) { setObjectProperty("unsetComment", unsetComment != null ? unsetComment.toString() : null); }
    
    // UNSET TAG operations
    public void setUnsetTagName(String unsetTagName) { setObjectProperty("unsetTagName", unsetTagName); }
    
    // REFRESH operations
    public void setRefreshDirectory(Boolean refreshDirectory) { setObjectProperty("refreshDirectory", refreshDirectory != null ? refreshDirectory.toString() : null); }
    public void setRefreshSubpath(String refreshSubpath) { setObjectProperty("refreshSubpath", refreshSubpath); }
    
    // Getters using generic storage
    public String getCatalogName() { return getObjectProperty("catalogName"); }
    public String getSchemaName() { return getObjectProperty("schemaName"); }
    public Boolean getIfExists() { 
        String val = getObjectProperty("ifExists");
        return val != null ? Boolean.valueOf(val) : null;
    }
    public String getRenameTo() { return getObjectProperty("renameTo"); }
    
    // Property getters for tests
    public String getUrl() { return getObjectProperty("url"); }
    public String getStorageIntegration() { return getObjectProperty("storageIntegration"); }
    public String getComment() { return getObjectProperty("comment"); }
    public String getEncryption() { return getObjectProperty("encryption"); }
    public String getFileFormat() { return getObjectProperty("fileFormat"); }
    public Boolean getDirectoryEnable() { String val = getObjectProperty("directoryEnable"); return val != null ? Boolean.valueOf(val) : null; }
    public String getAwsKeyId() { return getObjectProperty("awsKeyId"); }
    public String getAwsSecretKey() { return getObjectProperty("awsSecretKey"); }
    public String getAzureAccountName() { return getObjectProperty("azureAccountName"); }
    public String getGcsServiceAccountKey() { return getObjectProperty("gcsServiceAccountKey"); }
    public String getTagName() { return getObjectProperty("tagName"); }
    public String getTagValue() { return getObjectProperty("tagValue"); }
    
    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }
    
    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = new ValidationErrors(); // Create fresh errors, don't call super yet
        
        if (stageName == null || stageName.trim().isEmpty()) {
            errors.addError("stageName is required");
            return errors; // Return early if stageName is missing - other validations not meaningful
        }
        
        // Now call super validation since stageName is valid
        errors.addAll(super.validate(database));
        
        // Validate that at least one operation is specified
        boolean hasSetOperation = hasAnySetOperation();
        boolean hasUnsetOperation = hasAnyUnsetOperation();
        boolean hasRenameOperation = getObjectProperty("renameTo") != null;
        boolean hasRefreshOperation = getObjectProperty("refreshDirectory") != null;
        
        if (!hasSetOperation && !hasUnsetOperation && !hasRenameOperation && !hasRefreshOperation) {
            errors.addError("ALTER STAGE requires at least one SET, UNSET, RENAME, or REFRESH operation");
        }
        
        // Validate SET vs UNSET mutual exclusivity for same properties
        if (hasConflictingSetUnset()) {
            errors.addError("Cannot both SET and UNSET the same property");
        }
        
        // Validate REFRESH directory requires external stage (will be checked by Snowflake)
        // Let Snowflake handle detailed validation (honest approach)
        
        return errors;
    }
    
    boolean hasAnySetOperation() {
        return objectProperties.entrySet().stream()
            .anyMatch(entry -> {
                String key = entry.getKey();
                // Check for actual property names that represent SET operations
                return !key.startsWith("unset") && 
                       !key.equals("renameTo") && 
                       !key.equals("refreshDirectory") && 
                       !key.equals("refreshSubpath") && 
                       !key.equals("catalogName") && 
                       !key.equals("schemaName") && 
                       !key.equals("ifExists") &&
                       !key.equals("tagName") &&
                       !key.equals("tagValue");
            });
    }
    
    boolean hasAnyUnsetOperation() {
        return objectProperties.entrySet().stream()
            .anyMatch(entry -> entry.getKey().startsWith("unset"));
    }
    
    boolean hasConflictingSetUnset() {
        // Check for conflicting SET/UNSET operations on same properties
        return (objectProperties.containsKey("url") && objectProperties.containsKey("unsetUrl")) ||
               (objectProperties.containsKey("storageIntegration") && objectProperties.containsKey("unsetStorageIntegration")) ||
               (objectProperties.containsKey("awsKeyId") && objectProperties.containsKey("unsetCredentials")) ||
               (objectProperties.containsKey("encryption") && objectProperties.containsKey("unsetEncryption")) ||
               (objectProperties.containsKey("fileFormat") && objectProperties.containsKey("unsetFileFormat")) ||
               (objectProperties.containsKey("comment") && objectProperties.containsKey("unsetComment"));
    }
    
    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterStageStatement statement = new AlterStageStatement();
        statement.setStageName(stageName);
        
        // Apply all generic properties
        for (Map.Entry<String, String> entry : objectProperties.entrySet()) {
            statement.setObjectProperty(entry.getKey(), entry.getValue());
        }
        
        return new SqlStatement[]{statement};
    }
    
    @Override
    public String getConfirmationMessage() {
        if (getObjectProperty("renameTo") != null) {
            return "Stage " + stageName + " renamed to " + getObjectProperty("renameTo");
        } else {
            return "Stage " + stageName + " altered";
        }
    }
}