package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;

public class NoOpTransformer implements SnapshotTransformer {

    private static NoOpTransformer instance = new NoOpTransformer();

    private NoOpTransformer() {
    }

    public static NoOpTransformer getInstance() {
        return instance;
    }

    @Override
    public DatabaseObject transform(DatabaseObject object, Scope scope) {
        return object;
    }
}
