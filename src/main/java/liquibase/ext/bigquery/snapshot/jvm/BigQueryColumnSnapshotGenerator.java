package liquibase.ext.bigquery.snapshot.jvm;


import liquibase.database.Database;

import liquibase.ext.bigquery.database.BigqueryDatabase;


import liquibase.snapshot.jvm.ColumnSnapshotGenerator;
import liquibase.structure.DatabaseObject;

public class BigQueryColumnSnapshotGenerator extends ColumnSnapshotGenerator {


    @Override
    public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
        int priority = super.getPriority(objectType, database);
        if (priority != PRIORITY_NONE && database instanceof BigqueryDatabase) {
            return PRIORITY_DATABASE;
        }

        return PRIORITY_NONE;
    }

}
