package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.Scope;
import liquibase.change.core.TagDatabaseChange;
import liquibase.sqlgenerator.core.MarkChangeSetRanGenerator;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RanChangeSetTest {

    @Test
    public void is_same_as_when_both_changelogs_have_classpath_prefix() {
        RanChangeSet ranChangeSet = new RanChangeSet("classpath:/db/file.log", "1", "author", null, null, null, null, null, null, null, null, null);
        ChangeSet incomingChangeSet = new ChangeSet("1", "author", false, false, "classpath:/db/file.log", null, null, null);
        assertTrue(ranChangeSet.isSameAs(incomingChangeSet));
    }

    @Test
    public void is_same_when_we_use_relative_directories() {
        RanChangeSet ranChangeSet = new RanChangeSet("db/file.log", "1", "author", null, null, null, null, null, null, null, null, null);
        ChangeSet incomingChangeSet = new ChangeSet("1", "author", false, false, "db/../db/file.log", null, null, null);
        assertTrue(ranChangeSet.isSameAs(incomingChangeSet));
    }

    @Test
    public void is_not_same_when_changeset_has_classpath_prefix() {
        RanChangeSet ranChangeSet = new RanChangeSet("db/ran-changeset.log", "1", "author", null, null, null, null, null, null, null, null, null);
        ChangeSet incomingChangeSet = new ChangeSet("1", "author", false, false, "classpath:/db/file.log", null, null, null);
        assertFalse(ranChangeSet.isSameAs(incomingChangeSet));
    }

    @Test
    public void stored_change_log_falls_back_to_change_log() {
        RanChangeSet ranChangeSet = new RanChangeSet("db/file.log", "1", "author", null, null, null, null, null, null, null, null, null);

        assertEquals("db/file.log", ranChangeSet.getStoredChangeLog());
    }

    @Test
    public void stored_change_log_uses_explicit_stored_path() {
        RanChangeSet ranChangeSet = new RanChangeSet("logical/file.log", "1", "author", null, null, null, null, null, null, null, null, null, "physical/file.log");

        assertEquals("physical/file.log", ranChangeSet.getStoredChangeLog());
    }

    @Test
    public void constructor_with_order_executed_mirrors_mark_change_set_ran_generator() throws Exception {
        final var changeSet = createChangeSetWithInheritedContextsAndLabels();
        final var tagChange = new TagDatabaseChange();
        tagChange.setTag("v1.0");
        changeSet.addChange(tagChange);
        final var generator = new ExposingMarkChangeSetRanGenerator();

        final RanChangeSet ranChangeSet = Scope.child(Collections.singletonMap(Scope.Attr.deploymentId.name(), "1234567890"),
                () -> new RanChangeSet(changeSet, ChangeSet.ExecType.EXECUTED, 42));

        assertEquals("1234567890", ranChangeSet.getDeploymentId());
        assertEquals(MarkChangeSetRanGenerator.getLiquibaseBuildVersion(), ranChangeSet.getLiquibaseVersion());
        assertEquals(Integer.valueOf(42), ranChangeSet.getOrderExecuted());
        assertEquals(MarkChangeSetRanGenerator.getTagFromChangeset(changeSet), ranChangeSet.getTag());
        assertEquals("v1.0", ranChangeSet.getTag());

        assertEquals(generator.contextsColumn(changeSet), ranChangeSet.getContextExpression().toString());
        assertEquals("(childContext1, childContext2) AND includeContext AND rootContext AND (changeSetContext1 AND changeSetContext2)",
                ranChangeSet.getContextExpression().toString());
        assertEquals(new Labels(generator.labelsColumn(changeSet)).toString(), ranChangeSet.getLabels().toString());
        assertEquals("includelabel1,includelabel2,rootlabel,changesetlabel", ranChangeSet.getLabels().toString());
    }

    @Test
    public void constructor_with_order_executed_keeps_fields_of_existing_constructor() {
        final var changeSet = createChangeSetWithInheritedContextsAndLabels();
        changeSet.setComments("some comments");

        final var expected = new RanChangeSet(changeSet, ChangeSet.ExecType.MARK_RAN, null, null);
        final var actual = new RanChangeSet(changeSet, ChangeSet.ExecType.MARK_RAN, null);

        assertEquals(expected.getChangeLog(), actual.getChangeLog());
        assertEquals(expected.getStoredChangeLog(), actual.getStoredChangeLog());
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getAuthor(), actual.getAuthor());
        assertEquals(expected.getLastCheckSum(), actual.getLastCheckSum());
        assertEquals(expected.getExecType(), actual.getExecType());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getComments(), actual.getComments());
        assertNotNull(actual.getDateExecuted());
        assertNull(actual.getTag());
        assertNull(actual.getOrderExecuted());
        assertEquals(expected, actual);
    }

    @Test
    public void constructor_with_order_executed_handles_changeset_without_contexts_and_labels() {
        final var changeSet = new ChangeSet("1", "author", false, false, "db/file.log", null, null, null);

        final var ranChangeSet = new RanChangeSet(changeSet, ChangeSet.ExecType.EXECUTED, 1);

        assertTrue(ranChangeSet.getContextExpression().isEmpty());
        assertTrue(ranChangeSet.getLabels().isEmpty());
        assertNull(ranChangeSet.getTag());
        assertEquals(Scope.getCurrentScope().getDeploymentId(), ranChangeSet.getDeploymentId());
    }

    private static ChangeSet createChangeSetWithInheritedContextsAndLabels() {
        final var rootChangeLog = new DatabaseChangeLog("db/root.xml");
        rootChangeLog.setContextFilter(new ContextExpression("rootContext"));
        rootChangeLog.setIncludeLabels(new Labels("rootLabel"));
        final var childChangeLog = new DatabaseChangeLog("db/child.xml");
        childChangeLog.setContextFilter(new ContextExpression("childContext1, childContext2"));
        childChangeLog.setIncludeContextFilter(new ContextExpression("includeContext"));
        childChangeLog.setIncludeLabels(new Labels("includeLabel1, includeLabel2"));
        childChangeLog.setParentChangeLog(rootChangeLog);
        final var changeSet = new ChangeSet("1", "author", false, false, "db/child.xml",
                "changeSetContext1 AND changeSetContext2", null, childChangeLog);
        changeSet.setLabels(new Labels("changeSetLabel"));
        return changeSet;
    }

    private static class ExposingMarkChangeSetRanGenerator extends MarkChangeSetRanGenerator {
        String contextsColumn(final ChangeSet changeSet) {
            return getContextsColumn(changeSet);
        }

        String labelsColumn(final ChangeSet changeSet) {
            return getLabelsColumn(changeSet);
        }
    }
}
