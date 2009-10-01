package liquibase.precondition.core;

import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.precondition.Precondition;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

public class ViewExistsPrecondition implements Precondition {
    private String schemaName;
    private String viewName;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = StringUtils.trimToNull(schemaName);
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        DatabaseSnapshot snapshot;
        try {
            snapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(database, getSchemaName(), null);
        } catch (DatabaseException e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (snapshot.getView(getViewName()) == null) {
            throw new PreconditionFailedException("View "+database.escapeStringForDatabase(getViewName())+" does not exist", changeLog, this);
        }
    }

    public String getName() {
        return "viewExists";
    }
}