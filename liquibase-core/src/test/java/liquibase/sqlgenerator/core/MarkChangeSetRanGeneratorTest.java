package liquibase.sqlgenerator.core;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.statementlogic.StatementLogicChain;
import liquibase.changelog.ChangeSet;
import liquibase.sdk.database.MockDatabase;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.statement.core.MarkChangeSetRanStatement;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class MarkChangeSetRanGeneratorTest extends AbstractSqlGeneratorTest<MarkChangeSetRanStatement> {
    public MarkChangeSetRanGeneratorTest() throws Exception {
        super(new MarkChangeSetRanGenerator());
    }

    @Override
    protected MarkChangeSetRanStatement createSampleSqlStatement() {
        return new MarkChangeSetRanStatement(new ChangeSet("1", "a", false, false, "c", null, null, null), ChangeSet.ExecType.EXECUTED);
    }

    @Test
    public void generateSql_markRan() {
        Action[] actions = new MarkChangeSetRanGenerator().generateActions(new MarkChangeSetRanStatement(new ChangeSet("1", "a", false, false, "c", null, null, null), ChangeSet.ExecType.MARK_RAN), new ExecutionEnvironment(new MockDatabase()), new StatementLogicChain(null));
        assertEquals(1, actions.length);
        assertTrue(actions[0].describe(), actions[0].describe().contains("MARK_RAN"));
    }
}
