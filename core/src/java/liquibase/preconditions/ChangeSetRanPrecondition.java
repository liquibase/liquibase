package liquibase.preconditions;

import liquibase.database.Database;
import liquibase.DatabaseChangeLog;
import liquibase.ChangeSet;
import liquibase.RanChangeSet;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.JDBCException;
import liquibase.exception.DatabaseHistoryException;

public class ChangeSetRanPrecondition implements Precondition {

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
        ChangeSet changeSet = new ChangeSet(getId(), getAuthor(), false, false, getChangeLogFile(), getChangeLogFile(), null, null);
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(changeSet);
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (ranChangeSet == null) {
            throw new PreconditionFailedException("Change Set '"+changeSet.toString(false)+"' has not been run", changeLog, this);
        }
    }

    public String getTagName() {
        return "changeSetRan";
    }
}
