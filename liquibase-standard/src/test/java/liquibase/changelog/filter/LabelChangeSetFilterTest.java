package liquibase.changelog.filter;

import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import org.junit.Test;

import static org.junit.Assert.*;

public class LabelChangeSetFilterTest {

    @Test
    public void emptyLabels() {
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);
        testChangeSet.setLabels(new Labels());

        filter = new LabelChangeSetFilter(new LabelExpression("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1,test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1,test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1, @test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1,@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1, @test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1,@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void nullLabels() {
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);

        filter = new LabelChangeSetFilter(new LabelExpression("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test public void reallyNullLabels(){
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);
        testChangeSet.setLabels(null);

        filter = new LabelChangeSetFilter(new LabelExpression("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void nullListLabels() {
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);
        testChangeSet.setLabels(new Labels(""));

        filter = new LabelChangeSetFilter(new LabelExpression("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void singleLabels() {
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);
        testChangeSet.setLabels(new Labels("TEST1"));

        filter = new LabelChangeSetFilter(new LabelExpression("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        filter = new LabelChangeSetFilter(new LabelExpression("@test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void inheritableLabels() {
        LabelChangeSetFilter filter;

        //
        // Command line argument equivalent
        //
        filter = new LabelChangeSetFilter(new LabelExpression("test1"));

        //
        // Changeset with no labels
        //
        ChangeSet testChangeSet = createTestChangeSet("test1");
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        //
        // Changeset with a label of "test2"
        //
        testChangeSet = createTestChangeSet("test1");
        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        //
        // Command line argument equivalent
        //
        filter = new LabelChangeSetFilter(new LabelExpression("test2"));

        //
        // Changeset with no labels
        //
        testChangeSet = createTestChangeSet("test1");
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        //
        // Changeset with a label of "test2"
        //
        testChangeSet = createTestChangeSet("test1");
        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        //
        // Changeset with no labels and no included labels
        //
        testChangeSet = createTestChangeSet(null);
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        //
        // Changeset with label of "test2" and no included labels
        //
        testChangeSet = createTestChangeSet(null);
        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        //
        // Changeset with label of "test1" and no included labels
        //
        testChangeSet = createTestChangeSet(null);
        testChangeSet.setLabels(new Labels("test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    //
    // Create the changeset and add an include label
    //
    private ChangeSet createTestChangeSet(String label) {
        DatabaseChangeLog changeLog = new DatabaseChangeLog();
        ChangeSet testChangeSet = new ChangeSet(changeLog);
        if (label != null) {
            testChangeSet.getChangeLog().setIncludeLabels(new Labels(label));
        }
        changeLog.addChangeSet(testChangeSet);

        return testChangeSet;
    }

    @Test
    public void multiLabels() {
        LabelChangeSetFilter filter;
        ChangeSet testChangeSet = new ChangeSet(null);
        Labels[] testLabelsArray = {new Labels("test1", "test2"), new Labels("test1, test2"), new Labels("test1,test2")};

        for (Labels testLabels : testLabelsArray) {
            testChangeSet.setLabels(testLabels);

            filter = new LabelChangeSetFilter(new LabelExpression("test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test1, test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test3"));
            assertFalse(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test3, test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test3, TEST1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression());
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression(""));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test1, test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test1, @test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test1, @test2"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test3"));
            assertFalse(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test3, test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test3, @test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test3, @test1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test3, TEST1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("test3, @TEST1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());

            filter = new LabelChangeSetFilter(new LabelExpression("@test3, @TEST1"));
            assertTrue(filter.accepts(testChangeSet).isAccepted());
        }
    }
}
