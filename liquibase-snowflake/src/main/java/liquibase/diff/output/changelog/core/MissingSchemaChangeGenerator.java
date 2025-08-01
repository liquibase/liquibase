package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.CreateSchemaChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.MissingObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class MissingSchemaChangeGenerator implements MissingObjectChangeGenerator {

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
        if (liquibase.database.object.Schema.class.isAssignableFrom(objectType) && 
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
        return new Class[] { Table.class };
    }

    @Override
    public Change[] fixMissing(DatabaseObject missingObject, DiffOutputControl control, 
                               Database referenceDatabase, Database comparisonDatabase, 
                               ChangeGeneratorChain chain) {
        
        liquibase.database.object.Schema schema = (liquibase.database.object.Schema) missingObject;
        
        CreateSchemaChange change = new CreateSchemaChange();
        change.setSchemaName(schema.getName());
        
        // Set configuration properties if they exist
        if (schema.getComment() != null) {
            change.setComment(schema.getComment());
        }
        if (schema.getDataRetentionTimeInDays() != null) {
            change.setDataRetentionTimeInDays(schema.getDataRetentionTimeInDays());
        }
        if (schema.getMaxDataExtensionTimeInDays() != null) {
            change.setMaxDataExtensionTimeInDays(schema.getMaxDataExtensionTimeInDays());
        }
        if (schema.getTransient() != null && schema.getTransient()) {
            change.setTransient(schema.getTransient());
        }
        if (schema.getDefaultDdlCollation() != null) {
            change.setDefaultDdlCollation(schema.getDefaultDdlCollation());
        }
        if (schema.getManagedAccess() != null && schema.getManagedAccess()) {
            change.setManagedAccess(schema.getManagedAccess());
        }
        
        return new Change[] { change };
    }
}