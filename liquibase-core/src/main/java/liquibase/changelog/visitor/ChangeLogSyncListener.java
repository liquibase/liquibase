package liquibase.changelog.visitor;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;

public interface ChangeLogSyncListener {
    void markedRan(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database);

}
