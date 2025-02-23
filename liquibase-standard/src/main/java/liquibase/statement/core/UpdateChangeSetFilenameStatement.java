package liquibase.statement.core;

import liquibase.changelog.ChangeSet;
import liquibase.statement.AbstractSqlStatement;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdateChangeSetFilenameStatement extends AbstractSqlStatement {
    private final ChangeSet changeSet;
    private final String oldFilename;
}
