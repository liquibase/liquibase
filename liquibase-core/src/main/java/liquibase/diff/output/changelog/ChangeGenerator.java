package liquibase.diff.output.changelog;

import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.structure.DatabaseObject;

public interface ChangeGenerator {

    final int PRIORITY_NONE = -1;
    final int PRIORITY_DEFAULT = 1;
    final int PRIORITY_DATABASE = 5;
    final int PRIORITY_ADDITIONAL = 50;

    int getPriority(Class<? extends DatabaseObject> objectType, Database database);

    Class<? extends DatabaseObject>[] runAfterTypes();
    Class<? extends DatabaseObject>[] runBeforeTypes();

    Change[] fixSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons);

    Change[] fixOutputAsSchema(Change[] changes, CompareControl.SchemaComparison[] schemaComparisons);
}
