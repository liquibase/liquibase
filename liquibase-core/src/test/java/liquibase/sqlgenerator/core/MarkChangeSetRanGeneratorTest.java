package liquibase.sqlgenerator.core;

import liquibase.ContextExpression;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.MockDatabase;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.MarkChangeSetRanStatement;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void generateSqlWithComplexContext() {
        String changeSetContextExpression = "changeSetContext1 AND changeSetContext2";
        DatabaseChangeLog rootChangeLog = new DatabaseChangeLog();
        rootChangeLog.setContexts(new ContextExpression("rootContext1 OR (rootContext2) AND (rootContext3)"));
        DatabaseChangeLog childChangeLog = new DatabaseChangeLog();
        childChangeLog.setContexts(new ContextExpression("childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3"));
        childChangeLog.setIncludeContexts(new ContextExpression("includeContext1, includeContext2 AND includeContext3"));
        childChangeLog.setParentChangeLog(rootChangeLog);
        ChangeSet changeSet = new ChangeSet("1", "a", false, false, "c", changeSetContextExpression, null, childChangeLog);

        Sql[] sqls = new MarkChangeSetRanGenerator().generateSql(new MarkChangeSetRanStatement(changeSet, ChangeSet.ExecType.EXECUTED), new MockDatabase(), new MockSqlGeneratorChain());
        assertTrue(sqls[0].toSql(), sqls[0].toSql().contains("(childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3) AND " +
                                                                     "(includeContext1, includeContext2 AND includeContext3) AND " +
                                                                     "(rootContext1 OR (rootContext2) AND (rootContext3)) AND " +
                                                                     "(changeSetContext1 AND changeSetContext2)"));
    }
}
