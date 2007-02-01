package liquibase.database.struture;

import junit.framework.*;
import liquibase.database.struture.Sequence;

public class SequenceTest extends TestCase {
    Sequence sequence;

    public void testSequence() throws Exception {
        Sequence sequence = new Sequence("name", 1, 2, 3);
        assertEquals("name", sequence.getName());
        assertEquals(1, sequence.getIncrementBy().intValue());
        assertEquals(2, sequence.getMinValue().intValue());
        assertEquals(3, sequence.getMaxValue().intValue());
    }

    public void testEquals() throws Exception {
        Sequence sequence1 = new Sequence("name", 1, 2, 3);
        assertTrue(sequence1.equals(sequence1));
        assertFalse(sequence1.equals(null));
        assertTrue(sequence1.equals(new Sequence("name", 4, 5, 6)));
        assertFalse(sequence1.equals(new Sequence("nameB", 4, 5, 6)));

    }
}