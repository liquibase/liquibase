package liquibase.sqlgenerator.core;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.database.core.MockDatabase;
import liquibase.sdk.resource.MockResourceAccessor;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.AbstractSqlGeneratorTest;
import liquibase.sqlgenerator.MockSqlGeneratorChain;
import liquibase.statement.core.MarkChangeSetRanStatement;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

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
        rootChangeLog.setContextFilter(new ContextExpression("rootContext1 OR (rootContext2) AND (rootContext3)"));
        DatabaseChangeLog childChangeLog = new DatabaseChangeLog();
        childChangeLog.setContextFilter(new ContextExpression("childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3"));
        childChangeLog.setIncludeContextFilter(new ContextExpression("includeContext1, includeContext2 AND includeContext3"));
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
        rootChangeLog.setContextFilter(new ContextExpression("rootContext1 OR (rootContext2) AND (rootContext3)"));
        DatabaseChangeLog childChangeLog = new DatabaseChangeLog();
        childChangeLog.setContextFilter(new ContextExpression("childChangeLogContext1, childChangeLogContext2 AND childChangeLogContext3"));
        childChangeLog.setIncludeContextFilter(new ContextExpression("includeContext1, includeContext2 AND includeContext3"));
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

        changeSet.setContextFilter(null);
        assert new MarkChangeSetRanGenerator().getContextsColumn(changeSet) == null;

        changeSet.setContextFilter(new ContextExpression(""));
        assert new MarkChangeSetRanGenerator().getContextsColumn(changeSet) == null;

        changeSet.setContextFilter(new ContextExpression("a"));
        assertEquals("a", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContextFilter(new ContextExpression("a or b"));
        assertEquals("(a or b)", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeLog.setIncludeContextFilter(new ContextExpression("p1"));
        assertEquals("p1 AND (a or b)", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContextFilter(new ContextExpression("a"));
        assertEquals("p1 AND a", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContextFilter(new ContextExpression());
        assertEquals("p1", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));

        changeSet.setContextFilter(null);
        assertEquals("p1", new MarkChangeSetRanGenerator().getContextsColumn(changeSet));
    }

    @Test
    public void makeSureDescriptionIsTruncatedWhenALongPathIsSet() throws Exception {

        MockResourceAccessor resourceAccessor = new MockResourceAccessor();
        String filePath = "This/is/a/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/very/ver/long/test/change/path";

        resourceAccessor.setContent(filePath, "test");
        final DatabaseChangeLog changeLog = new DatabaseChangeLog();
        final ChangeSet changeSet = new ChangeSet("1", "a", false, false, "c", null, null, changeLog);

        SQLFileChange sqlFileChange = new SQLFileChange();
        sqlFileChange.setPath(filePath);
        changeSet.addChange(sqlFileChange);

        MarkChangeSetRanStatement changeSetRanStatement = new MarkChangeSetRanStatement(changeSet, ChangeSet.ExecType.EXECUTED);
        MarkChangeSetRanGenerator changeSetRanGenerator = new MarkChangeSetRanGenerator();
        Map<String, Object> newMap = new HashMap<>();
        newMap.put(Scope.Attr.resourceAccessor.name(), resourceAccessor);

        String sql = Scope.child(newMap, () -> changeSetRanGenerator.generateSql(changeSetRanStatement, new MockDatabase(), new MockSqlGeneratorChain())[0].toSql());

        final int descriptionColumnIndex = 18;
        String databaseChangeLogDescription = sql.split(",")[descriptionColumnIndex];
        String truncatedPath = databaseChangeLogDescription.split("path=")[1].split("'")[0];

        assertTrue(truncatedPath.endsWith("/..."));
        assertTrue(truncatedPath.length() <= filePath.length());

    }
}
