package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterWarehouseChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.database.object.Warehouse;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChangedWarehouseChangeGenerator implements ChangedObjectChangeGenerator {
    
    // State properties that should be excluded from change generation
    private static final String[] EXCLUDED_STATE_FIELDS = {
        "state", "startedClusters", "running", "queued", "isDefault", "isCurrent",
        "available", "provisioning", "quiescing", "other", "createdOn", 
        "resumedOn", "updatedOn", "owner", "ownerRoleType"
    };
    
    private static final Set<String> EXCLUDED_FIELDS_SET = new HashSet<>(Arrays.asList(EXCLUDED_STATE_FIELDS));

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (Warehouse.class.isAssignableFrom(objectType) && database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences, 
                             DiffOutputControl control, Database referenceDatabase, 
                             Database comparisonDatabase, ChangeGeneratorChain chain) {
        Warehouse warehouse = (Warehouse) changedObject;
        
        // Check if there are any configuration property differences (ignore state properties)
        boolean hasConfigurationChanges = differences.hasDifferences() && hasNonStateChanges(differences);
        
        if (!hasConfigurationChanges) {
            // Only state properties changed, no ALTER needed
            return new Change[0];  
        }
        
        AlterWarehouseChange change = new AlterWarehouseChange();
        change.setWarehouseName(warehouse.getName());
        
        // Only set properties that actually changed (and are not state properties)
        if (differences.isDifferent("type") && !EXCLUDED_FIELDS_SET.contains("type")) {
            change.setWarehouseType(warehouse.getType());
        }
        if (differences.isDifferent("size") && !EXCLUDED_FIELDS_SET.contains("size")) {
            change.setWarehouseSize(warehouse.getSize());
        }
        if (differences.isDifferent("minClusterCount") && !EXCLUDED_FIELDS_SET.contains("minClusterCount")) {
            change.setMinClusterCount(warehouse.getMinClusterCount());
        }
        if (differences.isDifferent("maxClusterCount") && !EXCLUDED_FIELDS_SET.contains("maxClusterCount")) {
            change.setMaxClusterCount(warehouse.getMaxClusterCount());
        }
        if (differences.isDifferent("autoSuspend") && !EXCLUDED_FIELDS_SET.contains("autoSuspend")) {
            change.setAutoSuspend(warehouse.getAutoSuspend());
        }
        if (differences.isDifferent("autoResume") && !EXCLUDED_FIELDS_SET.contains("autoResume")) {
            change.setAutoResume(warehouse.getAutoResume());
        }
        if (differences.isDifferent("resourceMonitor") && !EXCLUDED_FIELDS_SET.contains("resourceMonitor")) {
            change.setResourceMonitor(warehouse.getResourceMonitor());
        }
        if (differences.isDifferent("comment") && !EXCLUDED_FIELDS_SET.contains("comment")) {
            change.setComment(warehouse.getComment());
        }
        if (differences.isDifferent("enableQueryAcceleration") && !EXCLUDED_FIELDS_SET.contains("enableQueryAcceleration")) {
            change.setEnableQueryAcceleration(warehouse.getEnableQueryAcceleration());
        }
        if (differences.isDifferent("queryAccelerationMaxScaleFactor") && !EXCLUDED_FIELDS_SET.contains("queryAccelerationMaxScaleFactor")) {
            change.setQueryAccelerationMaxScaleFactor(warehouse.getQueryAccelerationMaxScaleFactor());
        }
        if (differences.isDifferent("scalingPolicy") && !EXCLUDED_FIELDS_SET.contains("scalingPolicy")) {
            change.setScalingPolicy(warehouse.getScalingPolicy());
        }
        // Note: resourceConstraint is not available in AlterWarehouseChange
        
        return new Change[] { change };
    }

    @Override
    public Change[] fixSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, liquibase.diff.compare.CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }
    
    private boolean hasNonStateChanges(ObjectDifferences differences) {
        // Check all known configuration fields
        return differences.isDifferent("type") || 
               differences.isDifferent("size") ||
               differences.isDifferent("minClusterCount") ||
               differences.isDifferent("maxClusterCount") ||
               differences.isDifferent("autoSuspend") ||
               differences.isDifferent("autoResume") ||
               differences.isDifferent("resourceMonitor") ||
               differences.isDifferent("comment") ||
               differences.isDifferent("enableQueryAcceleration") ||
               differences.isDifferent("queryAccelerationMaxScaleFactor") ||
               differences.isDifferent("scalingPolicy");
    }
}