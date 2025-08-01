package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.AlterDatabaseChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.Difference;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.ChangedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

import java.util.ArrayList;
import java.util.List;

public class ChangedDatabaseChangeGenerator implements ChangedObjectChangeGenerator {

    @Override
    public Change[] fixSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public Change[] fixOutputAsSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons) {
        return changes;
    }

    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        if (liquibase.database.object.Database.class.isAssignableFrom(objectType) && 
            database instanceof SnowflakeDatabase) {
            return PRIORITY_DATABASE;
        }
        return PRIORITY_NONE;
    }

    @Override
    public Class<? extends DatabaseObject>[] runAfterTypes() {
        return null;
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return new Class[] { Schema.class, Table.class };
    }

    @Override
    public Change[] fixChanged(DatabaseObject changedObject, ObjectDifferences differences,
                               DiffOutputControl control, Database referenceDatabase,
                               Database comparisonDatabase, ChangeGeneratorChain chain) {
        
        liquibase.database.object.Database database = (liquibase.database.object.Database) changedObject;
        List<Change> changes = new ArrayList<>();
        
        // Create ALTER DATABASE change for each difference
        for (Difference difference : differences.getDifferences()) {
            AlterDatabaseChange change = new AlterDatabaseChange();
            change.setDatabaseName(database.getName());
            
            String field = difference.getField();
            Object newValue = difference.getReferenceValue();
            
            // Map differences to ALTER DATABASE operations
            switch (field) {
                case "comment":
                    change.setComment((String) newValue);
                    break;
                case "dataRetentionTimeInDays":
                    change.setDataRetentionTimeInDays((String) newValue);
                    break;
                case "maxDataExtensionTimeInDays":
                    change.setNewMaxDataExtensionTimeInDays((String) newValue);
                    break;
                case "defaultDdlCollation":
                    change.setNewDefaultDdlCollation((String) newValue);
                    break;
                case "resourceMonitor":
                    // ResourceMonitor is not available in AlterDatabaseChange - skip for now
                    continue;
                default:
                    // Skip unknown or non-alterable fields
                    continue;
            }
            
            changes.add(change);
        }
        
        return changes.toArray(new Change[0]);
    }
}