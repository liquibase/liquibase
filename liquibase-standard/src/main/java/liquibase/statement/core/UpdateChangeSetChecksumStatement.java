package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class UpdateChangeSetChecksumStatement extends AbstractSqlStatement {

    private final ChangeSet changeSet;

    public UpdateChangeSetChecksumStatement(ChangeSet changeSet) {
        this.changeSet = changeSet;
    }

}
