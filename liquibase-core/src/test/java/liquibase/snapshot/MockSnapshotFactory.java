package liquibase.snapshot;

import liquibase.Scope;
import liquibase.exception.ActionPerformException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MockSnapshotFactory extends SnapshotFactory {
    private List<DatabaseObject> objects;

    public MockSnapshotFactory(DatabaseObject... objects) {
        this.objects = new ArrayList<DatabaseObject>();
        if (objects != null) {
            this.objects.addAll(Arrays.asList(objects));
        }
    }

    public MockSnapshotFactory add(DatabaseObject... objects) {
        if (objects != null) {
            this.objects.addAll(Arrays.asList(objects));
        }
        return this;
    }

    @Override
    public boolean has(DatabaseObject example, Scope scope) throws ActionPerformException, InvalidExampleException {
        return this.get(example, scope) != null;
    }

    @Override
    public <T extends DatabaseObject> T get(T example, Scope scope) throws ActionPerformException, InvalidExampleException {
        for (DatabaseObject  object : objects) {
            if (example.equals(object)) {
                return (T) object;
            }
        }

        return null;
    }
}
