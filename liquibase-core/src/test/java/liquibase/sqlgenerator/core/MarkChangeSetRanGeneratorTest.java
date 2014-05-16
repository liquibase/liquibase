package liquibase.sqlgenerator.core;

import liquibase.changelog.ChangeSet;
import liquibase.sdk.database.MockDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
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
        Sql[] sqls = new MarkChangeSetRanGenerator().generateSql(new MarkChangeSetRanStatement(new ChangeSet("1", "a", false, false, "c", null, null, null), ChangeSet.ExecType.MARK_RAN), new MockDatabase(), new MockSqlGeneratorChain());
        assertEquals(1, sqls.length);
        assertTrue(sqls[0].toSql(), sqls[0].toSql().contains("MARK_RAN"));
    }
}
