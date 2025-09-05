package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateTableChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

/**
 * Snowflake-specific generator for creating CREATE TABLE changes when tables are missing.
 * Handles Snowflake-specific table attributes captured during snapshot.
 */
public class MissingTableChangeGeneratorSnowflake extends MissingTableChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        // Get the base CreateTableChange from the parent class
        Change[] parentChanges = super.fixMissing(missingObject, control, referenceDatabase, comparisonDatabase, chain);
        
        if (parentChanges == null || parentChanges.length == 0) {
            return parentChanges;
        }
        
        // Enhance the CreateTableChange with Snowflake-specific attributes
        for (Change change : parentChanges) {
            if (change instanceof CreateTableChange) {
                enhanceWithSnowflakeAttributes((CreateTableChange) change, (Table) missingObject);
            }
        }
        
        return parentChanges;
    }

    /**
     * Enhances a CreateTableChange with Snowflake-specific table attributes from the snapshot.
     */
    private void enhanceWithSnowflakeAttributes(CreateTableChange change, Table table) {
        // Handle transient table type
        String isTransient = table.getAttribute("isTransient", String.class);
        if ("YES".equalsIgnoreCase(isTransient)) {
            change.setTableType("TRANSIENT");
        }
        
        // Handle clustering key
        String clusteringKey = table.getAttribute("clusteringKey", String.class);
        if (!StringUtil.isEmpty(clusteringKey)) {
            // The CreateTableChange doesn't have a setAttribute method
            // We need to handle this differently - Snowflake attributes are passed via XSD namespace
            // For now, store this information in the table remarks if it's not already set
            if (StringUtil.isEmpty(change.getRemarks())) {
                change.setRemarks("CLUSTER BY (" + clusteringKey + ")");
            }
        }
        
        // Handle data retention time
        String retentionTime = table.getAttribute("retentionTime", String.class);
        if (!StringUtil.isEmpty(retentionTime) && !"null".equalsIgnoreCase(retentionTime)) {
            try {
                int retentionDays = Integer.parseInt(retentionTime);
                if (retentionDays > 0) {
                    // Store in remarks if not already set
                    String existingRemarks = change.getRemarks();
                    String retentionComment = "DATA_RETENTION_TIME_IN_DAYS=" + retentionDays;
                    if (StringUtil.isEmpty(existingRemarks)) {
                        change.setRemarks(retentionComment);
                    } else if (!existingRemarks.contains("DATA_RETENTION_TIME_IN_DAYS")) {
                        change.setRemarks(existingRemarks + "; " + retentionComment);
                    }
                }
            } catch (NumberFormatException e) {
                // Log warning but continue - don't fail the diff generation
            }
        }
        
        // For other attributes, we'll rely on the SQL generator to handle XSD namespace attributes
        // The standard CreateTableChange doesn't support setAttribute, so we can't enhance it further here
    }
}