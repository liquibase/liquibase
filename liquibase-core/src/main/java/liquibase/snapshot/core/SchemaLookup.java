package liquibase.snapshot.core;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.snapshot.AbstractSnapshotLookupLogic;
import liquibase.snapshot.NewDatabaseSnapshot;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;

import java.util.ArrayList;
import java.util.Collection;

public class SchemaLookup extends AbstractSnapshotLookupLogic<Schema> {
    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, ExecutionEnvironment environment) {
        if (objectType.isAssignableFrom(Schema.class)) {
            return PRIORITY_OBJECT;
        } else {
            return PRIORITY_NONE;
        }
    }

    @Override
    public <T extends DatabaseObject> Collection<T> lookup(Class<T> objectType, DatabaseObject example, ExecutionEnvironment environment) throws DatabaseException, UnsupportedException {
        if (!Schema.class.isAssignableFrom(objectType)) {
            return null;
        }
        ArrayList<Schema> list = new ArrayList<Schema>();
        list.add(new Schema(((Schema) example).getCatalogName(), example.getName()));
        return (Collection<T>) list;
    }
}
