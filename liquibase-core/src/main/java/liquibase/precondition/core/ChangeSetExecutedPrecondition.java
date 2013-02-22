package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;

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

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        ChangeSet interestedChangeSet = new ChangeSet(getId(), getAuthor(), false, false, getChangeLogFile(), null, null, false, changeSet.getObjectQuotingStrategy());
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(interestedChangeSet);
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if (ranChangeSet == null || ranChangeSet.getExecType() == null || !ranChangeSet.getExecType().ran) {
            throw new PreconditionFailedException("Change Set '"+interestedChangeSet.toString(false)+"' has not been run", changeLog, this);
        }
    }

    public String getName() {
        return "changeSetExecuted";
    }
}
