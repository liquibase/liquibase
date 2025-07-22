package liquibase.change.core;

import liquibase.change.*;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.CreateTableStatement;

/**
 * Enhanced CreateTable change for Snowflake-specific features.
 */
@DatabaseChange(
    name = "createTableSnowflake",
    description = "Creates a table with Snowflake-specific features",
    priority = ChangeMetaData.PRIORITY_DEFAULT,
    appliesTo = "table",
    since = "4.33"
)
public class CreateTableSnowflakeChange extends CreateTableChange {

    private Boolean transient_;
    private String clusterByColumns;
    private String dataRetentionTimeInDays;
    private String maxDataExtensionTimeInDays;
    private Boolean copyGrants;

    @DatabaseChangeProperty(description = "Whether this is a transient table")
    public Boolean getTransient() {
        return transient_;
    }

    public void setTransient(Boolean transient_) {
        this.transient_ = transient_;
    }

    @DatabaseChangeProperty(description = "Comma-separated list of columns for clustering")
    public String getClusterByColumns() {
        return clusterByColumns;
    }

    public void setClusterByColumns(String clusterByColumns) {
        this.clusterByColumns = clusterByColumns;
    }

    @DatabaseChangeProperty(description = "Data retention time in days")
    public String getDataRetentionTimeInDays() {
        return dataRetentionTimeInDays;
    }

    public void setDataRetentionTimeInDays(String dataRetentionTimeInDays) {
        this.dataRetentionTimeInDays = dataRetentionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Maximum data extension time in days")
    public String getMaxDataExtensionTimeInDays() {
        return maxDataExtensionTimeInDays;
    }

    public void setMaxDataExtensionTimeInDays(String maxDataExtensionTimeInDays) {
        this.maxDataExtensionTimeInDays = maxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Whether to copy grants from the replaced table")
    public Boolean getCopyGrants() {
        return copyGrants;
    }

    public void setCopyGrants(Boolean copyGrants) {
        this.copyGrants = copyGrants;
    }

    @Override
    public SqlStatement[] generateStatements(Database database) {
        // Get the base CREATE TABLE statement
        SqlStatement[] baseStatements = super.generateStatements(database);
        
        if (baseStatements.length > 0 && baseStatements[0] instanceof CreateTableStatement) {
            CreateTableStatement statement = (CreateTableStatement) baseStatements[0];
            
            // Encode Snowflake-specific options in the remarks field for the generator to parse
            StringBuilder remarksBuilder = new StringBuilder();
            
            if (getClusterByColumns() != null && !getClusterByColumns().trim().isEmpty()) {
                remarksBuilder.append("CLUSTER_BY:").append(getClusterByColumns().trim());
            }
            
            if (getDataRetentionTimeInDays() != null && !getDataRetentionTimeInDays().trim().isEmpty()) {
                if (remarksBuilder.length() > 0) remarksBuilder.append("|");
                remarksBuilder.append("DATA_RETENTION:").append(getDataRetentionTimeInDays().trim());
            }
            
            if (getMaxDataExtensionTimeInDays() != null && !getMaxDataExtensionTimeInDays().trim().isEmpty()) {
                if (remarksBuilder.length() > 0) remarksBuilder.append("|");
                remarksBuilder.append("MAX_DATA_EXTENSION:").append(getMaxDataExtensionTimeInDays().trim());
            }
            
            if (getCopyGrants() != null && getCopyGrants()) {
                if (remarksBuilder.length() > 0) remarksBuilder.append("|");
                remarksBuilder.append("COPY_GRANTS:true");
            }
            
            // Preserve existing remarks
            if (getRemarks() != null && !getRemarks().trim().isEmpty()) {
                if (remarksBuilder.length() > 0) remarksBuilder.append("|");
                remarksBuilder.append("COMMENT:").append(getRemarks().trim());
            }
            
            if (remarksBuilder.length() > 0) {
                statement.setRemarks(remarksBuilder.toString());
            }
            
            // Set transient table flag using tablespace field (temporary approach)
            if (getTransient() != null && getTransient()) {
                statement.setTablespace("TRANSIENT");
            }
        }
        
        return baseStatements;
    }

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors errors = super.validate(database);
        
        // Validate cluster by columns format
        if (getClusterByColumns() != null && !getClusterByColumns().trim().isEmpty()) {
            String columns = getClusterByColumns().trim();
            if (columns.contains("(") || columns.contains(")")) {
                errors.addError("clusterByColumns should contain only column names separated by commas, without parentheses");
            }
        }
        
        // Validate data retention time
        if (getDataRetentionTimeInDays() != null && !getDataRetentionTimeInDays().trim().isEmpty()) {
            try {
                int days = Integer.parseInt(getDataRetentionTimeInDays().trim());
                if (days < 0 || days > 90) {
                    errors.addError("dataRetentionTimeInDays must be between 0 and 90");
                }
            } catch (NumberFormatException e) {
                errors.addError("dataRetentionTimeInDays must be a valid number");
            }
        }
        
        return errors;
    }
}