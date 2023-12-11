package liquibase.changelog.filter;

import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
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
