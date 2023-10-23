package liquibase.changelog;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RanChangeSetTest {

    @Test
    public void is_same_as_when_both_changelogs_have_classpath_prefix() throws Exception {
        RanChangeSet ranChangeSet = new RanChangeSet("classpath:/db/file.log", "1", "author", null, null, null, null, null, null, null, null, null);
        ChangeSet incomingChangeSet = new ChangeSet("1", "author", false, false, "classpath:/db/file.log", null, null, null);
        assertTrue(ranChangeSet.isSameAs(incomingChangeSet));
    }
}