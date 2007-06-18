package liquibase.migrator;

import junit.framework.TestCase;
import liquibase.migrator.exception.DuplicateChangeSetException;

public class DuplicateChangeSetExceptionTest extends TestCase {


    public void testDuplicateChangeSetException() throws Exception {
        DuplicateChangeSetException duplicateChangeSetException = new DuplicateChangeSetException("MESSAGE HERE");
        assertEquals("MESSAGE HERE", duplicateChangeSetException.getMessage());
    }
}