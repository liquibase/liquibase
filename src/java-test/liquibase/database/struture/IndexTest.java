package liquibase.database.struture;

import junit.framework.*;
import liquibase.database.struture.Index;

public class IndexTest extends TestCase {

    public void testCompareTo() throws Exception {
        Index index1 = new Index();

        assertEquals(0, index1.compareTo(index1));
        assertTrue(index1.compareTo(new String()) > 0);
    }

    public void testGetConnection() throws Exception {
        assertNull(new Index().getConnection());
    }
}