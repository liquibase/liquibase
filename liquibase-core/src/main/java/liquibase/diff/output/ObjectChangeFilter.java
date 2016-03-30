package liquibase.diff.output;

import liquibase.database.Database;
import liquibase.diff.ObjectDifferences;
import liquibase.structure.DatabaseObject;

public interface ObjectChangeFilter {

    public boolean includeMissing(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    public boolean includeUnexpected(DatabaseObject object, Database referenceDatabase, Database comparisionDatabase);

    public boolean includeChanged(DatabaseObject object, ObjectDifferences differences, Database referenceDatabase, Database comparisionDatabase);

}
