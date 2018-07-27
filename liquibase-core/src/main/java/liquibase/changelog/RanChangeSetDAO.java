package liquibase.changelog;

import liquibase.database.Database;
import liquibase.exception.DatabaseException;

import java.util.List;

public interface RanChangeSetDAO<T extends RanChangeSet> {

    List<T> prepareRanChangeSets(Database database, boolean databaseChecksumsCompatible) throws DatabaseException;

}
