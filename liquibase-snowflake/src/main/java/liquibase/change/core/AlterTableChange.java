package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableStatement;

/**
 * Snowflake-specific change for altering table properties.
 * Supports clustering operations, data retention settings, and other Snowflake-specific table modifications.
 */
@DatabaseChange(
    name = "alterTable",
    description = "Alter table properties in Snowflake",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "table"
)
public class AlterTableChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    
    // Clustering operations (mutually exclusive)
    private String clusterBy;
    private Boolean dropClusteringKey;
    private Boolean suspendRecluster;
    private Boolean resumeRecluster;
    
    // Property settings (can be combined)
    private Integer setDataRetentionTimeInDays;
    private Boolean setChangeTracking;
    private Boolean setEnableSchemaEvolution;

    @DatabaseChangeProperty(description = "Name of the catalog")
    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
    }

    @DatabaseChangeProperty(description = "Name of the schema")
    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    @DatabaseChangeProperty(description = "Name of the table", mustEqualExisting = "table")
    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @DatabaseChangeProperty(description = "Comma-separated list of columns to cluster by")
    public String getClusterBy() {
        return clusterBy;
    }

    public void setClusterBy(String clusterBy) {
        this.clusterBy = clusterBy;
    }

    @DatabaseChangeProperty(description = "Drop the clustering key from the table")
    public Boolean getDropClusteringKey() {
        return dropClusteringKey;
    }

    public void setDropClusteringKey(Boolean dropClusteringKey) {
        this.dropClusteringKey = dropClusteringKey;
    }

    @DatabaseChangeProperty(description = "Suspend automatic reclustering for the table")
    public Boolean getSuspendRecluster() {
        return suspendRecluster;
    }

    public void setSuspendRecluster(Boolean suspendRecluster) {
        this.suspendRecluster = suspendRecluster;
    }

    @DatabaseChangeProperty(description = "Resume automatic reclustering for the table")
    public Boolean getResumeRecluster() {
        return resumeRecluster;
    }

    public void setResumeRecluster(Boolean resumeRecluster) {
        this.resumeRecluster = resumeRecluster;
    }

    @DatabaseChangeProperty(description = "Set data retention time in days (0-90)")
    public Integer getSetDataRetentionTimeInDays() {
        return setDataRetentionTimeInDays;
    }

    public void setSetDataRetentionTimeInDays(Integer setDataRetentionTimeInDays) {
        this.setDataRetentionTimeInDays = setDataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Enable or disable change tracking")
    public Boolean getSetChangeTracking() {
        return setChangeTracking;
    }

    public void setSetChangeTracking(Boolean setChangeTracking) {
        this.setChangeTracking = setChangeTracking;
    }

    @DatabaseChangeProperty(description = "Enable or disable schema evolution")  
    public Boolean getSetEnableSchemaEvolution() {
        return setEnableSchemaEvolution;
    }

    public void setSetEnableSchemaEvolution(Boolean setEnableSchemaEvolution) {
        this.setEnableSchemaEvolution = setEnableSchemaEvolution;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterTableStatement statement = new AlterTableStatement(
            getCatalogName(),
            getSchemaName(),
            getTableName()
        );
        
        // Set clustering operations
        statement.setClusterBy(getClusterBy());
        statement.setDropClusteringKey(getDropClusteringKey());
        statement.setSuspendRecluster(getSuspendRecluster());
        statement.setResumeRecluster(getResumeRecluster());
        
        // Set property operations
        statement.setSetDataRetentionTimeInDays(getSetDataRetentionTimeInDays());
        statement.setSetChangeTracking(getSetChangeTracking());
        statement.setSetEnableSchemaEvolution(getSetEnableSchemaEvolution());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public String getConfirmationMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Table ").append(getTableName()).append(" altered");
        
        if (clusterBy != null) {
            message.append(" - clustering set to (").append(clusterBy).append(")");
        } else if (Boolean.TRUE.equals(dropClusteringKey)) {
            message.append(" - clustering key dropped");
        } else if (Boolean.TRUE.equals(suspendRecluster)) {
            message.append(" - reclustering suspended");
        } else if (Boolean.TRUE.equals(resumeRecluster)) {
            message.append(" - reclustering resumed");
        }
        
        if (setDataRetentionTimeInDays != null) {
            message.append(" - data retention set to ").append(setDataRetentionTimeInDays).append(" days");
        }
        
        if (setChangeTracking != null) {
            message.append(" - change tracking ").append(setChangeTracking ? "enabled" : "disabled");
        }
        
        if (setEnableSchemaEvolution != null) {
            message.append(" - schema evolution ").append(setEnableSchemaEvolution ? "enabled" : "disabled");
        }
        
        return message.toString();
    }

    @Override
    public boolean supportsRollback(Database database) {
        // ALTER TABLE operations can be complex to rollback, especially clustering changes
        return false;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);

        if (tableName == null) {
            validationErrors.addError("tableName is required");
        }

        // Validate mutual exclusivity - only one clustering operation per change
        int clusteringOperationCount = 0;
        if (clusterBy != null) clusteringOperationCount++;
        if (Boolean.TRUE.equals(dropClusteringKey)) clusteringOperationCount++;
        if (Boolean.TRUE.equals(suspendRecluster)) clusteringOperationCount++;
        if (Boolean.TRUE.equals(resumeRecluster)) clusteringOperationCount++;

        if (clusteringOperationCount > 1) {
            validationErrors.addError("Only one clustering operation allowed per alterTable change");
        }

        // Validate data retention time range
        if (setDataRetentionTimeInDays != null) {
            if (setDataRetentionTimeInDays < 0 || setDataRetentionTimeInDays > 90) {
                validationErrors.addError("setDataRetentionTimeInDays must be between 0 and 90");
            }
        }

        // At least one operation must be specified
        boolean hasOperation = clusteringOperationCount > 0 || 
                              setDataRetentionTimeInDays != null ||
                              setChangeTracking != null ||
                              setEnableSchemaEvolution != null;
        
        if (!hasOperation) {
            validationErrors.addError("At least one Snowflake-specific operation must be specified");
        }

        return validationErrors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}