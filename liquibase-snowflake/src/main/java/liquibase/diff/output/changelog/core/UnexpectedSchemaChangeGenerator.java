package liquibase.diff.output.changelog.core;

import liquibase.change.Change;
import liquibase.change.core.DropSchemaChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.ChangeGeneratorChain;
import liquibase.diff.output.changelog.UnexpectedObjectChangeGenerator;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class UnexpectedSchemaChangeGenerator implements UnexpectedObjectChangeGenerator {

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
        return new Class[] { Table.class };
    }

    @Override
    public Class<? extends DatabaseObject>[] runBeforeTypes() {
        return null;
    }

    @Override
    public Change[] fixUnexpected(DatabaseObject unexpectedObject, DiffOutputControl control,
                                  Database referenceDatabase, Database comparisonDatabase,
                                  ChangeGeneratorChain chain) {
        
        liquibase.database.object.Schema schema = (liquibase.database.object.Schema) unexpectedObject;
        
        DropSchemaChange change = new DropSchemaChange();
        change.setSchemaName(schema.getName());
        
        return new Change[] { change };
    }
}