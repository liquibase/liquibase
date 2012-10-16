package liquibase.diff;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.snapshot.DatabaseSnapshot;

public interface DiffGenerator extends PrioritizedService{
    DiffResult compare(Database referenceDatabase, Database comparisonDatabase, DiffControl diffControl) throws LiquibaseException;

    DiffResult compare(DatabaseSnapshot referenceSnapshot, DatabaseSnapshot comparisonSnapshot, DiffControl diffControl) throws DatabaseException;

    boolean supports(Database referenceDatabase, Database comparisonDatabase);

}
