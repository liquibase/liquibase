package liquibase.changelog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
}
