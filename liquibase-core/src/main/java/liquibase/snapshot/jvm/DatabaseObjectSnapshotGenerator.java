package liquibase.snapshot.jvm;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.Table;

public interface DatabaseObjectSnapshotGenerator<DatabaseObjectType extends DatabaseObject> extends PrioritizedService {

    /**
     * Does this generator support the given database?
     */
    public boolean supports(Class<? extends DatabaseObject> databaseObjectClass, Database database);

    boolean has(DatabaseObjectType example, Database database) throws DatabaseException;

    DatabaseObjectType snapshot(DatabaseObjectType example, Database database) throws DatabaseException;

    DatabaseObjectType[] get(DatabaseObject container, Database database) throws DatabaseException;

}
