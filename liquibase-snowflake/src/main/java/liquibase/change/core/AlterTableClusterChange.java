package liquibase.change.core;

import liquibase.change.AbstractChange;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AlterTableClusterStatement;

/**
 * Snowflake-specific change for altering table clustering operations.
 * Supports setting clustering keys, dropping clustering keys, and suspending/resuming reclustering.
 */
@DatabaseChange(
    name = "alterTableCluster",
    description = "Alter table clustering configuration in Snowflake",
    priority = 1,
    appliesTo = "table"
)
public class AlterTableClusterChange extends AbstractChange {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String clusterBy;
    private Boolean dropClusteringKey;
    private Boolean suspendRecluster;
    private Boolean resumeRecluster;

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

    @Override
    public String getConfirmationMessage() {
        StringBuilder message = new StringBuilder();
        message.append("Table ").append(getTableName()).append(" clustering");
        
        if (clusterBy != null) {
            message.append(" set to (").append(clusterBy).append(")");
        } else if (Boolean.TRUE.equals(dropClusteringKey)) {
            message.append(" key dropped");
        } else if (Boolean.TRUE.equals(suspendRecluster)) {
            message.append(" suspended");
        } else if (Boolean.TRUE.equals(resumeRecluster)) {
            message.append(" resumed");
        }
        
        return message.toString();
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        AlterTableClusterStatement statement = new AlterTableClusterStatement(
            getCatalogName(),
            getSchemaName(),
            getTableName()
        );
        
        statement.setClusterBy(getClusterBy());
        statement.setDropClusteringKey(getDropClusteringKey());
        statement.setSuspendRecluster(getSuspendRecluster());
        statement.setResumeRecluster(getResumeRecluster());
        
        return new SqlStatement[]{statement};
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validationErrors = super.validate(database);

        if (tableName == null) {
            validationErrors.addError("tableName is required");
        }

        // Validate mutual exclusivity - only one clustering operation per change
        int operationCount = 0;
        if (clusterBy != null) operationCount++;
        if (Boolean.TRUE.equals(dropClusteringKey)) operationCount++;
        if (Boolean.TRUE.equals(suspendRecluster)) operationCount++;
        if (Boolean.TRUE.equals(resumeRecluster)) operationCount++;

        if (operationCount == 0) {
            validationErrors.addError("At least one clustering operation must be specified");
        } else if (operationCount > 1) {
            validationErrors.addError("Only one clustering operation allowed per alterTableCluster change");
        }

        return validationErrors;
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }
}