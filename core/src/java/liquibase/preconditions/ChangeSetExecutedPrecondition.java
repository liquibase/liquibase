package liquibase.preconditions;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseHistoryException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.JDBCException;

public class ChangeSetExecutedPrecondition implements Precondition {

    private String changeLogFile;
    private String id;
    private String author;

    public String getChangeLogFile() {
        return changeLogFile;
    }

    public void setChangeLogFile(String changeLogFile) {
        this.changeLogFile = changeLogFile;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void check(Database database, DatabaseChangeLog changeLog) throws PreconditionFailedException, PreconditionErrorException {
        ChangeSet changeSet = new ChangeSet(getId(), getAuthor(), false, false, getChangeLogFile(), getChangeLogFile(), null, null, false);
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(changeSet);
        } catch (DatabaseHistoryException dhe) {
            // If the databasechangelog table doesn't exist, the changeset obviously was not ran
            try {
                if (!database.doesChangeLogTableExist()) {
                    ranChangeSet = null;
                } else {
                    throw new PreconditionErrorException(dhe, changeLog, this);
                }
            } catch (JDBCException e) {
                throw new PreconditionErrorException(e, changeLog, this);
            }
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (ranChangeSet == null) {
            throw new PreconditionFailedException("Change Set '"+changeSet.toString(false)+"' has not been run", changeLog, this);
        }
    }

    public String getTagName() {
        return "changeSetExecuted";
    }
}
