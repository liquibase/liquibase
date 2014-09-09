package liquibase.snapshot;

import liquibase.ExecutionEnvironment;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnsupportedException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Relation;

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

    @Override
    public NewDatabaseSnapshot createSnapshot(DatabaseObject[] examples, SnapshotControl snapshotControl, ExecutionEnvironment env) throws DatabaseException, UnsupportedException {
        return new NewDatabaseSnapshot(snapshotControl, env);
    }

    public void addObjects(DatabaseObject... objects) {
        for (DatabaseObject object : objects) {
            this.objects.add(object);

            if (object instanceof Relation) {
                for (Column column : ((Relation) object).getColumns()) {
                    this.objects.add(column);
                }
                this.objects.add(object.getSchema());
            }
        }

    }

    public void removeObjects(DatabaseObject... objects) {
        for (DatabaseObject object : objects) {
            this.objects.remove(object);

            if (object instanceof Relation) {
                for (Column column : ((Relation) object).getColumns()) {
                    this.objects.remove(column);
                }
            }
        }

    }


}
