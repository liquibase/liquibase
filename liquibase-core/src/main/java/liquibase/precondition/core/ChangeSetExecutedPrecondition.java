package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.AbstractPrecondition;

public class ChangeSetExecutedPrecondition extends AbstractPrecondition {

    private String changeLogFile;
    private String id;
    private String author;

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

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

    @Override
    public Warnings warn(Database database) {
        return new Warnings();
    }

    @Override
    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }
    
    @Override
    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet, ChangeExecListener changeExecListener)
            throws PreconditionFailedException, PreconditionErrorException {
        ObjectQuotingStrategy objectQuotingStrategy = null;
        if (changeSet == null) {
            objectQuotingStrategy = ObjectQuotingStrategy.LEGACY;
        } else {
            objectQuotingStrategy = changeSet.getObjectQuotingStrategy();
        }
        String changeLogFile = getChangeLogFile();
        if (changeLogFile == null) {
            changeLogFile = changeLog.getLogicalFilePath();
        }
        ChangeSet interestedChangeSet = new ChangeSet(getId(), getAuthor(), false, false, changeLogFile, null, null, false, objectQuotingStrategy, changeLog);
        RanChangeSet ranChangeSet;
        try {
            ranChangeSet = database.getRanChangeSet(interestedChangeSet);
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
        if ((ranChangeSet == null) || (ranChangeSet.getExecType() == null) || !ranChangeSet.getExecType().ran) {
            throw new PreconditionFailedException("Change Set '"+interestedChangeSet.toString(false)+"' has not been run", changeLog, this);
        }
    }

    @Override
    public String getName() {
        return "changeSetExecuted";
    }
}
