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
    private Integer setMaxDataExtensionTimeInDays;
    private String setDefaultDdlCollation;
    
    // Search optimization operations
    private String addSearchOptimization;
    private Boolean dropSearchOptimization;
    
    // Row access policy operations
    private String addRowAccessPolicy;
    private String dropRowAccessPolicy;
    
    // Aggregation policy operations
    private String setAggregationPolicy;
    private Boolean unsetAggregationPolicy;
    private Boolean forceAggregationPolicy;
    
    // Projection policy operations
    private String setProjectionPolicy;
    private Boolean unsetProjectionPolicy;
    private Boolean forceProjectionPolicy;
    
    // Tag operations
    private String setTag;
    private String unsetTag;

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

    @DatabaseChangeProperty(description = "Set maximum data extension time in days (0-90)")
    public Integer getSetMaxDataExtensionTimeInDays() {
        return setMaxDataExtensionTimeInDays;
    }

    public void setSetMaxDataExtensionTimeInDays(Integer setMaxDataExtensionTimeInDays) {
        this.setMaxDataExtensionTimeInDays = setMaxDataExtensionTimeInDays;
    }

    @DatabaseChangeProperty(description = "Set default DDL collation specification")
    public String getSetDefaultDdlCollation() {
        return setDefaultDdlCollation;
    }

    public void setSetDefaultDdlCollation(String setDefaultDdlCollation) {
        this.setDefaultDdlCollation = setDefaultDdlCollation;
    }

    @DatabaseChangeProperty(description = "Add search optimization to the table (empty for default, or specific method)")
    public String getAddSearchOptimization() {
        return addSearchOptimization;
    }

    public void setAddSearchOptimization(String addSearchOptimization) {
        this.addSearchOptimization = addSearchOptimization;
    }

    @DatabaseChangeProperty(description = "Drop search optimization from the table")
    public Boolean getDropSearchOptimization() {
        return dropSearchOptimization;
    }

    public void setDropSearchOptimization(Boolean dropSearchOptimization) {
        this.dropSearchOptimization = dropSearchOptimization;
    }

    @DatabaseChangeProperty(description = "Add row access policy (format: \"policy_name ON (col1, col2)\")")
    public String getAddRowAccessPolicy() {
        return addRowAccessPolicy;
    }

    public void setAddRowAccessPolicy(String addRowAccessPolicy) {
        this.addRowAccessPolicy = addRowAccessPolicy;
    }

    @DatabaseChangeProperty(description = "Drop row access policy (policy name or \"ALL\" for all policies)")
    public String getDropRowAccessPolicy() {
        return dropRowAccessPolicy;
    }

    public void setDropRowAccessPolicy(String dropRowAccessPolicy) {
        this.dropRowAccessPolicy = dropRowAccessPolicy;
    }

    @DatabaseChangeProperty(description = "Set aggregation policy for the table")
    public String getSetAggregationPolicy() {
        return setAggregationPolicy;
    }

    public void setSetAggregationPolicy(String setAggregationPolicy) {
        this.setAggregationPolicy = setAggregationPolicy;
    }

    @DatabaseChangeProperty(description = "Remove aggregation policy from the table")
    public Boolean getUnsetAggregationPolicy() {
        return unsetAggregationPolicy;
    }

    public void setUnsetAggregationPolicy(Boolean unsetAggregationPolicy) {
        this.unsetAggregationPolicy = unsetAggregationPolicy;
    }

    @DatabaseChangeProperty(description = "Force setting aggregation policy (use with setAggregationPolicy)")
    public Boolean getForceAggregationPolicy() {
        return forceAggregationPolicy;
    }

    public void setForceAggregationPolicy(Boolean forceAggregationPolicy) {
        this.forceAggregationPolicy = forceAggregationPolicy;
    }

    @DatabaseChangeProperty(description = "Set projection policy for the table")
    public String getSetProjectionPolicy() {
        return setProjectionPolicy;
    }

    public void setSetProjectionPolicy(String setProjectionPolicy) {
        this.setProjectionPolicy = setProjectionPolicy;
    }

    @DatabaseChangeProperty(description = "Remove projection policy from the table")
    public Boolean getUnsetProjectionPolicy() {
        return unsetProjectionPolicy;
    }

    public void setUnsetProjectionPolicy(Boolean unsetProjectionPolicy) {
        this.unsetProjectionPolicy = unsetProjectionPolicy;
    }

    @DatabaseChangeProperty(description = "Force setting projection policy (use with setProjectionPolicy)")
    public Boolean getForceProjectionPolicy() {
        return forceProjectionPolicy;
    }

    public void setForceProjectionPolicy(Boolean forceProjectionPolicy) {
        this.forceProjectionPolicy = forceProjectionPolicy;
    }

    @DatabaseChangeProperty(description = "Set tags on the table (format: \"tag1 = 'value1', tag2 = 'value2'\")")
    public String getSetTag() {
        return setTag;
    }

    public void setSetTag(String setTag) {
        this.setTag = setTag;
    }

    @DatabaseChangeProperty(description = "Unset tags from the table (format: \"tag1, tag2, tag3\")")
    public String getUnsetTag() {
        return unsetTag;
    }

    public void setUnsetTag(String unsetTag) {
        this.unsetTag = unsetTag;
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
        statement.setSetMaxDataExtensionTimeInDays(getSetMaxDataExtensionTimeInDays());
        statement.setSetDefaultDdlCollation(getSetDefaultDdlCollation());
        
        // Set search optimization operations
        statement.setAddSearchOptimization(getAddSearchOptimization());
        statement.setDropSearchOptimization(getDropSearchOptimization());
        
        // Set row access policy operations
        statement.setAddRowAccessPolicy(getAddRowAccessPolicy());
        statement.setDropRowAccessPolicy(getDropRowAccessPolicy());
        
        // Set aggregation policy operations
        statement.setSetAggregationPolicy(getSetAggregationPolicy());
        statement.setUnsetAggregationPolicy(getUnsetAggregationPolicy());
        statement.setForceAggregationPolicy(getForceAggregationPolicy());
        
        // Set projection policy operations
        statement.setSetProjectionPolicy(getSetProjectionPolicy());
        statement.setUnsetProjectionPolicy(getUnsetProjectionPolicy());
        statement.setForceProjectionPolicy(getForceProjectionPolicy());
        
        // Set tag operations
        statement.setSetTag(getSetTag());
        statement.setUnsetTag(getUnsetTag());
        
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
        
        if (setMaxDataExtensionTimeInDays != null) {
            message.append(" - max data extension set to ").append(setMaxDataExtensionTimeInDays).append(" days");
        }
        
        if (setDefaultDdlCollation != null) {
            message.append(" - default DDL collation set to ").append(setDefaultDdlCollation);
        }
        
        if (addSearchOptimization != null) {
            message.append(" - search optimization added").append(addSearchOptimization.isEmpty() ? "" : " (" + addSearchOptimization + ")");
        }
        
        if (Boolean.TRUE.equals(dropSearchOptimization)) {
            message.append(" - search optimization dropped");
        }
        
        if (addRowAccessPolicy != null) {
            message.append(" - row access policy added: ").append(addRowAccessPolicy);
        }
        
        if (dropRowAccessPolicy != null) {
            message.append(" - row access policy dropped: ").append(dropRowAccessPolicy);
        }
        
        if (setAggregationPolicy != null) {
            message.append(" - aggregation policy set to ").append(setAggregationPolicy);
            if (Boolean.TRUE.equals(forceAggregationPolicy)) {
                message.append(" (forced)");
            }
        }
        
        if (Boolean.TRUE.equals(unsetAggregationPolicy)) {
            message.append(" - aggregation policy unset");
        }
        
        if (setProjectionPolicy != null) {
            message.append(" - projection policy set to ").append(setProjectionPolicy);
            if (Boolean.TRUE.equals(forceProjectionPolicy)) {
                message.append(" (forced)");
            }
        }
        
        if (Boolean.TRUE.equals(unsetProjectionPolicy)) {
            message.append(" - projection policy unset");
        }
        
        if (setTag != null) {
            message.append(" - tags set: ").append(setTag);
        }
        
        if (unsetTag != null) {
            message.append(" - tags unset: ").append(unsetTag);
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

        // Enhanced validation: Clustering expression syntax and column count
        if (clusterBy != null) {
            String[] columns = clusterBy.split(",");
            if (columns.length > 4) {
                validationErrors.addError("Maximum 4 columns allowed in clustering key, found: " + columns.length);
            }
            
            // Validate each clustering expression
            for (String column : columns) {
                String trimmedColumn = column.trim();
                if (trimmedColumn.isEmpty()) {
                    validationErrors.addError("Empty clustering expression found in clusterBy");
                } else if (!isValidClusteringExpression(trimmedColumn)) {
                    validationErrors.addError("Invalid clustering expression: '" + trimmedColumn + "'. Must be a valid column name or expression");
                }
            }
        }

        // Validate data retention time range
        if (setDataRetentionTimeInDays != null) {
            if (setDataRetentionTimeInDays < 0 || setDataRetentionTimeInDays > 90) {
                validationErrors.addError("setDataRetentionTimeInDays must be between 0 and 90");
            }
        }
        
        // Validate max data extension time range
        if (setMaxDataExtensionTimeInDays != null) {
            if (setMaxDataExtensionTimeInDays < 0 || setMaxDataExtensionTimeInDays > 90) {
                validationErrors.addError("setMaxDataExtensionTimeInDays must be between 0 and 90");
            }
        }
        
        // Validate default DDL collation format
        if (setDefaultDdlCollation != null) {
            if (setDefaultDdlCollation.trim().isEmpty()) {
                validationErrors.addError("setDefaultDdlCollation cannot be empty");
            }
            // Basic collation format validation - should contain valid collation specification
            if (!isValidCollationSpecification(setDefaultDdlCollation)) {
                validationErrors.addError("Invalid collation specification: '" + setDefaultDdlCollation + "'");
            }
        }

        // At least one operation must be specified
        boolean hasOperation = clusteringOperationCount > 0 || 
                              setDataRetentionTimeInDays != null ||
                              setChangeTracking != null ||
                              setEnableSchemaEvolution != null ||
                              setMaxDataExtensionTimeInDays != null ||
                              setDefaultDdlCollation != null ||
                              addSearchOptimization != null ||
                              Boolean.TRUE.equals(dropSearchOptimization) ||
                              addRowAccessPolicy != null ||
                              dropRowAccessPolicy != null ||
                              setAggregationPolicy != null ||
                              Boolean.TRUE.equals(unsetAggregationPolicy) ||
                              setProjectionPolicy != null ||
                              Boolean.TRUE.equals(unsetProjectionPolicy) ||
                              setTag != null ||
                              unsetTag != null;
        
        if (!hasOperation) {
            validationErrors.addError("At least one Snowflake-specific operation must be specified");
        }

        return validationErrors;
    }
    
    /**
     * Validates clustering expression syntax.
     * Accepts column names, expressions, and function calls.
     */
    private boolean isValidClusteringExpression(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        
        // Basic validation: should be valid SQL identifier or expression
        // Allow column names, functions like UPPER(col), expressions like col1 + col2
        String trimmed = expression.trim();
        
        // Must not contain dangerous SQL keywords or characters
        String upperExpression = trimmed.toUpperCase();
        if (upperExpression.contains("DROP") || upperExpression.contains("DELETE") || 
            upperExpression.contains("INSERT") || upperExpression.contains("UPDATE") ||
            upperExpression.contains(";") || upperExpression.contains("--")) {
            return false;
        }
        
        // Allow basic identifiers, quoted identifiers, function calls, and simple expressions
        return trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*$") ||  // Simple identifier
               trimmed.matches("^\"[^\"]+\"$") ||               // Quoted identifier
               trimmed.matches("^[a-zA-Z_][a-zA-Z0-9_]*\\([^)]*\\)$") || // Function call
               trimmed.matches("^[a-zA-Z_\"'][a-zA-Z0-9_\\s+\\-*/()\"']*$"); // Expression
    }
    
    /**
     * Validates collation specification format.
     * Should be a valid Snowflake collation specification.
     */
    private boolean isValidCollationSpecification(String collation) {
        if (collation == null || collation.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = collation.trim();
        
        // Must not contain dangerous SQL keywords or characters
        String upperCollation = trimmed.toUpperCase();
        if (upperCollation.contains("DROP") || upperCollation.contains("DELETE") || 
            upperCollation.contains("INSERT") || upperCollation.contains("UPDATE") ||
            upperCollation.contains(";") || upperCollation.contains("--")) {
            return false;
        }
        
        // Allow quoted strings, identifiers, and collation specifications
        // Examples: 'utf8', "en_US", utf8_general_ci, en_US.UTF-8
        return trimmed.matches("^['\"][^'\"]+['\"]$") ||       // Quoted collation
               trimmed.matches("^[a-zA-Z0-9_.-]+$");          // Unquoted collation spec
    }

    @Override
    public String getSerializedObjectNamespace() {
        return "http://www.liquibase.org/xml/ns/snowflake";
    }
}