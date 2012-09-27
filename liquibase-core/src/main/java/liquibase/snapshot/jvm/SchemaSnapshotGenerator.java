package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public class SchemaSnapshotGenerator extends JdbcDatabaseObjectSnapshotGenerator<Schema>  {

    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    public boolean has(Schema example, Database database) throws DatabaseException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Schema[] get(DatabaseObject container, Database database) throws DatabaseException {
        return new Schema[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Schema snapshot(Schema example, Database database) throws DatabaseException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
