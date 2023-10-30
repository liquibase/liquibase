package liquibase.changelog;

import org.junit.Test;

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
}
