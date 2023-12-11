package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;

public interface ObjectChangeFilter {

    boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database
            comparisionDatabase);

    boolean include(DatabaseObject object);
}
