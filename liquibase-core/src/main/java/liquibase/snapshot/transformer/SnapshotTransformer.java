package liquibase.snapshot.transformer;

import liquibase.Scope;
import liquibase.structure.DatabaseObject;

public interface SnapshotTransformer {
    DatabaseObject transform(DatabaseObject object, Scope scope);
}
