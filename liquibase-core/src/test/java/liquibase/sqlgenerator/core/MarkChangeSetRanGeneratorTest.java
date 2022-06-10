package liquibase.sqlgenerator.core;

import liquibase.ContextExpression;
import liquibase.Labels;
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

    /**
     * Ensure that upon running an update on a changeset that has been run before, we still update the labels,
     * contexts and comments columns in the DBCL table.
     */
    @Test
    public void generateSqlSecondRunUpdatesLabelsContextsComments() {
        String changeSetContextExpression = "changeSetContext1 AND changeSetContext2";
        DatabaseChangeLog rootChangeLog = new DatabaseChangeLog();
        rootChangeLog.setContexts(new ContextExpression("rootContext1 OR (rootContext2) AND (rootContext3)"));
        DatabaseChangeLog childChangeLog = new DatabaseChangeLog();
        childChangeLog.setContexts(new ContextExpression("childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3"));
        childChangeLog.setIncludeContexts(new ContextExpression("includeContext1, includeContext2 AND includeContext3"));
        childChangeLog.setParentChangeLog(rootChangeLog);
        ChangeSet changeSet = new ChangeSet("1", "a", false, false, "c", changeSetContextExpression, null, childChangeLog);
        changeSet.setComments("comment12345");
        changeSet.setLabels(new Labels("newlabel123"));

        Sql[] sqls = new MarkChangeSetRanGenerator().generateSql(new MarkChangeSetRanStatement(changeSet, ChangeSet.ExecType.RERAN), new MockDatabase(), new MockSqlGeneratorChain());
        String sql = sqls[0].toSql();
        assertTrue(sql.contains("COMMENTS = 'comment12345'"));
        assertTrue(sql.contains("CONTEXTS = '(childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3) AND (includeContext1, includeContext2 AND includeContext3) AND (rootContext1 OR (rootContext2) AND (rootContext3)) AND (changeSetContext1 AND changeSetContext2)'"));
        assertTrue(sql.contains("LABELS = 'newlabel123'"));
    }

    @Test
    public void getLabelsColumn() {
        final DatabaseChangeLog changeLog = new DatabaseChangeLog();
        final ChangeSet changeSet = new ChangeSet("1", "a", false, false, "c", null, null, changeLog);

        changeSet.setLabels(null);
        assert new MarkChangeSetRanGenerator().getLabelsColumn(changeSet) == null;

        changeSet.setLabels(new Labels(""));
        assert new MarkChangeSetRanGenerator().getLabelsColumn(changeSet) == null;

        changeSet.setLabels(new Labels("a"));
        assertEquals("a", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));

        changeSet.setLabels(new Labels("a", "b"));
        assertEquals("a,b", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));

        changeLog.setIncludeLabels(new Labels("p1"));
        assertEquals("p1,a,b", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));

        changeSet.setLabels(new Labels("a"));
        assertEquals("p1,a", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));

        changeSet.setLabels(new Labels());
        assertEquals("p1", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));

        changeSet.setLabels(null);
        assertEquals("p1", new MarkChangeSetRanGenerator().getLabelsColumn(changeSet));
    }

    @Test
    public void getContextsColumn() {
        final DatabaseChangeLog changeLog = new DatabaseChangeLog();
        final ChangeSet changeSet = new ChangeSet("1", "a", false, false, "c", null, null, changeLog);

        changeSet.setContexts(null);
        assert new MarkChangeSetRanGenerator().getContextsColumn(changeSet) == null;

        changeSet.setContexts(new ContextExpression(""));
        assert new MarkChangeSetRanGenerator().getContextsColumn(changeSet) == null;

        changeSet.setContexts(new ContextExpression("a"));
        assertEquals("a", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContexts(new ContextExpression("a or b"));
        assertEquals("(a or b)", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeLog.setIncludeContexts(new ContextExpression("p1"));
        assertEquals("p1 AND (a or b)", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContexts(new ContextExpression("a"));
        assertEquals("p1 AND a", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContexts(new ContextExpression());
        assertEquals("p1", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContexts(null);
        assertEquals("p1", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));
    }}
