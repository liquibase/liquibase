package org.liquibase.ide.common;

import liquibase.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.migrator.Migrator;

public interface IdeFacade {
    ProgressMonitor getProgressMonitor();

    Migrator getMigrator(Database database);

    DatabaseChangeLog getChangeLog();

    ChangeLogWriter getChangeLogWriter();
}
