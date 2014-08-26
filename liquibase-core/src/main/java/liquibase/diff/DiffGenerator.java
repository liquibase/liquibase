package liquibase.diff;

import liquibase.database.Database;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.NewDatabaseSnapshot;

public interface DiffGenerator extends PrioritizedService {
    DiffResult compare(NewDatabaseSnapshot referenceSnapshot, NewDatabaseSnapshot comparisonSnapshot, CompareControl compareControl) throws DatabaseException;

    boolean supports(Database referenceDatabase, Database comparisonDatabase);

}
