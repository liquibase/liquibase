package liquibase.precondition.core;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.PreconditionErrorException;
import liquibase.exception.PreconditionFailedException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.precondition.Precondition;

public class ObjectQuotingStrategyPrecondition implements Precondition {
    private ObjectQuotingStrategy strategy;

    public String getName() {
        return "expectedQuotingStrategy";
    }

    public Warnings warn(Database database) {
        return new Warnings();
    }

    public ValidationErrors validate(Database database) {
        return new ValidationErrors();
    }

    public void check(Database database, DatabaseChangeLog changeLog, ChangeSet changeSet) throws PreconditionFailedException, PreconditionErrorException {
        try {
            if (changeLog.getObjectQuotingStrategy() != strategy) {
                throw new PreconditionFailedException("Quoting strategy Precondition failed: expected "
                        + strategy +", got "+changeSet.getObjectQuotingStrategy(), changeLog, this);
            }
        } catch (PreconditionFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new PreconditionErrorException(e, changeLog, this);
        }
    }

    public void setStrategy(String strategy) {
        this.strategy = ObjectQuotingStrategy.valueOf(strategy);
    }
}
