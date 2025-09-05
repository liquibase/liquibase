package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterTableChange;
import liquibase.change.core.SetTableRemarksChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;
import liquibase.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Snowflake-specific generator for creating ALTER TABLE changes when table properties change.
 * Handles Snowflake-specific table attributes like clustering, retention time, and feature settings.
 */
public class ChangedTableChangeGeneratorSnowflake extends ChangedTableChangeGenerator {

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Table.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return super.getPriority(objectType, database) + PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, DiffOutputControl control, Database referenceDatabase, Database comparisonDatabase, ChangeGeneratorChain chain) {
        if (differences == null) {
            return null;
        }
        
        Table table = (Table) changedObject;
        List<Change> changes = new ArrayList<>();

        // Handle remarks/comment changes - delegate to parent class unless Snowflake-specific handling is needed
        if (!isSnowflakeSpecificCommentHandlingNeeded(table)) {
            // For standard comment handling, delegate to chain instead of calling super directly
            Change[] parentChanges = chain.fixChanged(changedObject, differences, control, referenceDatabase, comparisonDatabase);
            if (parentChanges != null) {
                for (Change change : parentChanges) {
                    changes.add(change);
                }
            }
        }

        // Handle Snowflake-specific table property changes
        
        // Handle clustering key changes
        Difference clusteringDiff = differences.getDifference("clusteringKey");
        if (clusteringDiff != null) {
            String newClusteringKey = (String) clusteringDiff.getComparedValue(); // New/target value
            String oldClusteringKey = (String) clusteringDiff.getReferenceValue(); // Old/current value
            
            if (!StringUtil.isEmpty(newClusteringKey)) {
                // Set new clustering key
                AlterTableChange change = createAlterTableChange(table, control);
                change.setClusterBy(newClusteringKey);
                changes.add(change);
            } else if (!StringUtil.isEmpty(oldClusteringKey)) {
                // Drop existing clustering key
                AlterTableChange change = createAlterTableChange(table, control);
                change.setDropClusteringKey(Boolean.TRUE);
                changes.add(change);
            }
        }

        // Handle data retention time changes
        Difference retentionDiff = differences.getDifference("retentionTime");
        if (retentionDiff != null) {
            String retentionTimeStr = (String) retentionDiff.getComparedValue(); // New/target value
            if (!StringUtil.isEmpty(retentionTimeStr) && !"null".equalsIgnoreCase(retentionTimeStr)) {
                try {
                    int retentionDays = Integer.parseInt(retentionTimeStr);
                    AlterTableChange change = createAlterTableChange(table, control);
                    change.setSetDataRetentionTimeInDays(retentionDays);
                    changes.add(change);
                } catch (NumberFormatException e) {
                    // Log warning but continue
                }
            }
        }

        // Handle change tracking changes
        Difference changeTrackingDiff = differences.getDifference("changeTracking");
        if (changeTrackingDiff != null) {
            String changeTrackingStr = (String) changeTrackingDiff.getComparedValue(); // New/target value
            Boolean changeTracking = convertToBoolean(changeTrackingStr);
            if (changeTracking != null) {
                AlterTableChange change = createAlterTableChange(table, control);
                change.setSetChangeTracking(changeTracking);
                changes.add(change);
            }
        }

        // Handle schema evolution changes
        Difference schemaEvolutionDiff = differences.getDifference("enableSchemaEvolution");
        if (schemaEvolutionDiff != null) {
            String schemaEvolutionStr = (String) schemaEvolutionDiff.getComparedValue(); // New/target value
            Boolean enableSchemaEvolution = convertToBoolean(schemaEvolutionStr);
            if (enableSchemaEvolution != null) {
                AlterTableChange change = createAlterTableChange(table, control);
                change.setSetEnableSchemaEvolution(enableSchemaEvolution);
                changes.add(change);
            }
        }

        // Handle comment changes differently if it's a Snowflake table with special handling needed
        Difference commentDiff = differences.getDifference("remarks");
        if (commentDiff != null && isSnowflakeSpecificCommentHandlingNeeded(table)) {
            // Remove any SetTableRemarksChange added by parent class
            changes.removeIf(change -> change instanceof SetTableRemarksChange);
            
            // Add Snowflake-specific comment handling if needed
            String newComment = (String) commentDiff.getComparedValue(); // New/target value
            SetTableRemarksChange change = new SetTableRemarksChange();
            if (control.getIncludeCatalog()) {
                change.setCatalogName(table.getSchema().getCatalogName());
            }
            if (control.getIncludeSchema()) {
                change.setSchemaName(table.getSchema().getName());
            }
            change.setTableName(table.getName());
            change.setRemarks(newComment);
            changes.add(change);
        }

        return changes.isEmpty() ? null : changes.toArray(new Change[0]);
    }

    /**
     * Creates a basic AlterTableChange with common properties set.
     */
    private AlterTableChange createAlterTableChange(Table table, DiffOutputControl control) {
        AlterTableChange change = new AlterTableChange();
        if (control.getIncludeCatalog()) {
            change.setCatalogName(table.getSchema().getCatalogName());
        }
        if (control.getIncludeSchema()) {
            change.setSchemaName(table.getSchema().getName());
        }
        change.setTableName(table.getName());
        return change;
    }

    /**
     * Converts various string representations to boolean values.
     * Handles Snowflake's YES/NO, ON/OFF, Y/N, true/false formats.
     */
    private Boolean convertToBoolean(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "yes":
            case "y":
            case "on":
            case "true":
            case "1":
                return true;
            case "no":
            case "n":
            case "off":
            case "false":
            case "0":
                return false;
            default:
                return null;
        }
    }

    /**
     * Determines if Snowflake-specific comment handling is needed.
     * This could be extended to handle special cases like transient tables, etc.
     */
    private boolean isSnowflakeSpecificCommentHandlingNeeded(Table table) {
        // For now, use standard comment handling
        // This method allows for future extension if special handling is needed
        return false;
    }
}