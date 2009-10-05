package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.MarkChangeSetRanStatement;

public class MarkChangeSetRanGeneratorTest extends AbstractSqlGeneratorTest<MarkChangeSetRanStatement> {
    public MarkChangeSetRanGeneratorTest() throws Exception {
        super(new MarkChangeSetRanGenerator());
    }

    protected MarkChangeSetRanStatement createSampleSqlStatement() {
        return new MarkChangeSetRanStatement(new ChangeSet("1", "a", false, false, "c", "d", null, null), ChangeSet.ExecType.EXECUTED);
    }
}
