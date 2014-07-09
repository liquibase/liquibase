package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;

/**
 * Removes the fact that a change set ran.
 */
public class RemoveChangeSetRanStatusStatement extends AbstractStatement {

    public static final String CHANGE_SET = "changeSet";

    public RemoveChangeSetRanStatusStatement() {
    }

    public RemoveChangeSetRanStatusStatement(ChangeSet changeSet) {
        setChangeSet(changeSet);
    }

    public ChangeSet getChangeSet() {
        return getAttribute(CHANGE_SET, ChangeSet.class);
    }

    public RemoveChangeSetRanStatusStatement setChangeSet(ChangeSet changeSet) {
        return (RemoveChangeSetRanStatusStatement) setAttribute(CHANGE_SET, changeSet);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return null;
    }
}
