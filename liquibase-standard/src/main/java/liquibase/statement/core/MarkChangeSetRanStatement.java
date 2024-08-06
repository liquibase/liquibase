package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class MarkChangeSetRanStatement extends AbstractSqlStatement {

    private final ChangeSet changeSet;

    private final ChangeSet.ExecType execType;

    public MarkChangeSetRanStatement(ChangeSet changeSet, ChangeSet.ExecType execType) {
        this.changeSet = changeSet;
        this.execType = execType;
    }

}
