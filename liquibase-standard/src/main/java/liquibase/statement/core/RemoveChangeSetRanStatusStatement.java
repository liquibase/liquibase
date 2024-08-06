package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class RemoveChangeSetRanStatusStatement extends AbstractSqlStatement {
    private final ChangeSet changeSet;

    public RemoveChangeSetRanStatusStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

}
