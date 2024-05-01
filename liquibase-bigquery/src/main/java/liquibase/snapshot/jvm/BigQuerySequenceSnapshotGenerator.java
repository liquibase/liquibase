package liquibase.snapshot.jvm;

import liquibase.Scope;
import liquibase.database.BigQueryDatabase;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.structure.DatabaseObject;

public class BigQuerySequenceSnapshotGenerator extends SequenceSnapshotGenerator {

        @Override
        public int getPriority(Class<? extends DatabaseObject> objectType, Database database) {
            if (database instanceof BigQueryDatabase) {
                return super.getPriority(objectType, database) + PRIORITY_DATABASE;
            } else {
                return PRIORITY_NONE;
            }
        }

        @Override
        protected DatabaseObject snapshotObject(DatabaseObject example, DatabaseSnapshot snapshot) throws DatabaseException {
            Scope.getCurrentScope().getLog(this.getClass()).info("Sequences are not supported by BigQuery");
            return null;
        }

}
