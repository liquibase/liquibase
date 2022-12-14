package liquibase.changelog.filter;

import liquibase.LabelExpression;
import liquibase.Labels;
import liquibase.changelog.ChangeSet;
import org.junit.Test;

import static org.junit.Assert.*;

public class LabelChangeSetFilterTest {

    @Test
    public void emptyLabels() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter(new LabelExpression());
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void nullLabels() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter();
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test public void reallyNullLabels(){
        LabelChangeSetFilter filter = new LabelChangeSetFilter(null);
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void nullListLabels() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter(new LabelExpression(""));
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void singleLabels() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter(new LabelExpression("TEST1"));
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1", "@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void multiLabels() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, @test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, @test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void multiLabelsSingleParameter() {
        LabelChangeSetFilter filter = new LabelChangeSetFilter(new LabelExpression("test1, test2"));
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, @test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, @test2"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3"));
        assertFalse(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @test1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @TEST1"));
        assertTrue(filter.accepts(testChangeSet).isAccepted());
    }

    @Test
    public void multiLabelIdenticalParameterHandling() {
        LabelChangeSetFilter filter1 = new LabelChangeSetFilter(new LabelExpression("test1", "test2"));
        LabelChangeSetFilter filter2 = new LabelChangeSetFilter(new LabelExpression("test1, test2"));
        ChangeSet testChangeSet = new ChangeSet(null);

        testChangeSet.setLabels(new Labels("test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, TEST1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels());
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels(""));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test1, @test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test1, @test2"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @test1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, TEST1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("test3, @TEST1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());

        testChangeSet.setLabels(new Labels("@test3, @TEST1"));
        assertEquals(filter1.accepts(testChangeSet).isAccepted(), filter2.accepts(testChangeSet).isAccepted());
    }
}
